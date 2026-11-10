package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import io.github.sherifshabans.mushaf.Ayah
import io.github.sherifshabans.mushaf.MushafColors
import io.github.sherifshabans.mushaf.MushafDecoration
import io.github.sherifshabans.mushaf.MushafPager
import io.github.sherifshabans.mushaf.MushafSpread
import io.github.sherifshabans.mushaf.PageTurn
import io.github.sherifshabans.mushaf.SurahHeaderStyle as LibSurahHeaderStyle
import io.github.sherifshabans.mushaf.TajweedHit
import io.github.sherifshabans.mushaf.rememberMushafPagerState
import io.github.sherifshabans.mushaf.toArabicDigits

const val MUSHAF_PAGE_COUNT = 604

@Composable
fun MushafView(
    startPage: Int,
    modifier: Modifier = Modifier,
    colors: MushafColors = MushafColors.Light,
    paperColor: Color? = null,
    settings: MushafReaderSettings = MushafReaderSettings(),
    highlightedAyahs: Map<Int, Color> = emptyMap(),
    onAyahLongClick: ((Ayah) -> Unit)? = null,
    onTajweedClick: ((TajweedHit) -> Unit)? = null,
    onPageChanged: (Int) -> Unit = {},
) {
    val effectiveColors = if (paperColor != null) colors.copy(paper = paperColor) else colors
    val decoration = remember(settings) { settings.toDecoration() }
    val pagerState = rememberMushafPagerState(initialPage = startPage)
    val pageChanged = rememberUpdatedState(onPageChanged)
    var visiblePage by remember { mutableIntStateOf(startPage.coerceIn(1, MUSHAF_PAGE_COUNT)) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { index ->
            val page = index + 1
            visiblePage = page
            pageChanged.value(page)
        }
    }
    // MushafPager owns the right-to-left turn (slide or curl) and the wide-screen
    // spread. showHeaders stays off: the frame's medallion is the page number,
    // and a plain footer is drawn here only when that frame is absent.
    Column(modifier) {
        MushafPager(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            state = pagerState,
            tajweed = settings.tajweed,
            naturalMadd = settings.colorNaturalMadd,
            colors = effectiveColors,
            decoration = decoration,
            showHeaders = false,
            pageTurn = if (settings.pageTurn == MushafPageTurn.CURL) PageTurn.Curl else PageTurn.Slide,
            spread = if (settings.spread == MushafSpreadMode.AUTO) MushafSpread.Auto else MushafSpread.Single,
            highlightedAyahs = highlightedAyahs,
            onAyahLongClick = onAyahLongClick,
            onTajweedClick = if (settings.tajweed) onTajweedClick else null,
            pagePadding = PaddingValues(horizontal = 3.dp, vertical = 2.dp),
        )
        if (!settings.libraryDrawsPageNumber) {
            Box(Modifier.fillMaxWidth().padding(top = 2.dp), contentAlignment = Alignment.Center) {
                BasicText(
                    toArabicDigits(visiblePage),
                    style = TextStyle(fontSize = 15.sp, color = effectiveColors.marker),
                )
            }
        }
    }
}

/** Maps stored reader settings onto a 1.6.0 [MushafDecoration]. */
internal fun MushafReaderSettings.toDecoration(): MushafDecoration {
    val header = when (headerStyle) {
        SuraHeaderStyle.MODERN -> LibSurahHeaderStyle.MODERN
        SuraHeaderStyle.PLAIN -> LibSurahHeaderStyle.PLAIN
        else -> LibSurahHeaderStyle.CLASSIC
    }
    return when (look) {
        MushafLook.CAIRO -> MushafDecoration.Cairo
        MushafLook.DIYANET -> MushafDecoration.Diyanet
        MushafLook.INDOPAK -> MushafDecoration.IndoPak
        MushafLook.ORNATE -> MushafDecoration(
            pageFrame = pageFrame,
            surahHeaderStyle = header,
            ayahFrame = true,
            divisionMarks = drawDivisionMarks,
        )
    }
}
