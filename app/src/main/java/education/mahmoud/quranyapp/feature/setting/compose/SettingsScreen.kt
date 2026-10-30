package education.mahmoud.quranyapp.feature.setting.compose

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranFont
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.NumberHelper

private data class BackgroundOption(val id: Int, @StringRes val label: Int, @ColorRes val color: Int)

private val backgroundOptions = listOf(
    BackgroundOption(Constants.WHITE, R.string.white, R.color.bg_white),
    BackgroundOption(Constants.YELLOW, R.string.yellow, R.color.bg_yellow),
    BackgroundOption(Constants.GREEN, R.string.green, R.color.bg_green),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nightMode: Boolean,
    backgroundColor: Int,
    onNightModeChange: (Boolean) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // ArrowBack is not auto-mirrored in this Compose version
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SectionHeader(R.string.settings_section_reading)
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = nightMode, role = Role.Switch, onValueChange = onNightModeChange)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(stringResource(R.string.night_mode), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.night_mode_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = nightMode, onCheckedChange = null)
                }
            }

            SectionHeader(R.string.color_for_background_for_regular_mode)
            SettingsCard {
                Column(Modifier.alpha(if (nightMode) DISABLED_ALPHA else 1f).padding(vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().selectableGroup().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        backgroundOptions.forEach { option ->
                            ColorOption(
                                option = option,
                                selected = option.id == backgroundColor,
                                enabled = !nightMode,
                                onClick = { onBackgroundColorChange(option.id) },
                            )
                        }
                    }
                    if (nightMode) {
                        Text(
                            stringResource(R.string.background_disabled_in_night_mode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp),
                        )
                    }
                }
            }

            SectionHeader(R.string.settings_section_preview)
            ReadingPreview(nightMode = nightMode, backgroundColor = backgroundColor)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) { content() }
}

@Composable
private fun RowScope.ColorOption(
    option: BackgroundOption,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                // some reading colours are translucent; show them over white like the reader page
                .background(Color.White, CircleShape)
                .background(colorResource(option.color), CircleShape)
                .border(
                    if (selected) BorderStroke(3.dp, primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = primary)
        }
        Text(
            stringResource(option.label),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ReadingPreview(nightMode: Boolean, backgroundColor: Int) {
    val background = if (nightMode) {
        colorResource(R.color.bg_ays_night_mode)
    } else {
        colorResource(backgroundOptions.firstOrNull { it.id == backgroundColor }?.color ?: R.color.bg_white)
    }
    val textColor = colorResource(if (nightMode) R.color.ayas_color_night_mode else R.color.ayas_color)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = NumberHelper.formatAyahWithNumber(stringResource(R.string.settings_preview_ayah), 1),
            fontFamily = QuranFont,
            fontSize = 22.sp,
            lineHeight = 40.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}

private const val DISABLED_ALPHA = 0.38f

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    QuranyTheme {
        SettingsScreen(false, Constants.YELLOW, {}, {}, {})
    }
}
