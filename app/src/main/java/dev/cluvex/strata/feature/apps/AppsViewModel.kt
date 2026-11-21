package dev.cluvex.strata.feature.apps

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cluvex.strata.R
import dev.cluvex.strata.core.apk.AppSigners
import dev.cluvex.strata.core.apk.ApkExtractor
import dev.cluvex.strata.core.apk.InstalledApp
import dev.cluvex.strata.core.apk.InstalledApps
import dev.cluvex.strata.core.apk.InstalledSigners
import dev.cluvex.strata.core.dhizuku.DhizukuBridge
import dev.cluvex.strata.core.i18n.tr
import dev.cluvex.strata.core.i18n.trPlural
import dev.cluvex.strata.feature.explorer.TaskState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class AppFilter { ALL, USER, SYSTEM, LAUNCHABLE, RESTRICTED, DEBUG_SIGNED }
enum class RestrictedKind { DISABLED, HIDDEN, SUSPENDED }
enum class AppSort { NAME, PACKAGE, INSTALL_TIME, UPDATE_TIME, SIZE }

data class AppsUiState(
    val loading: Boolean = true,
    val all: List<InstalledApp> = emptyList(),
    val dhizukuAvailable: Boolean = false,
    val query: String = "",
    val selected: Set<String> = emptySet(),
    val task: TaskState? = null,
    val message: String? = null,
    val lastExtracted: File? = null,
    val filter: AppFilter = AppFilter.ALL,
    val signerCache: Map<String, Boolean> = emptyMap(),
    val signerLoading: Boolean = false,
    val hiddenPackages: Set<String> = emptySet(),
    val filters: Set<AppFilter> = setOf(AppFilter.ALL),
    val restrictedKinds: Set<RestrictedKind> = RestrictedKind.entries.toSet(),
    val backend: dev.cluvex.strata.core.control.AppControlBridge.Backend = dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO,
    val availableBackends: Set<dev.cluvex.strata.core.control.AppControlBridge.Backend> = setOf(dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO),
    val alwaysAvailableBackends: Set<dev.cluvex.strata.core.control.AppControlBridge.Backend> = setOf(dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO),
    val capabilities: Set<dev.cluvex.strata.core.control.AppControlBridge.Capability> = emptySet(),
    val controlLoaded: Boolean = false,
    val controlHiddenPackages: Set<String> = emptySet(),
    val controlSuspendedPackages: Set<String> = emptySet(),
    val resolvedBackend: dev.cluvex.strata.core.control.AppControlBridge.Backend = dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO,
    val sort: AppSort = AppSort.NAME,
    val ascending: Boolean = true,
    val sizeCache: Map<String, Long> = emptyMap(),
    val sizeLoading: Boolean = false,
    val permissionChecked: Boolean = false,
    val hasAppsPermission: Boolean = true,
    val showPermissionDialog: Boolean = false,
    val showReapplyButton: Boolean = false,
    val isInitialCheckDone: Boolean = false,
    val selfPackage: String = "",
) {
    val visible: List<InstalledApp>
        get() {
            val eff = if (filters.isEmpty()) setOf(AppFilter.ALL) else filters
            val filtered = all.asSequence()
                .filter { app ->
                    eff.any { f ->
                        when (f) {
                            AppFilter.ALL -> true
                            AppFilter.USER -> !app.system
                            AppFilter.SYSTEM -> app.system
                            AppFilter.LAUNCHABLE -> app.launchable
                            AppFilter.RESTRICTED -> restrictedKinds.any { k ->
                                when (k) {
                                    RestrictedKind.DISABLED -> !app.enabled
                                    RestrictedKind.HIDDEN -> app.packageName in hiddenPackages
                                    RestrictedKind.SUSPENDED -> app.suspended
                                }
                            }
                            AppFilter.DEBUG_SIGNED -> signerCache[app.packageName] == true
                        }
                    }
                }
                .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }
                .toList()
            val sorted = when (sort) {
                AppSort.NAME -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
                AppSort.PACKAGE -> filtered.sortedBy { it.packageName }
                AppSort.INSTALL_TIME -> filtered.sortedBy { it.firstInstall }
                AppSort.UPDATE_TIME -> filtered.sortedBy { it.lastUpdate }
                AppSort.SIZE -> filtered.sortedBy { sizeCache[it.packageName] ?: 0L }
            }
            return if (ascending) sorted else sorted.asReversed()
        }
}

data class SignerSummary(
    val label: String,
    val shortHash: String,
    val platform: Boolean,
    val debug: Boolean,
    val rotated: Boolean,
    val multiple: Boolean,
)

class AppsViewModel(app: Application) : AndroidViewModel(app) {

