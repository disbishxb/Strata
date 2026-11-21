package dev.cluvex.strata.feature.explorer.components

import androidx.compose.ui.res.vectorResource
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.decode.BitmapFactoryDecoder
import coil3.request.ImageRequest
import dev.cluvex.strata.R
import dev.cluvex.strata.core.fs.LocalFileAccess
import dev.cluvex.strata.core.model.FileItem
import dev.cluvex.strata.core.model.ListPrefs
import dev.cluvex.strata.core.root.RootManager
import dev.cluvex.strata.core.util.AppIconLoader
import dev.cluvex.strata.core.media.BundleIconLoader
import dev.cluvex.strata.core.util.FileKind
import dev.cluvex.strata.core.util.FileKinds
import dev.cluvex.strata.core.util.Formatters
import dev.cluvex.strata.feature.explorer.dnd.DropHalo
import dev.cluvex.strata.feature.explorer.dnd.DropLook
import dev.cluvex.strata.feature.explorer.dnd.dropHaloBehind
import dev.cluvex.strata.feature.explorer.dnd.fileGestures
import dev.cluvex.strata.feature.explorer.dnd.rememberDropAnimation
import dev.cluvex.strata.ui.LocalSettings
import dev.cluvex.strata.ui.LocalUiDensity
import dev.cluvex.strata.ui.isolated
import dev.cluvex.strata.ui.theme.KindPalette
import dev.cluvex.strata.ui.theme.toShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.io.File
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileRow(
    item: FileItem,
    selected: Boolean,
    flashKey: Int,
    showThumbnail: Boolean,
    hasApkInDir: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    flashPulses: Int = FLASH_PULSES,
    drop: DropLook = DropLook.NONE,
    onDragStart: ((Offset, PointerId) -> Boolean)? = null,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val density = LocalUiDensity.current
    val settings = LocalSettings.current
    val list = settings.list
    val iconSize = density.iconDp.dp
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, motion.fastSpatialSpec(), label = "press")
    val look = rememberDropAnimation(drop)
    val background by animateColorAsState(
        when {
            drop.hovered -> colors.primaryContainer.copy(alpha = 0.6f)
            selected -> colors.secondaryContainer
            else -> Color.Transparent
        },
        motion.defaultEffectsSpec(),
        label = "rowBg",
    )
    val ghostAlpha by animateFloatAsState(if (drop.ghosted) 0.36f else 1f, motion.defaultEffectsSpec(), label = "rowGhost")
    val flash = rememberFlash(flashKey, flashPulses)
    val flashColor = colors.primary.copy(alpha = 0.32f)

    Row(
        modifier
            .then(drop.spot)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ghostAlpha
            }
            .background(background, MaterialTheme.shapes.medium)
            .drawBehind {
                val a = flash.value
                if (a > 0f) drawRect(flashColor, alpha = a)
            }
            .fileGestures(interaction, onClick, onLongClick, onDragStart)
            .padding(horizontal = 6.dp, vertical = density.rowPadDp.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DropHalo(look, drop.anchor) {
            Leading(item, selected, showThumbnail, hasApkInDir, iconSize, if (settings.behavior.iconTapSelects) onIconClick else null)
        }
        Spacer(Modifier.width(if (density.iconDp <= 30) 8.dp else 10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = density.nameSp.sp,
                maxLines = if (list.wrapNames) 3 else density.nameMaxLines,
                overflow = TextOverflow.Ellipsis,
            )
            val date = if (list.showDate) Formatters.listDate(item.lastModified, list.dateStyle, list.use24Hour) else null
            val info = trailingInfo(item, list)
            if (date != null || info != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        date.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = density.metaSp.sp,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (info != null) {
                        Text(
                            info,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = density.metaSp.sp,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileTile(
    item: FileItem,
    selected: Boolean,
    selecting: Boolean,
    flashKey: Int,
    showThumbnail: Boolean,
    hasApkInDir: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    flashPulses: Int = FLASH_PULSES,
    drop: DropLook = DropLook.NONE,
    onDragStart: ((Offset, PointerId) -> Boolean)? = null,
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val settings = LocalSettings.current
    val list = settings.list
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, motion.fastSpatialSpec(), label = "press")
    val look = rememberDropAnimation(drop)
    val background by animateColorAsState(
        when {
            drop.hovered -> colors.primaryContainer
            selected -> colors.secondaryContainer
            else -> colors.surfaceContainerLow
        },
        motion.defaultEffectsSpec(),
        label = "tileBg",
    )
    val ghostAlpha by animateFloatAsState(if (drop.ghosted) 0.36f else 1f, motion.defaultEffectsSpec(), label = "tileGhost")
    val flash = rememberFlash(flashKey, flashPulses)
    val flashColor = colors.primary.copy(alpha = 0.32f)
    val shape = settings.appearance.iconShape.toShape()
    val kind = FileKinds.of(item)
    
    Column(
        modifier
            .then(drop.spot)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = ghostAlpha
            }
            .background(background, MaterialTheme.shapes.large)
            .drawBehind {
                val a = flash.value
                if (a > 0f) drawRect(flashColor, alpha = a)
            }
            .fileGestures(interaction, onClick, onLongClick, onDragStart)
            .padding(6.dp)
            .testTag("file-tile"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1f).then(drop.anchor).dropHaloBehind(look, reach = 0.72f), contentAlignment = Alignment.Center) {
            val picture = showThumbnail && list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO)
            val apk = showThumbnail && list.apkIcons &&
    (kind == FileKind.APK || item.name.endsWith(".apk", ignoreCase = true) || item.name.endsWith(".apk.1", ignoreCase = true))
            val bundle = showThumbnail && list.apkIcons && item.extension in setOf("apks", "xapk", "apkm")
            
            if (list.apkIcons && item.isDirectory) {
                AppIconWithFolder(item, 56.dp, shape)
            } else {
                when {
                    picture -> {
                        KindIcon(item, 48.dp, shape)
                        AsyncImage(
                            model = thumbnailModel(item),
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
                        )
                    }
                    apk -> if (hasApkInDir) ApkIconOrKind(item, 56.dp, shape) else KindIcon(item, 56.dp, shape)
                    bundle -> BundleIconWithFolder(item, 56.dp, shape)
                    else -> KindIcon(item, 56.dp, shape)
                }
            }

            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(colors.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.explorer_selected), tint = colors.onPrimary, modifier = Modifier.size(14.dp))
                    }
                } else if (selecting) {
                    Box(Modifier.fillMaxSize().clip(CircleShape).border(2.dp, colors.outline, CircleShape).background(colors.surface.copy(alpha = 0.6f)))
                }
            }
        }
        Text(
            isolated(Formatters.displayName(item.name, item.isDirectory, list.showExtensions)),
            style = MaterialTheme.typography.labelMedium,
            maxLines = if (list.wrapNames) 3 else 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        val info = trailingInfo(item, list) ?: if (list.showDate) Formatters.listDate(item.lastModified, list.dateStyle, list.use24Hour) else null
        if (info != null) {
            Text(info, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AppIconWithFolder(item: FileItem, size: Dp, shape: Shape) {
    val context = LocalContext.current
    var appIcon by remember(item.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    LaunchedEffect(item.path) {
        withContext(Dispatchers.IO) {
            var tempBmp: Bitmap? = AppIconLoader.loadIcon(context, item.path)
            
            val isSystemApp = item.path.startsWith("/system/app/") || 
                              item.path.startsWith("/system/priv-app/") ||
                              item.path.startsWith("/system_ext/app/") ||
                              item.path.startsWith("/product/app/") ||
                              item.path.startsWith("/vendor/app/") ||
                              item.path.startsWith("/data/app/") ||
                              item.path.startsWith("/data/app-staging/")

            if (tempBmp == null && isSystemApp) {
                try {
                    val access = item.access
                    val dir = access.file(item.path)
                    var targetApk: File? = null
                    
                    if (item.name.startsWith("~~")) {
                        dir.listFiles()?.firstOrNull()?.let { subDir ->
                            val subApk = access.child(subDir, "base.apk")
                            if (subApk.exists()) targetApk = subApk
                        }
                    }
                    
                    if (targetApk == null) {
                        val files = dir.listFiles()
                        targetApk = files?.firstOrNull { it.name == "base.apk" } 
                                 ?: files?.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                    }
                    
                    if (targetApk == null) {
                        dir.listFiles()?.firstOrNull { it.isDirectory }?.let { archDir ->
                            val archFiles = access.file(archDir.path).listFiles()
                            targetApk = archFiles?.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                        }
                    }
                    
                    targetApk?.let { apk ->
                        val cacheDir = File(context.cacheDir, "tmp_app_icons").apply { mkdirs() }
                        val localFile = access.materialize(apk, cacheDir)
                        if (localFile.exists() && localFile.length() > 0) {
                            tempBmp = AppIconLoader.loadIcon(context, localFile.absolutePath)
                        }
                        if (localFile.absolutePath != apk.absolutePath) {
                            localFile.delete()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            val finalBmp = tempBmp
            if (finalBmp != null) {
                appIcon = finalBmp.asImageBitmap()
            }
        }
    }

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        KindIcon(item, size, shape)
        
        appIcon?.let { icon ->
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(size * 0.47f)
            )
        }
    }
}

@Composable
private fun BundleIconWithFolder(item: FileItem, size: Dp, shape: Shape) {
    val context = LocalContext.current
    var appIcon by remember(item.path, item.lastModified) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(item.path, item.lastModified) {
        withContext(Dispatchers.IO) {
            val bmp = BundleIconLoader.load(context, item.path, item.lastModified)
            if (bmp != null) appIcon = bmp.asImageBitmap()
        }
    }

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        KindIcon(item, size, shape)
        appIcon?.let { icon ->
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(size * 0.47f)
            )
        }
    }
}

@Composable
private fun rememberFlash(flashKey: Int, pulses: Int): Animatable<Float, *> {
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashKey) {
        if (flashKey == 0) {
            flash.snapTo(0f)
            return@LaunchedEffect
        }
        repeat(pulses) {
            flash.animateTo(1f, tween(FLASH_IN_MS, easing = FastOutSlowInEasing))
            flash.animateTo(0.25f, tween(FLASH_OUT_MS, easing = FastOutSlowInEasing))
        }
        flash.animateTo(0f, tween(FLASH_FADE_MS))
    }
    return flash
}

const val FLASH_PULSES = 2
private const val FLASH_IN_MS = 200
private const val FLASH_OUT_MS = 280
private const val FLASH_FADE_MS = 520

fun flashDurationMs(pulses: Int): Long = (pulses * (FLASH_IN_MS + FLASH_OUT_MS) + FLASH_FADE_MS).toLong()

@Composable
private fun trailingInfo(item: FileItem, list: ListPrefs): String? = when {
    item.isSymlink -> stringResource(R.string.explorer_link)
    item.isDirectory -> null
    !list.showSize -> null
    else -> Formatters.size(item.size, list.sizeUnits)
}

@Composable
private fun thumbnailModel(item: FileItem): Any {
    val context = LocalContext.current
    return remember(item.path) {
        if (item.extension == "gif" || item.extension == "webp") {
            ImageRequest.Builder(context).data(item.path.toPath()).decoderFactory(BitmapFactoryDecoder.Factory()).build()
        } else {
            item.path.toPath()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Leading(item: FileItem, selected: Boolean, showThumbnail: Boolean, hasApkInDir: Boolean, iconSize: Dp, onIconClick: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    val settings = LocalSettings.current
    val shape = settings.appearance.iconShape.toShape()
    
    val kind = FileKinds.of(item)
    val isApk = item.name.endsWith(".apk", ignoreCase = true) ||
            item.name.endsWith(".apk.1", ignoreCase = true) ||
            kind == FileKind.APK
    val isBundle = item.extension in setOf("apks", "xapk", "apkm")

    Box(
        Modifier
            .size(iconSize)
            .clip(shape)
            .then(if (onIconClick != null) Modifier.clickable(onClickLabel = stringResource(R.string.common_select)) { onIconClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier.size(iconSize).clip(shape).background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.explorer_selected), tint = colors.onPrimary)
            }
        } else if (settings.list.apkIcons && item.isDirectory) {
            AppIconWithFolder(item, iconSize, shape)
        } else {
            when {
                isApk && hasApkInDir -> ApkIconOrKind(item, iconSize, shape)
                isApk -> KindIcon(item, iconSize, shape)
                isBundle && settings.list.apkIcons -> BundleIconWithFolder(item, iconSize, shape)
                showThumbnail && settings.list.thumbnails && (kind == FileKind.IMAGE || kind == FileKind.VIDEO) -> Box(
                    Modifier.size(iconSize),
                    contentAlignment = Alignment.Center,
                ) {
                    KindIcon(item, iconSize, shape)
                    AsyncImage(
                        model = thumbnailModel(item),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(iconSize).clip(shape),
                    )
                }
                else -> KindIcon(item, iconSize, shape)
            }
        }
    }
}

@Composable
private fun ApkIconOrKind(item: FileItem, size: Dp, shape: Shape) {
    val context = LocalContext.current
    var bitmap by remember(item.path) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    
    LaunchedEffect(item.path, item.lastModified) {
        withContext(Dispatchers.IO) {
            val bmp = AppIconLoader.loadIcon(context, item.path)
            if (bmp != null) {
                bitmap = bmp.asImageBitmap()
            }
        }
    }

    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!,
                contentDescription = stringResource(R.string.explorer_app_icon),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(size).testTag("apk-icon"),
            )
        } else {
            KindIcon(item, size, shape)
        }
    }
}

@Composable
private fun KindIcon(item: FileItem, iconSize: Dp, shape: Shape) {
    val colors = MaterialTheme.colorScheme
    val settings = LocalSettings.current
    val isDark = colors.background.luminance() < 0.5f

    val grammar = dev.cluvex.strata.core.text.StgxRegistry.entryForExtension(item.extension)?.grammar
    // 原生 .py 没有 stgx 时用 "python" 兜底；.stgx 显式写了 icon 则用它的
    val iconSpec = grammar?.icon ?: if (item.extension.equals("py", true)) "python" else null

    // A. drawable 分支：.py / .stgx "python"，保持 tint = onPrimaryContainer、无背景 Box
    val drawableRes = dev.cluvex.strata.core.text.StgxIcons.drawableOf(iconSpec)
    if (drawableRes != null) {
        Icon(
            imageVector = androidx.compose.ui.graphics.vector.ImageVector.vectorResource(drawableRes),
            contentDescription = item.name,
            modifier = Modifier.size(iconSize).clip(shape),
            tint = colors.onPrimaryContainer,
        )
        return
    }

    // B. .stgx 自定义分支（bitmap / vector + KindPalette 背景）
    val stgxBitmap = dev.cluvex.strata.core.text.StgxIcons.bitmapOfExtension(item.extension)
    var stgxIcon = if (stgxBitmap == null) dev.cluvex.strata.core.text.StgxIcons.vectorOf(iconSpec) else null

    // 特殊处理：如果 stgx 里的 icon 字段写的是 archive，直接替换为系统的 FolderZip 图标
    if (grammar?.icon == "archive") {
        stgxIcon = Icons.Rounded.FolderZip
    }

    if (stgxBitmap != null || stgxIcon != null) {
        val stgxColors = dev.cluvex.strata.core.text.StgxRegistry.colorsOfExtension(item.extension)
        val customBg = if (isDark) stgxColors.second ?: stgxColors.first else stgxColors.first

        var kind = FileKinds.of(item)
        kind = when (grammar?.icon) {
            "zip", "archive", "folder_zip" -> FileKind.ARCHIVE
            "code" -> FileKind.CODE
            "terminal", "shell" -> FileKind.SHELL
            "data_object", "json" -> FileKind.JSON
            "description", "markdown", "text" -> FileKind.TEXT
            "image" -> FileKind.IMAGE
            "video" -> FileKind.VIDEO
            "audio" -> FileKind.AUDIO
            "pdf" -> FileKind.PDF
            "font" -> FileKind.FONT
            "apk" -> FileKind.APK
            else -> kind
        }

        val tintKind = dev.cluvex.strata.core.text.StgxRegistry.tintOfExtension(item.extension)
        val stgxHue = dev.cluvex.strata.core.text.StgxRegistry.hueOfExtension(item.extension)
        val defaultTile = when {
            tintKind != null -> KindPalette.colors(tintKind, settings.appearance.colorfulIcons, colors, isDark)
            stgxHue != null -> KindPalette.colorsForHue(stgxHue.toDouble(), settings.appearance.colorfulIcons, colors, isDark)
            else -> KindPalette.colors(kind, settings.appearance.colorfulIcons, colors, isDark)
        }
        val containerColor = customBg ?: defaultTile.container
        val contentColor = stgxColors.third ?: defaultTile.content

        Box(Modifier.size(iconSize).clip(shape).background(containerColor), contentAlignment = Alignment.Center) {
            if (stgxBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = stgxBitmap,
                    contentDescription = item.name,
                    modifier = Modifier.size(iconSize * 0.66f),
                )
            } else {
                Icon(stgxIcon!!, contentDescription = item.name, tint = contentColor, modifier = Modifier.padding(iconSize * 0.22f))
            }
        }
        return
    }

    // C. 原生分支
    val kind = FileKinds.of(item)
    val tile = KindPalette.colors(kind, settings.appearance.colorfulIcons, colors, isDark)
    val custom = FileKinds.getCustomIcon(item.extension)
    Box(
        Modifier
            .size(iconSize)
            .clip(shape)
            .background(tile.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            custom ?: kind.icon,
            contentDescription = stringResource(kind.label),
            tint = tile.content,
            modifier = Modifier.padding(iconSize * 0.22f),
        )
    }
}