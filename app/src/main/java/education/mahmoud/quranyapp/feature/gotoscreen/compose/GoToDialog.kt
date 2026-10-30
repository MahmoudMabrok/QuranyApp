package education.mahmoud.quranyapp.feature.gotoscreen.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.gotoscreen.GoToInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Callbacks for the three kinds of jump the dialog supports. */
data class GoToActions(
    val onSuraAyah: (sura: Int, ayah: Int) -> Unit,
    val onJuz: (Int) -> Unit,
    val onPage: (Int) -> Unit,
)

private enum class GoToMode { SuraAyah, Juz, Page }

/**
 * Quick-jump dialog (sura/ayah, juz or page) built with Material 3 so it follows
 * [education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme].
 * Inputs accept digits only, show their valid range, and are validated before jumping.
 */
@Composable
fun GoToDialog(
    repository: QuranRepository,
    onDismiss: () -> Unit,
    actions: GoToActions,
) {
    val suras by produceState<List<SuraItem>>(initialValue = emptyList(), repository) {
        value = withContext(Dispatchers.IO) { repository.suras }
    }

    var mode by rememberSaveable { mutableStateOf(GoToMode.SuraAyah) }
    var suraText by rememberSaveable { mutableStateOf("") }
    var ayahText by rememberSaveable { mutableStateOf("") }
    var juzText by rememberSaveable { mutableStateOf("") }
    var pageText by rememberSaveable { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val suraResult = GoToInput.parse(suraText, 1, GoToInput.SURA_COUNT)
    val selectedSura = (suraResult as? GoToInput.Result.Valid)?.let { valid ->
        suras.firstOrNull { it.index == valid.value }
    }
    val ayahMax = selectedSura?.numOfAyahs ?: MAX_AYAHS_IN_SURA
    val ayahResult = GoToInput.parse(ayahText, 1, ayahMax)
    val juzResult = GoToInput.parse(juzText, 1, GoToInput.JUZ_COUNT)
    val pageResult = GoToInput.parse(pageText, 1, GoToInput.PAGE_COUNT)

    val submit: () -> Unit = {
        showErrors = true
        when (mode) {
            GoToMode.SuraAyah -> {
                val sura = (suraResult as? GoToInput.Result.Valid)?.value
                val ayah = when (ayahResult) {
                    GoToInput.Result.Empty -> 1
                    is GoToInput.Result.Valid -> ayahResult.value
                    is GoToInput.Result.OutOfRange -> null
                }
                if (sura != null && ayah != null) actions.onSuraAyah(sura, ayah)
            }
            GoToMode.Juz -> (juzResult as? GoToInput.Result.Valid)?.let { actions.onJuz(it.value) }
            GoToMode.Page -> (pageResult as? GoToInput.Result.Valid)?.let { actions.onPage(it.value) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.jump), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TabRow(
                    selectedTabIndex = mode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    GoToMode.values().forEach { item ->
                        Tab(
                            selected = mode == item,
                            onClick = {
                                mode = item
                                showErrors = false
                            },
                            text = { Text(stringResource(item.titleRes())) },
                        )
                    }
                }
                when (mode) {
                    GoToMode.SuraAyah -> {
                        val suraSupport = selectedSura?.let {
                            stringResource(R.string.goto_sura_info, it.name.orEmpty(), it.numOfAyahs)
                        }
                        NumberField(
                            value = suraText,
                            onValueChange = { suraText = it },
                            label = stringResource(R.string.goto_sura_label),
                            result = suraResult,
                            min = 1,
                            max = GoToInput.SURA_COUNT,
                            required = true,
                            showErrors = showErrors,
                            supportOverride = suraSupport,
                            imeAction = ImeAction.Next,
                            onIme = submit,
                            autoFocus = true,
                        )
                        NumberField(
                            value = ayahText,
                            onValueChange = { ayahText = it },
                            label = stringResource(R.string.goto_ayah_label),
                            result = ayahResult,
                            min = 1,
                            max = ayahMax,
                            required = false,
                            showErrors = showErrors,
                            imeAction = ImeAction.Go,
                            onIme = submit,
                        )
                    }
                    GoToMode.Juz -> NumberField(
                        value = juzText,
                        onValueChange = { juzText = it },
                        label = stringResource(R.string.goto_juz_label),
                        result = juzResult,
                        min = 1,
                        max = GoToInput.JUZ_COUNT,
                        required = true,
                        showErrors = showErrors,
                        imeAction = ImeAction.Go,
                        onIme = submit,
                        autoFocus = true,
                    )
                    GoToMode.Page -> NumberField(
                        value = pageText,
                        onValueChange = { pageText = it },
                        label = stringResource(R.string.goto_page_label),
                        result = pageResult,
                        min = 1,
                        max = GoToInput.PAGE_COUNT,
                        required = true,
                        showErrors = showErrors,
                        imeAction = ImeAction.Go,
                        onIme = submit,
                        autoFocus = true,
                    )
                }
            }
        },
        confirmButton = { Button(onClick = submit) { Text(stringResource(R.string.go)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) } },
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    result: GoToInput.Result,
    min: Int,
    max: Int,
    required: Boolean,
    showErrors: Boolean,
    imeAction: ImeAction,
    onIme: () -> Unit,
    supportOverride: String? = null,
    autoFocus: Boolean = false,
) {
    val isError = result is GoToInput.Result.OutOfRange ||
        (required && showErrors && result is GoToInput.Result.Empty)
    val support = when {
        isError -> stringResource(R.string.goto_error_range, min, max)
        supportOverride != null -> supportOverride
        else -> stringResource(R.string.goto_range, min, max)
    }
    val focusRequester = remember { FocusRequester() }
    if (autoFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(GoToInput.sanitize(it)) },
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = { Text(support) },
        textStyle = TextStyle(textAlign = TextAlign.Start, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        keyboardActions = if (imeAction == ImeAction.Go) KeyboardActions(onGo = { onIme() }) else KeyboardActions.Default,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .focusRequester(focusRequester),
    )
}

private fun GoToMode.titleRes(): Int = when (this) {
    GoToMode.SuraAyah -> R.string.goto_tab_sura
    GoToMode.Juz -> R.string.goto_tab_juz
    GoToMode.Page -> R.string.goto_tab_page
}

private const val MAX_AYAHS_IN_SURA = 286