    private val pm = app.packageManager
    private val _state = MutableStateFlow(AppsUiState())
    val state: StateFlow<AppsUiState> = _state.asStateFlow()

    private val _requestPermissionEvent = MutableSharedFlow<String>()
    val requestPermissionEvent: SharedFlow<String> = _requestPermissionEvent

    private val permissionGetInstalledApps = "com.android.permission.GET_INSTALLED_APPS"

    private val icons = LruCache<String, Bitmap>(256)
    private val signerSummaries = LruCache<String, SignerSummary>(1024)

    private val platformSha256: String? by lazy {
        InstalledSigners.of(pm, "android")?.primary?.let(InstalledSigners::sha256)
    }

    init {
        _state.update { it.copy(selfPackage = getApplication<Application>().packageName) }
        viewModelScope.launch {
            DhizukuBridge.state.collect { status ->
                _state.update { it.copy(dhizukuAvailable = status == DhizukuBridge.Status.READY) }
            }
        }
        checkAppsPermissionAndRefresh()
    }

    private fun isPermissionDeclared(context: android.content.Context): Boolean {
        return try {
            context.packageManager.getPermissionInfo(permissionGetInstalledApps, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun hasPermission(context: android.content.Context): Boolean {
        if (!isPermissionDeclared(context)) return true
        return ContextCompat.checkSelfPermission(context, permissionGetInstalledApps) == PackageManager.PERMISSION_GRANTED
    }

    fun checkAppsPermissionAndRefresh(silent: Boolean = false) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val declared = isPermissionDeclared(ctx)
            val granted = hasPermission(ctx)
            
            if (granted) {
                // 如果有权限了，关闭弹窗并刷新
                _state.update { it.copy(showPermissionDialog = false) }
                reload()
            } else {
                // 如果还是没权限
                _state.update { 
                    it.copy(
                        permissionChecked = true,
                        hasAppsPermission = false,
                        showReapplyButton = declared,
                        loading = false // 确保不卡加载圈
                    ) 
                }
                if (!silent) {
                    // 如果是用户主动点刷新，且没权限，确保弹窗还在
                    _state.update { it.copy(showPermissionDialog = true) }
                }
            }
        }
    }

    fun requestSystemPermission() {
        viewModelScope.launch {
            _requestPermissionEvent.emit(permissionGetInstalledApps)
        }
    }

    fun onPermissionResult() {
        viewModelScope.launch {
            val declared = isPermissionDeclared(getApplication())
            val granted = hasPermission(getApplication())
            
            _state.update { 
                it.copy(
                    hasAppsPermission = granted,
                    // 如果系统支持且仍未授权，强制弹 App 内弹窗
                    showPermissionDialog = declared && !granted, 
                    showReapplyButton = declared && !granted,
                    isInitialCheckDone = true
                ) 
            }
            // 只有在获得权限时，才需要重新读取列表。
            // 如果被拒绝了，列表本来也没有变化，直接让 UI 弹窗即可，避免闪烁。
            if (granted) {
                reload()
            }
        }
    }

    fun dismissPermissionDialog() {
        _state.update { it.copy(showPermissionDialog = false) }
    }

    fun openAppSettings() {
        val ctx = getApplication<Application>()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", ctx.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(intent) }
        _state.update { it.copy(showPermissionDialog = false) }
    }

    fun reload() {
        viewModelScope.launch {
            // 核心拦截：刷新前先检查权限
            val ctx = getApplication<Application>()
            val declared = isPermissionDeclared(ctx)
            val granted = hasPermission(ctx)
            
            if (declared && !granted) {
                // 如果没有权限，不执行加载，直接弹 App 弹窗
                _state.update { 
                    it.copy(
                        loading = false, // 确保不会卡在加载圈
                        showPermissionDialog = true,
                        showReapplyButton = true,
                        hasAppsPermission = false
                    ) 
                }
                return@launch
            }

            _state.update { it.copy(loading = true) }
            val apps = withContext(Dispatchers.IO) { InstalledApps(pm).list(includeSystem = true) }
            _state.update {
                it.copy(
                    loading = false,
                    all = apps,
                    selected = it.selected.filterTo(HashSet()) { p -> apps.any { a -> a.packageName == p } },
                    signerCache = it.signerCache.filterKeys { p -> apps.any { a -> a.packageName == p } },
                    sizeCache = it.sizeCache.filterKeys { p -> apps.any { a -> a.packageName == p } },
                )
            }
            if (_state.value.sort == AppSort.SIZE) loadSizes()
        }
    }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun toggleFilter(f: AppFilter) {
        _state.update {
            val cur = if (it.filters.isEmpty()) setOf(AppFilter.ALL) else it.filters
            val next = when {
                f == AppFilter.ALL -> setOf(AppFilter.ALL)
                f in cur -> (cur - f).ifEmpty { setOf(AppFilter.ALL) }
                else -> (cur - AppFilter.ALL) + f
            }
            it.copy(filters = next)
        }
        if (f == AppFilter.DEBUG_SIGNED && _state.value.signerCache.isEmpty() && !_state.value.signerLoading) loadSigners()
    }

    fun toggleRestrictedKind(k: RestrictedKind) = _state.update {
        val cur = it.restrictedKinds
        val next = if (k in cur) cur - k else cur + k
        it.copy(restrictedKinds = if (next.isEmpty()) RestrictedKind.entries.toSet() else next)
    }

    fun setBackend(b: dev.cluvex.strata.core.control.AppControlBridge.Backend) {
        _state.update { it.copy(backend = b) }
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val settingsRepo = dev.cluvex.strata.core.data.SettingsRepository(getApplication())
            val settings = settingsRepo.settings.first()
            val resolved = dev.cluvex.strata.core.control.AppControlBridge.resolve(b, ctx, settings)
                ?: dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO
            val caps = dev.cluvex.strata.core.control.AppControlBridge.capabilities(resolved)
            _state.update { it.copy(resolvedBackend = resolved, capabilities = caps) }
            settingsRepo.setControlBackend(b.name)
        }
    }

