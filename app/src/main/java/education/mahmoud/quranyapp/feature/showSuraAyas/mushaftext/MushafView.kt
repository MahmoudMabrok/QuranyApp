package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import io.github.sherifshabans.mushaf.Ayah
import io.github.sherifshabans.mushaf.MushafColors
import io.github.sherifshabans.mushaf.MushafDecoration
import io.github.sherifshabans.mushaf.MushafPage
import io.github.sherifshabans.mushaf.SurahHeaderStyle as LibSurahHeaderStyle
import io.github.sherifshabans.mushaf.toArabicDigits

const val MUSHAF_PAGE_COUNT = 604

@Composable
fun MushafView(
    startPage: Int,
    modifier: Modifier = Modifier,
    colors: MushafColors = MushafColors.Light,
    paperColor: Color? = null,
    headerStyle: SuraHeaderStyle = SuraHeaderStyle.CLASSIC,
    highlightedAyahs: Map<Int, Color> = emptyMap(),
    onAyahLongClick: ((Ayah) -> Unit)? = null,
    onPageChanged: (Int) -> Unit = {},
) {
    val effectiveColors = if (paperColor != null) colors.copy(paper = paperColor) else colors
    val decoration = MushafDecoration(
        pageFrame = true,
        surahHeaderStyle = when (headerStyle) {
            SuraHeaderStyle.MODERN -> LibSurahHeaderStyle.MODERN
            SuraHeaderStyle.PLAIN -> LibSurahHeaderStyle.PLAIN
            else -> LibSurahHeaderStyle.CLASSIC
        },
        ayahFrame = true,
    )
    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, MUSHAF_PAGE_COUNT - 1),
    ) { MUSHAF_PAGE_COUNT }
    val pageChanged = rememberUpdatedState(onPageChanged)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { pageChanged.value(it + 1) }
    }
    val outer = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier,
            beyondViewportPageCount = 1,
            key = { it },
        ) { index ->
            CompositionLocalProvider(LocalLayoutDirection provides outer) {
                val page = index + 1
                Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    MushafPage(
                        page = page,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        colors = effectiveColors,
                        decoration = decoration,
                        highlightedAyahs = highlightedAyahs,
                        onAyahLongClick = onAyahLongClick,
                    )
                    Box(Modifier.fillMaxWidth().padding(top = 2.dp), contentAlignment = Alignment.Center) {
                        BasicText(toArabicDigits(page), style = TextStyle(fontSize = 15.sp, color = effectiveColors.marker))
                    }
                }
            }
        }
    }
}
