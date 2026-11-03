package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.feature.ayah_actions.AyahActionsSheet
import education.mahmoud.quranyapp.feature.ayah_actions.TafseerSheet
import education.mahmoud.quranyapp.feature.listening_activity.AyahsListen
import education.mahmoud.quranyapp.feature.listening_activity.ListenServie
import education.mahmoud.quranyapp.feature.showSuraAyas.AyahsViewModel
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.Data
import io.github.sherifshabans.mushaf.Ayah
import io.github.sherifshabans.mushaf.MushafColors
import io.github.sherifshabans.mushaf.MushafPager
import io.github.sherifshabans.mushaf.rememberMushafPagerState
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date

/**
 * Reader that draws the Madinah mushaf as text with
 * [mushaf-text](https://github.com/sherifshabans/mushaf-text): exact 15-line pages in the
 * KFGQPC Hafs font, turning right to left. Ayah ids in mushaf-text (1..6236, mushaf order)
 * are the same numbers as [AyahItem.ayahIndex], so long-press actions and highlights map
 * straight onto the app's data.
 */
class MushafTextActivity : AppCompatActivity() {

    private val model: AyahsViewModel by viewModel()
    private var currentPage = 1
    private var toast: Toast? = null
    private var listenIntent: Intent? = null
    private var highlights by mutableStateOf<Set<Int>>(emptySet())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPage = intent.getIntExtra(Constants.PAGE_INDEX, 1).coerceIn(1, LAST_PAGE)
        highlights = model.highlightedAyahs
        val night = model.nightModeState
        setContent { Reader(night) }
    }

    @Composable
    private fun Reader(night: Boolean) {
        val colors = if (night) MushafColors.Dark else MushafColors.Light
        val state = rememberMushafPagerState(initialPage = currentPage)
        LaunchedEffect(state) {
            snapshotFlow { state.currentPage }.collect { currentPage = it + 1 }
        }
        val highlightColor = if (night) Color(0x66B8860B) else Color(0x66FFC107)
        Column(
            Modifier.fillMaxSize().background(colors.paper).statusBarsPadding().navigationBarsPadding(),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Chip(getString(R.string.mushaf_text_old_view), colors.marker) { openOldView() }
                Chip(getString(R.string.mushaf_text_bookmark), colors.marker) { bookmarkCurrentPage() }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                MushafPager(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    colors = colors,
                    highlightedAyahs = highlights.associateWith { highlightColor },
                    onAyahLongClick = ::onAyahLongPressed,
                )
            }
        }
    }

    @Composable
    private fun Chip(text: String, color: Color, onClick: () -> Unit) {
        BasicText(
            text = text,
            style = TextStyle(fontSize = 14.sp, color = color),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.12f))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }

    private fun onAyahLongPressed(ayah: Ayah) {
        val item = model.getAyah(ayah.id) ?: run {
            showMessage(getString(R.string.mushaf_text_ayah_not_loaded))
            return
        }
        AyahActionsSheet.show(
            this,
            item,
            highlighted = item.ayahIndex in highlights,
            night = model.nightModeState,
            listener = object : AyahActionsSheet.Listener {
                override fun onShowTafseer(ayah: AyahItem) {
                    TafseerSheet.show(this@MushafTextActivity, ayah, model.nightModeState)
                }

                override fun onToggleHighlight(ayah: AyahItem, highlight: Boolean) {
                    model.setAyahHighlighted(ayah.ayahIndex, highlight)
                    highlights = model.highlightedAyahs
                    showMessage(getString(if (highlight) R.string.ayah_highlighted else R.string.ayah_highlight_removed))
                }

                override fun onPlay(ayah: AyahItem) {
                    playAyah(ayah)
                }
            },
        )
    }

    private fun playAyah(ayah: AyahItem) {
        if (ayah.audioPath == null) {
            showMessage(getString(R.string.not_downlod_audio))
            return
        }
        listenIntent?.let { stopService(it) }
        val listen = AyahsListen()
        listen.ayahItemList = listOf(ayah)
        listenIntent = ListenServie.createService(applicationContext, listen)
    }

    private fun bookmarkCurrentPage() {
        val first = model.getAyahsByPage(currentPage).firstOrNull()
        val bookmark = BookmarkItem()
        bookmark.timemills = Date().time
        bookmark.pageNum = currentPage
        bookmark.suraName = first?.let { Data.SURA_NAMES.getOrNull(it.surahIndex - 1) }.orEmpty()
        model.addBookmark(bookmark)
        showMessage(getString(R.string.mushaf_text_bookmark_saved))
    }

    private fun openOldView() {
        MushafTextPrefs.setEnabled(this, false)
        startActivity(
            Intent(this, ShowAyahsActivity::class.java).putExtra(Constants.PAGE_INDEX, currentPage),
        )
        finish()
    }

    override fun onStop() {
        super.onStop()
        model.addLatestread(currentPage)
    }

    private fun showMessage(message: String) {
        toast?.cancel()
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT).also { it.show() }
    }

    companion object {
        private const val LAST_PAGE = 604

        fun intent(context: Context, page: Int): Intent =
            Intent(context, MushafTextActivity::class.java).putExtra(Constants.PAGE_INDEX, page)
    }
}