    fun setSort(s: AppSort) {
        _state.update { it.copy(sort = s) }
        if (s == AppSort.SIZE && _state.value.sizeCache.isEmpty() && !_state.value.sizeLoading) loadSizes()
    }

    fun toggleAscending() = _state.update { it.copy(ascending = !it.ascending) }

    private fun loadSigners() {
        viewModelScope.launch {
            _state.update { it.copy(signerLoading = true) }
            val snapshot = _state.value.all
            val map = withContext(Dispatchers.IO) {
                snapshot.mapNotNull { app -> signerSummary(app.packageName)?.let { app.packageName to it.debug } }.toMap()
            }
            _state.update { it.copy(signerCache = map, signerLoading = false) }
        }
    }

    private fun loadSizes() {
        viewModelScope.launch {
            _state.update { it.copy(sizeLoading = true) }
            val snapshot = _state.value.all
            val map = withContext(Dispatchers.IO) {
                snapshot.associate { app -> app.packageName to app.apkPaths.sumOf { File(it).length() } }
            }
            _state.update { it.copy(sizeCache = map, sizeLoading = false) }
        }
    }

    fun toggle(pkg: String) = _state.update {
        it.copy(selected = if (pkg in it.selected) it.selected - pkg else it.selected + pkg)
    }

    fun selectAll() = _state.update { s -> s.copy(selected = s.visible.mapTo(HashSet()) { it.packageName }) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    suspend fun signerSummary(pkg: String): SignerSummary? {
        signerSummaries.get(pkg)?.let { return it }
        return withContext(Dispatchers.IO) {
            val signers = InstalledSigners.of(pm, pkg) ?: return@withContext null
            val cert = signers.primary ?: return@withContext null
            SignerSummary(
                label = InstalledSigners.label(cert),
                shortHash = InstalledSigners.shortHash(cert),
                platform = platformSha256 != null && InstalledSigners.sha256(cert) == platformSha256,
                debug = InstalledSigners.isDebugKey(cert),
                rotated = signers.past.isNotEmpty(),
                multiple = signers.multipleSigners,
            ).also { signerSummaries.put(pkg, it) }
        }
    }

    suspend fun signers(pkg: String): AppSigners? = withContext(Dispatchers.IO) { InstalledSigners.of(pm, pkg) }

    suspend fun icon(pkg: String): Bitmap? {
        icons.get(pkg)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching { pm.getApplicationIcon(pkg).toBitmap(96, 96) }.getOrNull()?.also { icons.put(pkg, it) }
        }
    }

