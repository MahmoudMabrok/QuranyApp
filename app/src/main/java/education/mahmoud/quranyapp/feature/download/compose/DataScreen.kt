package education.mahmoud.quranyapp.feature.download.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.home_Activity.compose.SafeInsets
import education.mahmoud.quranyapp.feature.home_Activity.compose.paddingExceptBottom
import education.mahmoud.quranyapp.feature.download.DataViewModel
import education.mahmoud.quranyapp.feature.listen.compose.DownloadsSection
import education.mahmoud.quranyapp.feature.setting.compose.SectionHeader
import education.mahmoud.quranyapp.feature.setting.compose.SettingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(viewModel: DataViewModel, onBack: () -> Unit, onOpenListen: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val message = viewModel.message
    LaunchedEffect(message) {
        if (message != null) {
            viewModel.consumeMessage()
            snackbar.showSnackbar(context.getString(message))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = SafeInsets.content,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                windowInsets = SafeInsets.topBar,
                title = { Text(stringResource(R.string.download).trim()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        val mirror = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            modifier = Modifier.scale(scaleX = mirror, scaleY = 1f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paddingExceptBottom(padding)
                .verticalScroll(rememberScrollState())
                // bottom inset inside the scroll: content scrolls behind the nav bar but ends above it
                .padding(bottom = padding.calculateBottomPadding())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SectionHeader(R.string.data_offline)
            SettingsCard {
                Column(Modifier.padding(vertical = 4.dp)) {
                    StatusRow(R.string.data_quran_text, viewModel.ayahs)
                    Divider(Modifier.padding(horizontal = 16.dp))
                    StatusRow(R.string.data_tafseer, viewModel.tafseer)
                }
            }
            Text(
                stringResource(R.string.data_offline_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp),
            )

            DownloadsSection(viewModel.downloads, enabled = true, onDelete = viewModel::deleteDownloads)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onOpenListen, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Filled.Headphones, contentDescription = null)
                Text(stringResource(R.string.data_download_recitations), modifier = Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusRow(@StringRes title: Int, count: Int?) {
    val complete = count != null && count >= DataViewModel.TOTAL_AYAHS
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(
                if (count == null) "…" else stringResource(R.string.data_ayah_count, count, DataViewModel.TOTAL_AYAHS),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Default),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (count != null) {
            Icon(
                if (complete) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = stringResource(if (complete) R.string.data_complete else R.string.data_incomplete),
                tint = if (complete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}
