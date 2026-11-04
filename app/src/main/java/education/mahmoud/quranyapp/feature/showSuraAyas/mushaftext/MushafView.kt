package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import io.github.sherifshabans.mushaf.Ayah
import io.github.sherifshabans.mushaf.MushafColors
import io.github.sherifshabans.mushaf.MushafPage
import io.github.sherifshabans.mushaf.Quran
import io.github.sherifshabans.mushaf.toArabicDigits

const val MUSHAF_PAGE_COUNT = 604

/**
 * The whole mushaf, one [MushafPage] per screen. Owns the paging itself: 604 pages,
 * right-to-left like a printed copy (next page comes in from the left) whatever the locale.
 *
 * @param startPage mushaf page (1..604) shown first.
 * @param paperColor optional override for the page background (text area).
 * @param headerStyle surah name frame style (classic / modern / plain), same as the customized reader.
 * @param onPageChanged called with the mushaf page (1..604) each time a page settles,
 * including the start page; use it to save the last page and the reading log.
 */
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
    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, MUSHAF_PAGE_COUNT - 1),
    ) { MUSHAF_PAGE_COUNT }
    val pageChanged = rememberUpdatedState(onPageChanged)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { pageChanged.value(it + 1) }
    }
    val outer = LocalLayoutDirection.current
    val context = LocalContext.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier,
            beyondViewportPageCount = 1,
            key = { it },
        ) { index ->
            CompositionLocalProvider(LocalLayoutDirection provides outer) {
                val page = index + 1
                val startingSurah = remember(page) { startingSurahOnPage(context, page) }
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(effectiveColors.paper)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        MushafPage(
                            page = page,
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            colors = effectiveColors,
                            highlightedAyahs = highlightedAyahs,
                            onAyahLongClick = onAyahLongClick,
                        )
                        MushafPageFrame(colors = effectiveColors)
                        if (startingSurah != null && headerStyle != SuraHeaderStyle.PLAIN) {
                            SurahHeaderOverlay(
                                name = startingSurah,
                                style = headerStyle,
                                colors = effectiveColors,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .padding(top = 14.dp, start = 16.dp, end = 16.dp)
                                    .height(36.dp),
                            )
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(top = 2.dp), contentAlignment = Alignment.Center) {
                        BasicText(
                            toArabicDigits(page),
                            style = TextStyle(fontSize = 15.sp, color = effectiveColors.marker),
                        )
                    }
                }
            }
        }
    }
}

/** First ayah of a surah that starts on this page, or null. */
private fun startingSurahOnPage(context: Context, page: Int): String? {
    return try {
        val ayahs = Quran.page(context, page)
        ayahs.firstOrNull { it.number == 1 }?.let { ayah ->
            Quran.surahs.getOrNull(ayah.surah - 1)?.nameArabic ?: "سورة ${ayah.surah}"
        }
    } catch (_: Exception) {
        null
    }
}

/** Decorative double-line frame around a mushaf page (outer + inner rule + corner marks). */
@Composable
private fun MushafPageFrame(colors: MushafColors) {
    val line = colors.marker.copy(alpha = 0.7f)
    val thin = colors.marker.copy(alpha = 0.4f)
    Canvas(Modifier.fillMaxSize()) {
        val stroke = 1.5.dp.toPx()
        val inset = 4.dp.toPx()
        val innerInset = 10.dp.toPx()
        drawRect(
            color = line,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            style = Stroke(width = stroke),
        )
        drawRect(
            color = thin,
            topLeft = Offset(innerInset, innerInset),
            size = Size(size.width - innerInset * 2, size.height - innerInset * 2),
            style = Stroke(width = stroke * 0.7f),
        )
        val corner = 14.dp.toPx()
        val path = Path()
        listOf(
            Offset(inset, inset),
            Offset(size.width - inset, inset),
            Offset(inset, size.height - inset),
            Offset(size.width - inset, size.height - inset),
        ).forEach { c ->
            path.reset()
            path.moveTo(c.x - corner * (if (c.x < size.width / 2) 1 else -1), c.y)
            path.lineTo(c.x, c.y)
            path.lineTo(c.x, c.y - corner * (if (c.y < size.height / 2) 1 else -1))
            drawPath(path, line, style = Stroke(width = stroke * 1.4f))
        }
    }
}

/** Overlay cartouche / pill for the surah name when a surah starts on the page. */
@Composable
private fun SurahHeaderOverlay(
    name: String,
    style: SuraHeaderStyle,
    colors: MushafColors,
    modifier: Modifier = Modifier,
) {
    val isModern = style == SuraHeaderStyle.MODERN
    val bg = if (isModern) Color(0xFF00A388) else Color(0xFF0F5245)
    val fg = if (isModern) Color.White else Color(0xFFF3D98B)
    val shape = if (isModern) RoundedCornerShape(50) else RoundedCornerShape(6.dp)
    Box(
        modifier = modifier.background(bg, shape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = name,
            style = TextStyle(
                color = fg,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
    }
}