    fun extract(apps: List<InstalledApp>, dir: String, pattern: String) {
        if (apps.isEmpty()) return
        val target = if (dir.isBlank()) defaultDir() else File(dir)
        viewModelScope.launch {
            var done = 0
            val failed = ArrayList<String>()
            try {
                for ((i, app) in apps.withIndex()) {
                    _state.update { it.copy(task = TaskState(tr(R.string.apps_extracting), app.label, i.toFloat() / apps.size, indeterminate = false)) }
                    val result = runCatching { withContext(Dispatchers.IO) { ApkExtractor.extract(app, target, pattern) } }
                    if (result.isSuccess) done++ else failed += app.label
                    _state.update { it.copy(task = TaskState(tr(R.string.apps_extracting), app.label, (i + 1f) / apps.size, indeterminate = false)) }
                }
            } catch (e: CancellationException) {
                throw e
            } finally {
                _state.update { it.copy(task = null) }
            }
            val msg = if (failed.isEmpty()) {
                trPlural(R.plurals.apps_extracted, done, done, target.path)
            } else {
                tr(R.string.apps_extract_partial, done, failed.size, failed.joinToString())
            }
            _state.update { it.copy(message = msg, selected = emptySet(), lastExtracted = if (done > 0) target else null) }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun loadControl(context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(controlLoaded = false) }
            val settingsRepo = dev.cluvex.strata.core.data.SettingsRepository(getApplication())
            val settings = settingsRepo.settings.first()
            val avail = dev.cluvex.strata.core.control.AppControlBridge.available(context, settings)
            val persisted = runCatching {
                dev.cluvex.strata.core.control.AppControlBridge.Backend.valueOf(settings.controlBackend)
            }.getOrDefault(dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO)
            val eff = if (persisted in avail) persisted else dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO
            val resolved = dev.cluvex.strata.core.control.AppControlBridge.resolve(eff, context, settings)
                ?: dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO
            val caps = dev.cluvex.strata.core.control.AppControlBridge.capabilities(resolved)
            
            val hidden = dev.cluvex.strata.core.control.AppControlBridge.hiddenPackages(context, settings)
            val suspendedRaw = dev.cluvex.strata.core.control.AppControlBridge.suspendedPackages(context, settings)
            val suspended = suspendedRaw + _state.value.all.filter { it.suspended }.map { it.packageName }.toSet()
            
            _state.update { it.copy(
                availableBackends = avail, 
                backend = eff, 
                resolvedBackend = resolved, 
                capabilities = caps, 
                controlLoaded = true,
                hiddenPackages = hidden,
                controlHiddenPackages = hidden,
                controlSuspendedPackages = suspended
            ) }
        }
    }

    fun control(context: android.content.Context, pkg: String, op: String, value: Boolean = false) {
        viewModelScope.launch {
            val settingsRepo = dev.cluvex.strata.core.data.SettingsRepository(getApplication())
            val settings = settingsRepo.settings.first()
            val desired = _state.value.backend
            
            val isAddingLimit = (op == "enabled" && !value) || (op == "hidden" && value) || (op == "suspended" && value)
            
            val err: String? = if (!isAddingLimit && desired == dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO) {
                withContext(Dispatchers.IO) {
                    dev.cluvex.strata.core.control.AppControlBridge.autoLiftLimit(context, pkg, op, settings)
                }
            } else {
                val resolved = if (isAddingLimit || desired != dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO) {
                    dev.cluvex.strata.core.control.AppControlBridge.resolve(desired, context, settings)
                } else {
                    null
                } ?: dev.cluvex.strata.core.control.AppControlBridge.resolve(desired, context, settings)

                if (resolved == null || resolved == dev.cluvex.strata.core.control.AppControlBridge.Backend.AUTO) {
                    _state.update { it.copy(message = tr(R.string.apps_control_no_backend)) }
                    return@launch
                }
                
                withContext(Dispatchers.IO) {
                    when (op) {
                        "force_stop" -> dev.cluvex.strata.core.control.AppControlBridge.forceStop(context, resolved, pkg)
                        "enabled" -> dev.cluvex.strata.core.control.AppControlBridge.setEnabled(context, resolved, pkg, value)
                        "hidden" -> dev.cluvex.strata.core.control.AppControlBridge.setHidden(context, resolved, pkg, value)
                        "suspended" -> dev.cluvex.strata.core.control.AppControlBridge.setSuspended(context, resolved, pkg, value)
                        else -> "unknown op"
                    }
                }
            }
            
            _state.update { it.copy(message = if (err == null) tr(R.string.apps_control_done) else tr(R.string.apps_control_failed, err)) }
            
            if (op != "force_stop") {
                reloadAndSync(context)
            }
        }
    }

    private fun reloadAndSync(context: android.content.Context) {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) { InstalledApps(pm).list(includeSystem = true) }
            _state.update {
                it.copy(
                    loading = false,
                    all = apps,
                    selected = it.selected.filterTo(HashSet()) { p -> apps.any { a -> a.packageName == p } },
                    signerCache = it.signerCache.filterKeys { p -> apps.any { a -> a.packageName == p } },
                    sizeCache = it.sizeCache.filterKeys { p -> apps.any { a -> a.packageName == p } },
                )
            }
            loadControl(context)
        }
    }

    companion object {
        fun defaultDir(): File = File(Environment.getExternalStorageDirectory(), "Strata/APKs")
    }
}