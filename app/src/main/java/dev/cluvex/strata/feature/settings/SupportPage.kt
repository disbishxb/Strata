package dev.cluvex.strata.feature.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cluvex.strata.R
import dev.cluvex.strata.core.donate.Donation
import dev.cluvex.strata.core.donate.Wallet
import dev.cluvex.strata.core.util.QrCode
import dev.cluvex.strata.ui.asPath
import dev.cluvex.strata.ui.expressive.ExpressiveDialog
import dev.cluvex.strata.ui.rememberTextClipboard
import kotlinx.coroutines.launch

@Composable
fun SupportCard(onOpen: () -> Unit) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("settings-support-card"),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingIcon(Icons.Rounded.VolunteerActivism, Tint.PRIMARY, size = 48)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.settings_support_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.settings_support_summary), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
fun SupportPage(onBack: () -> Unit) {
    val clipboard = rememberTextClipboard()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val copied = stringResource(R.string.settings_support_copied)
    val savedToGallery = stringResource(R.string.settings_support_saved_to_gallery)
    val saveFailed = stringResource(R.string.settings_support_save_failed)

    var qrWallet by remember { mutableStateOf<Wallet?>(null) }

    SettingsPageScaffold(stringResource(R.string.settings_support_title), onBack, snackbar = snackbar) {
        Text(
            stringResource(R.string.settings_support_body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp).testTag("support-body"),
        )

        // 第一组：原作者
        SettingsGroup(stringResource(R.string.settings_support_original_wallets), footer = stringResource(R.string.settings_support_network_hint)) {
            Donation.originalWallets.forEach { wallet ->
                custom(title = wallet.network, icon = Icons.Rounded.AccountBalanceWallet, tint = Tint.PRIMARY) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                wallet.address,
                                style = MaterialTheme.typography.bodyMedium.asPath(),
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("wallet-${wallet.id}"),
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(wallet.address)
                                scope.launch { snackbar.showSnackbar(copied) }
                            },
                            modifier = Modifier.testTag("copy-${wallet.id}"),
                        ) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = stringResource(R.string.settings_support_copy, wallet.network))
                        }
                    }
                }
            }
        }

        // 第二组：Fork 维护者
        SettingsGroup(stringResource(R.string.settings_support_fork_title)) {
            Donation.forkWallets.forEach { wallet ->
                custom(title = wallet.network, icon = Icons.Rounded.AccountBalanceWallet, tint = Tint.PRIMARY) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        wallet.description?.let { desc ->
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        } ?: Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { qrWallet = wallet },
                            modifier = Modifier.testTag("qr-${wallet.id}"),
                        ) {
                            Icon(Icons.Rounded.QrCode, contentDescription = stringResource(R.string.settings_support_show_qr, wallet.network))
                        }
                    }
                }
            }
        }
    }

    // 二维码弹窗
    qrWallet?.let { wallet ->
        val qrBitmap = remember(wallet.address, wallet.qrContent) {
            QrCode.generate(wallet.qrContent ?: wallet.address)
        }

        ExpressiveDialog(
            onDismissRequest = { qrWallet = null },
            title = wallet.network,
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(240.dp)
                        )
                    }
                    wallet.description?.let { desc ->
                        Spacer(Modifier.height(16.dp))
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { qrWallet = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    qrBitmap?.let { bmp ->
                        val success = QrCode.saveToGallery(context, bmp, "Strata_QR_${wallet.id}")
                        scope.launch {
                            snackbar.showSnackbar(if (success) savedToGallery else saveFailed)
                        }
                    }
                    qrWallet = null
                }) {
                    Text(stringResource(R.string.common_save))
                }
            }
        )
    }
}