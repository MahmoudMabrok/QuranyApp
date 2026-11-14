package education.mahmoud.quranyapp.feature.setting.compose

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.text.SpannableString
import android.text.Spanned
import android.view.Gravity
import android.widget.TextView
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafFonts
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafPalette
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderInfo
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderSpan
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.toDecoration
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.toLibraryPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafReaderSettings
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafSpreadMode
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.home_Activity.compose.SafeInsets
import education.mahmoud.quranyapp.feature.home_Activity.compose.paddingExceptBottom
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranFont
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.NumberHelper
import androidx.compose.foundation.layout.PaddingValues
import io.github.sherifshabans.mushaf.MushafColors
import io.github.sherifshabans.mushaf.MushafPager
import io.github.sherifshabans.mushaf.rememberMushafPagerState

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
    reader: MushafReaderSettings = MushafReaderSettings(),
    onReaderChange: (MushafReaderSettings) -> Unit = {},
    versionName: String = "",
    onRate: () -> Unit = {},
    onUpdate: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = SafeInsets.content,
        topBar = {
            TopAppBar(
                windowInsets = SafeInsets.topBar,
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
                .paddingExceptBottom(padding)
                .verticalScroll(rememberScrollState())
                // bottom inset inside the scroll: content scrolls behind the nav bar but ends above it
                .padding(bottom = padding.calculateBottomPadding())
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

            SectionHeader(R.string.settings_section_mushaf)
            MushafSettings(
                settings = reader,
                nightMode = nightMode,
                backgroundColor = backgroundColor,
                onChange = onReaderChange,
            )

            SectionHeader(R.string.settings_section_about)
            SettingsCard {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(stringResource(R.string.settings_version), style = MaterialTheme.typography.titleMedium)
                    Text(
                        versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRate)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(stringResource(R.string.settings_rate), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.settings_rate_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onUpdate)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(stringResource(R.string.settings_update), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.settings_update_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun SectionHeader(@StringRes text: Int) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
internal fun SettingsCard(content: @Composable () -> Unit) {
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
private fun readingBackground(nightMode: Boolean, backgroundColor: Int): Color = if (nightMode) {
    colorResource(R.color.bg_ays_night_mode)
} else {
    colorResource(backgroundOptions.firstOrNull { it.id == backgroundColor }?.color ?: R.color.bg_white)
}

private data class HeaderOption(val style: SuraHeaderStyle, @StringRes val label: Int, @StringRes val summary: Int)

private val headerOptions = listOf(
    HeaderOption(SuraHeaderStyle.CLASSIC, R.string.mushaf_header_classic, R.string.mushaf_header_classic_summary),
    HeaderOption(SuraHeaderStyle.MODERN, R.string.mushaf_header_modern, R.string.mushaf_header_modern_summary),
    HeaderOption(SuraHeaderStyle.PLAIN, R.string.mushaf_header_plain, R.string.mushaf_header_plain_summary),
)

private data class LookOption(val look: MushafLook, @StringRes val label: Int, @StringRes val summary: Int)

private data class TurnOption(val turn: MushafPageTurn, @StringRes val label: Int, @StringRes val summary: Int)

private val turnOptions = listOf(
    TurnOption(MushafPageTurn.SLIDE, R.string.mushaf_turn_slide, R.string.mushaf_turn_slide_summary),
    TurnOption(MushafPageTurn.FLIP, R.string.mushaf_turn_flip, R.string.mushaf_turn_flip_summary),
    TurnOption(MushafPageTurn.FADE, R.string.mushaf_turn_fade, R.string.mushaf_turn_fade_summary),
    TurnOption(MushafPageTurn.COVER, R.string.mushaf_turn_cover, R.string.mushaf_turn_cover_summary),
)

private val lookOptions = listOf(
    LookOption(MushafLook.ORNATE, R.string.mushaf_look_ornate, R.string.mushaf_look_ornate_summary),
    LookOption(MushafLook.CAIRO, R.string.mushaf_look_cairo, R.string.mushaf_look_cairo_summary),
    LookOption(MushafLook.DIYANET, R.string.mushaf_look_diyanet, R.string.mushaf_look_diyanet_summary),
    LookOption(MushafLook.INDOPAK, R.string.mushaf_look_indopak, R.string.mushaf_look_indopak_summary),
)

@Composable
private fun MushafSettings(
    settings: MushafReaderSettings,
    nightMode: Boolean,
    backgroundColor: Int,
    onChange: (MushafReaderSettings) -> Unit,
) {
    val ornate = settings.ornateControlsApply
    MushafAppearancePreview(settings = settings, nightMode = nightMode)
    Spacer(Modifier.height(12.dp))
    LookCard(settings = settings, onChange = onChange)
    Spacer(Modifier.height(12.dp))
    HeaderStyleCard(
        settings = settings,
        enabled = ornate,
        nightMode = nightMode,
        backgroundColor = backgroundColor,
        onChange = onChange,
    )
    Spacer(Modifier.height(12.dp))
    SwitchCard(
        title = R.string.mushaf_page_frame,
        summary = if (ornate) R.string.mushaf_page_frame_summary else R.string.mushaf_ornate_only,
        checked = settings.pageFrame,
        enabled = ornate,
        onCheckedChange = { onChange(settings.copy(pageFrame = it)) },
    )
    Spacer(Modifier.height(12.dp))
    SwitchCard(
        title = R.string.mushaf_division_marks,
        summary = when {
            !ornate -> R.string.mushaf_ornate_only
            !settings.pageFrame -> R.string.mushaf_division_needs_frame
            else -> R.string.mushaf_division_marks_summary
        },
        checked = settings.divisionMarks,
        enabled = ornate && settings.pageFrame,
        onCheckedChange = { onChange(settings.copy(divisionMarks = it)) },
    )
    Spacer(Modifier.height(12.dp))
    PageTurnCard(settings = settings, onChange = onChange)
    Spacer(Modifier.height(12.dp))
    SwitchCard(
        title = R.string.mushaf_spread,
        summary = R.string.mushaf_spread_summary,
        checked = settings.spread == MushafSpreadMode.AUTO,
        enabled = true,
        onCheckedChange = { enabled ->
            onChange(settings.copy(spread = if (enabled) MushafSpreadMode.AUTO else MushafSpreadMode.SINGLE))
        },
    )
    Spacer(Modifier.height(12.dp))
    SwitchCard(
        title = R.string.mushaf_tajweed,
        summary = R.string.mushaf_tajweed_summary,
        checked = settings.tajweed,
        enabled = true,
        onCheckedChange = { onChange(settings.copy(tajweed = it)) },
    )
    Spacer(Modifier.height(12.dp))
    SwitchCard(
        title = R.string.mushaf_natural_madd,
        summary = R.string.mushaf_natural_madd_summary,
        checked = settings.naturalMadd,
        enabled = settings.tajweed,
        onCheckedChange = { onChange(settings.copy(naturalMadd = it)) },
    )
}

/**
 * Al-Fatiha drawn by mushaf-text with the settings being edited. The box height
 * is the page height, so the frame, header, marks, and tajweed all stay in view.
 * Swiping the sample uses the selected page turn.
 */
@Composable
private fun MushafAppearancePreview(settings: MushafReaderSettings, nightMode: Boolean) {
    val colors = if (nightMode) MushafColors.Dark else MushafColors.Light
    val turnLabel = when (settings.pageTurn) {
        MushafPageTurn.SLIDE -> R.string.mushaf_preview_turn_slide
        MushafPageTurn.FLIP -> R.string.mushaf_preview_turn_flip
        MushafPageTurn.FADE -> R.string.mushaf_preview_turn_fade
        MushafPageTurn.COVER -> R.string.mushaf_preview_turn_cover
    }
    SettingsCard {
        Column(Modifier.padding(12.dp)) {
            Text(stringResource(R.string.mushaf_preview_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.mushaf_preview_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(MUSHAF_PREVIEW_HEIGHT)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.paper),
            ) {
                if (!LocalInspectionMode.current) {
                    // Real pages, including the chosen slide or leaf-turn, so the
                    // sample matches the reader. One column: the box is narrower
                    // than the two-page spread.
                    MushafPager(
                        modifier = Modifier.fillMaxSize(),
                        state = rememberMushafPagerState(initialPage = MUSHAF_PREVIEW_PAGE),
                        tajweed = settings.tajweed,
                        naturalMadd = settings.colorNaturalMadd,
                        colors = colors,
                        decoration = settings.toDecoration(),
                        showHeaders = false,
                        pageTurn = settings.pageTurn.toLibraryPageTurn(),
                        pagePadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                    )
                }
            }
            Text(
                stringResource(turnLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private val MUSHAF_PREVIEW_HEIGHT = 300.dp
private const val MUSHAF_PREVIEW_PAGE = 1

@Composable
private fun PageTurnCard(settings: MushafReaderSettings, onChange: (MushafReaderSettings) -> Unit) {
    SettingsCard {
        Column(Modifier.padding(vertical = 8.dp).selectableGroup().testTag("page_turn")) {
            Text(
                stringResource(R.string.mushaf_page_turn),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            Text(
                stringResource(R.string.mushaf_page_turn_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            )
            turnOptions.forEach { option ->
                val selected = option.turn == settings.pageTurn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onChange(settings.copy(pageTurn = option.turn)) },
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(stringResource(option.label), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            stringResource(option.summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LookCard(settings: MushafReaderSettings, onChange: (MushafReaderSettings) -> Unit) {
    SettingsCard {
        Column(Modifier.padding(vertical = 8.dp).selectableGroup()) {
            Text(
                stringResource(R.string.mushaf_look),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            Text(
                stringResource(R.string.mushaf_look_summary),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            )
            lookOptions.forEach { option ->
                val selected = option.look == settings.look
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onChange(settings.copy(look = option.look)) },
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected, onClick = null)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(stringResource(option.label), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(option.summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderStyleCard(
    settings: MushafReaderSettings,
    enabled: Boolean,
    nightMode: Boolean,
    backgroundColor: Int,
    onChange: (MushafReaderSettings) -> Unit,
) {
    SettingsCard {
        Column(
            Modifier
                .padding(vertical = 8.dp)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .selectableGroup(),
        ) {
            Text(
                stringResource(R.string.mushaf_header_style),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            if (!enabled) {
                Text(
                    stringResource(R.string.mushaf_ornate_only),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            headerOptions.forEach { option ->
                val selected = option.style == settings.headerStyle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onChange(settings.copy(headerStyle = option.style)) },
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected, onClick = null, enabled = enabled)
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(stringResource(option.label), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(option.summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (option.style != SuraHeaderStyle.PLAIN) {
                        SuraHeaderPreview(
                            style = option.style,
                            nightMode = nightMode,
                            background = readingBackground(nightMode, backgroundColor),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchCard(
    @StringRes title: Int,
    @StringRes summary: Int,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

/** Real sura header (same drawing code as the reading pages) for Al-Fatiha. */
@Composable
private fun SuraHeaderPreview(style: SuraHeaderStyle, nightMode: Boolean, background: Color, modifier: Modifier = Modifier) {
    val ink = colorResource(if (nightMode) R.color.ayas_color_night_mode else R.color.ayas_color)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                textSize = 17f
                gravity = Gravity.CENTER
                val pad = (4 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
            }
        },
        update = { tv ->
            val palette = MushafPalette.of(style, nightMode, background.toArgb(), ink.toArgb())
            val info = SuraHeaderInfo(index = 1, name = "الفاتحة", revelationType = "Meccan", numOfAyahs = 7)
            val text = SpannableString(info.name)
            text.setSpan(
                SuraHeaderSpan(info, style, palette, MushafFonts.get(tv.context)) {
                    val w = tv.width - tv.totalPaddingLeft - tv.totalPaddingRight
                    if (w > 0) w else tv.resources.displayMetrics.widthPixels - (96 * tv.resources.displayMetrics.density).toInt()
                },
                0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            tv.setBackgroundColor(background.toArgb())
            tv.text = text
        },
    )
}

@Composable
private fun ReadingPreview(nightMode: Boolean, backgroundColor: Int) {
    val background = readingBackground(nightMode, backgroundColor)
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
