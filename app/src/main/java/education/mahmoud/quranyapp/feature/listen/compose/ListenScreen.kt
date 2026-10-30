package education.mahmoud.quranyapp.feature.listen.compose

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.gotoscreen.GoToInput
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranFont
import education.mahmoud.quranyapp.feature.listen.ListenInput
import education.mahmoud.quranyapp.feature.listen.ListenPhase
import education.mahmoud.quranyapp.feature.listen.ListenViewModel
import education.mahmoud.quranyapp.feature.listen.Reciter
import education.mahmoud.quranyapp.feature.listen.ReciterDownloads
import education.mahmoud.quranyapp.feature.listen.SessionMode
import education.mahmoud.quranyapp.feature.setting.compose.SectionHeader
import education.mahmoud.quranyapp.feature.setting.compose.SettingsCard
import education.mahmoud.quranyapp.utils.NumberHelper

private const val SURA_WEIGHT = 1.6f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListenScreen(viewModel: ListenViewModel, onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val message = viewModel.message
    val scrollState = rememberScrollState()
    val downloading = viewModel.phase is ListenPhase.Downloading
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.start() }
    val onPlay: () -> Unit = {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.start()
        }
    }
    LaunchedEffect(downloading) {
        if (downloading) scrollState.animateScrollTo(0)
    }
    LaunchedEffect(message) {
        if (message != null) {
            viewModel.consumeMessage()
            snackbar.showSnackbar(context.getString(message))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.listen)) },
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
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            when (val phase = viewModel.phase) {
                is ListenPhase.Playing -> PlayerCard(phase, viewModel)
                else -> {
                    if (phase is ListenPhase.Downloading) {
                        Spacer(Modifier.height(12.dp))
                        DownloadCard(phase, onCancel = viewModel::stop)
                    }
                    SelectionForm(viewModel, enabled = phase !is ListenPhase.Downloading)
                    Spacer(Modifier.height(20.dp))
                    if (phase !is ListenPhase.Downloading) {
                        Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Text(stringResource(R.string.listen_play), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    DownloadsSection(viewModel.downloads, enabled = phase !is ListenPhase.Downloading, onDelete = viewModel::deleteDownloads)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SelectionForm(viewModel: ListenViewModel, enabled: Boolean) {
    val form = viewModel.form
    val (_, errors) = viewModel.validation()
    val startMax = viewModel.ayahCount(form.startSura)
    val endMax = viewModel.ayahCount(form.endSura)

    SectionHeader(R.string.listen_reciter)
    SettingsCard {
        Column(Modifier.padding(16.dp)) {
            Text("اختر القراء", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            // Compose foundation 1.4.x FlowRow has no verticalArrangement parameter,
            // so the row spacing comes from the chips' vertical padding instead.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Reciter.values().forEach { reciter ->
                    FilterChip(
                        selected = form.selectedReciters.contains(reciter),
                        onClick = { if (enabled) viewModel.toggleReciter(reciter) },
                        label = { Text(stringResource(reciter.nameRes)) },
                        enabled = enabled,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
            }
            if (errors.noReciters) {
                Text("يجب اختيار قارئ واحد على الأقل", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    Text(
        "نظام الجلسة",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
    )
    SettingsCard {
        Column(Modifier.padding(16.dp)) {
            SessionMode.values().forEach { mode ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = form.sessionMode == mode,
                        onClick = { if (enabled) viewModel.updateForm(form.copy(sessionMode = mode)) },
                        enabled = enabled,
                    )
                    Text(
                        when (mode) {
                            SessionMode.CHUNK -> "كل شيخ يقرأ عدد من الآيات"
                            SessionMode.FULL -> "كل شيخ يقرأ النطاق كاملاً"
                            SessionMode.PER_AYAH -> "كل الشيوخ يقرأون نفس الآية"
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (form.sessionMode == SessionMode.CHUNK) {
                Spacer(Modifier.height(8.dp))
                NumberInput(
                    value = form.chunkSize,
                    onValue = { viewModel.updateForm(form.copy(chunkSize = it)) },
                    label = "عدد الآيات لكل شيخ",
                    hint = "1 - 50",
                    isError = errors.chunkSize,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    SectionHeader(R.string.listen_from)
    SettingsCard {
        SuraAyahRow(
            viewModel = viewModel,
            sura = form.startSura,
            onSura = { viewModel.updateForm(form.copy(startSura = it, startAyah = "")) },
            ayah = form.startAyah,
            onAyah = { viewModel.updateForm(form.copy(startAyah = it)) },
            ayahLabel = stringResource(R.string.listen_start_ayah),
            hint = stringResource(R.string.goto_range, 1, startMax),
            error = errors.startAyah,
            enabled = enabled,
        )
    }

    SectionHeader(R.string.listen_to)
    SettingsCard {
        SuraAyahRow(
            viewModel = viewModel,
            sura = form.endSura,
            onSura = { viewModel.updateForm(form.copy(endSura = it, endAyah = "")) },
            ayah = form.endAyah,
            onAyah = { viewModel.updateForm(form.copy(endAyah = it)) },
            ayahLabel = stringResource(R.string.listen_end_ayah),
            hint = stringResource(R.string.goto_range, 1, endMax),
            error = errors.endAyah,
            enabled = enabled,
        )
    }
    Text(
        stringResource(R.string.listen_empty_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
    )
    if (errors.order) {
        Text(
            stringResource(R.string.listen_order_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp),
        )
    }

    SectionHeader(R.string.listen_repeat)
    SettingsCard {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInput(
                value = form.repeatEach,
                onValue = { viewModel.updateForm(form.copy(repeatEach = it)) },
                label = stringResource(R.string.listen_repeat_each),
                hint = stringResource(R.string.goto_range, 1, ListenInput.MAX_REPEAT),
                isError = errors.repeatEach,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            NumberInput(
                value = form.repeatAll,
                onValue = { viewModel.updateForm(form.copy(repeatAll = it)) },
                label = stringResource(R.string.listen_repeat_all),
                hint = stringResource(R.string.goto_range, 1, ListenInput.MAX_REPEAT),
                isError = errors.repeatAll,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlayerCard(phase: ListenPhase.Playing, viewModel: ListenViewModel) {
    SettingsCard {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(phase.currentReciter.nameRes),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${phase.suraName} · ${phase.ayah.ayahInSurahIndex}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                phase.ayah.text,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = QuranFont,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::previous) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = null)
                }
                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        if (phase.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = viewModel::next) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null)
                }
            }
            OutlinedButton(onClick = viewModel::stop) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Text("إيقاف", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuraAyahRow(
    viewModel: ListenViewModel,
    sura: Int,
    onSura: (Int) -> Unit,
    ayah: String,
    onAyah: (String) -> Unit,
    ayahLabel: String,
    hint: String,
    error: Boolean,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val suras = viewModel.suras
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it },
            modifier = Modifier.weight(SURA_WEIGHT),
        ) {
            OutlinedTextField(
                value = suras.firstOrNull { it.index == sura }?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                label = { Text(stringResource(R.string.listen_sura)) },
                supportingText = { Text(" ") },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                suras.forEach { item ->
                    DropdownMenuItem(
                        text = { Text("${item.index}   ${item.name}") },
                        onClick = {
                            onSura(item.index)
                            expanded = false
                        },
                    )
                }
            }
        }
        NumberInput(ayah, onAyah, ayahLabel, hint, error, enabled, Modifier.weight(1f))
    }
}

@Composable
private fun NumberInput(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    hint: String,
    isError: Boolean,
    enabled: Boolean,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValue(GoToInput.sanitize(it)) },
        label = { Text(label) },
        supportingText = { Text(hint) },
        isError = isError,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
internal fun DownloadsSection(
    downloads: List<ReciterDownloads>,
    enabled: Boolean,
    onDelete: (Reciter?) -> Unit,
) {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<Pair<Boolean, Reciter?>?>(null) }
    fun size(bytes: Long) = android.text.format.Formatter.formatShortFileSize(context, bytes)

    SectionHeader(R.string.downloads_title)
    SettingsCard {
        Column(Modifier.padding(vertical = 8.dp)) {
            if (downloads.isEmpty()) {
                Text(
                    stringResource(R.string.downloads_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            downloads.forEachIndexed { index, item ->
                if (index > 0) Divider(Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(item.reciter.nameRes), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.downloads_summary, item.files, size(item.bytes)),
                            style = numberStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { pending = false to item.reciter }, enabled = enabled) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.downloads_delete),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            if (downloads.isNotEmpty()) {
                Divider(Modifier.padding(horizontal = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 4.dp),
                ) {
                    Text(
                        stringResource(R.string.downloads_total, size(downloads.sumOf { it.bytes })),
                        style = numberStyle(),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { pending = true to null }, enabled = enabled) {
                        Text(stringResource(R.string.downloads_delete_all), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    pending?.let { (all, reciter) ->
        AlertDialog(
            onDismissRequest = { pending = null },
            text = {
                Text(
                    if (all || reciter == null) {
                        stringResource(R.string.downloads_delete_all_confirm)
                    } else {
                        stringResource(R.string.downloads_delete_confirm, stringResource(reciter.nameRes))
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    pending = null
                    onDelete(if (all) null else reciter)
                }) { Text(stringResource(R.string.downloads_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text(stringResource(R.string.cancel_action)) }
            },
        )
    }
}

@Composable
private fun DownloadCard(phase: ListenPhase.Downloading, onCancel: () -> Unit) {
    SettingsCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.listen_downloading, phase.done, phase.total),
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Default,
            )
            val fraction = if (phase.total == 0) 0f else phase.done / phase.total.toFloat()
            LinearProgressIndicator(progress = fraction, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    }
}

@Composable
private fun numberStyle() = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Default)
