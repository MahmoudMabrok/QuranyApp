package education.mahmoud.quranyapp.feature.listen.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.gotoscreen.GoToInput
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranFont
import education.mahmoud.quranyapp.feature.listen.ListenInput
import education.mahmoud.quranyapp.feature.listen.ListenPhase
import education.mahmoud.quranyapp.feature.listen.ListenViewModel
import education.mahmoud.quranyapp.feature.listen.Reciter
import education.mahmoud.quranyapp.feature.setting.compose.SectionHeader
import education.mahmoud.quranyapp.feature.setting.compose.SettingsCard
import education.mahmoud.quranyapp.utils.NumberHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListenScreen(viewModel: ListenViewModel, onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val message = viewModel.message
    val scrollState = rememberScrollState()
    val downloading = viewModel.phase is ListenPhase.Downloading
    LaunchedEffect(downloading) {
        // bring the progress card (shown above the form) into view
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
                        Button(onClick = viewModel::start, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Text(stringResource(R.string.listen_play), modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectionForm(viewModel: ListenViewModel, enabled: Boolean) {
    val form = viewModel.form
    val (_, errors) = viewModel.validation()
    val startMax = viewModel.ayahCount(form.startSura)
    val endMax = viewModel.ayahCount(form.endSura)

    SectionHeader(R.string.listen_reciter)
    SettingsCard {
        ReciterPicker(viewModel.reciter, enabled, viewModel::selectReciter)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReciterPicker(selected: Reciter, enabled: Boolean, onSelect: (Reciter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = Modifier.padding(16.dp),
    ) {
        OutlinedTextField(
            value = stringResource(selected.nameRes),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Reciter.values().forEach { reciter ->
                DropdownMenuItem(
                    text = { Text(stringResource(reciter.nameRes)) },
                    onClick = {
                        onSelect(reciter)
                        expanded = false
                    },
                )
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
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.cancel_action))
            }
        }
    }
}

@Composable
private fun PlayerCard(phase: ListenPhase.Playing, viewModel: ListenViewModel) {
    SectionHeader(R.string.listen_now_playing)
    SettingsCard {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${phase.suraName} · ${stringResource(R.string.listen_ayah_n, phase.ayah.ayahInSurahIndex)}",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(viewModel.reciter.nameRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                NumberHelper.formatAyahWithNumber(phase.ayah.text, phase.ayah.ayahInSurahIndex),
                fontFamily = QuranFont,
                fontSize = 24.sp,
                lineHeight = 44.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            )
            Slider(
                value = phase.progressMs.toFloat(),
                onValueChange = { viewModel.seekTo(it.toInt()) },
                valueRange = 0f..phase.durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(phase.progressMs), style = numberStyle())
                Text(stringResource(R.string.listen_position, phase.position + 1, phase.total), style = numberStyle())
                Text(formatTime(phase.durationMs), style = numberStyle())
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // in RTL "previous" sits on the right, matching the reading direction
                IconButton(onClick = viewModel::previous, enabled = phase.position > 0) {
                    Icon(Icons.Filled.SkipNext, contentDescription = stringResource(R.string.listen_previous))
                }
                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(
                        if (phase.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = stringResource(if (phase.isPlaying) R.string.listen_pause else R.string.listen_play),
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(onClick = viewModel::next, enabled = phase.position + 1 < phase.total) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = stringResource(R.string.listen_next))
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = viewModel::stop) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Text(stringResource(R.string.listen_stop), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

private const val SURA_WEIGHT = 1.8f

/** The Quran font maps Arabic-Indic digits to ayah-marker glyphs, so numbers use the default font. */
@Composable
private fun numberStyle() = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Default)

private fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    // isolate as LTR so "m:ss" is not reordered inside the RTL layout
    return "\u2066" + NumberHelper.getArabicNumber(totalSeconds / 60) + ":" +
        NumberHelper.getArabicNumber(totalSeconds % 60).padStart(2, '٠') + "\u2069"
}
