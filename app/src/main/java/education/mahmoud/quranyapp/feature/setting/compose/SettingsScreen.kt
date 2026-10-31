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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
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
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.home_Activity.compose.SafeInsets
import education.mahmoud.quranyapp.feature.home_Activity.compose.paddingExceptBottom
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
    headerStyle: Int = SuraHeaderStyle.DEFAULT.id,
    pageFrame: Boolean = true,
    onHeaderStyleChange: (Int) -> Unit = {},
    onPageFrameChange: (Boolean) -> Unit = {},
    bookFlip: Boolean = true,
    onBookFlipChange: (Boolean) -> Unit = {},
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

            Spacer(Modifier.height(12.dp))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = bookFlip, role = Role.Switch, onValueChange = onBookFlipChange)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(stringResource(R.string.book_flip), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.book_flip_summary),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = bookFlip, onCheckedChange = null)
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

            SectionHeader(R.string.settings_section_mushaf)
            MushafSettings(
                headerStyle = headerStyle,
                pageFrame = pageFrame,
                nightMode = nightMode,
                backgroundColor = backgroundColor,
                onHeaderStyleChange = onHeaderStyleChange,
                onPageFrameChange = onPageFrameChange,
            )

            SectionHeader(R.string.settings_section_preview)
            ReadingPreview(nightMode = nightMode, backgroundColor = backgroundColor)
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

@Composable
private fun MushafSettings(
    headerStyle: Int,
    pageFrame: Boolean,
    nightMode: Boolean,
    backgroundColor: Int,
    onHeaderStyleChange: (Int) -> Unit,
    onPageFrameChange: (Boolean) -> Unit,
) {
    SettingsCard {
        Column(Modifier.padding(vertical = 8.dp).selectableGroup()) {
            Text(
                stringResource(R.string.mushaf_header_style),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            headerOptions.forEach { option ->
                val selected = option.style.id == headerStyle
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onHeaderStyleChange(option.style.id) })
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
    Spacer(Modifier.height(12.dp))
    SettingsCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = pageFrame, role = Role.Switch, onValueChange = onPageFrameChange)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 16.dp)) {
                Text(stringResource(R.string.mushaf_page_frame), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.mushaf_page_frame_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = pageFrame, onCheckedChange = null)
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
