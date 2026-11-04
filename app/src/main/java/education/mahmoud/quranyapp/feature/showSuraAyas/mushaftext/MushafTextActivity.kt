package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.ayah_actions.TafseerSheet
import education.mahmoud.quranyapp.feature.listening_activity.AyahsListen
import education.mahmoud.quranyapp.feature.listening_activity.ListenServie
import education.mahmoud.quranyapp.feature.showSuraAyas.AyahsViewModel
import education.mahmoud.quranyapp.utils.Constants
import io.github.sherifshabans.mushaf.Ayah
import io.github.sherifshabans.mushaf.MushafColors
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    private val readLog by lazy { ReadLogRecorder(model) }

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
        val startPage = remember { currentPage }
        val highlightColor = if (night) Color(0x66B8860B) else Color(0x66FFC107)
        Box(
            Modifier.fillMaxSize().background(colors.paper).statusBarsPadding().navigationBarsPadding(),
        ) {
            MushafView(
                startPage = startPage,
                modifier = Modifier.fillMaxSize(),
                colors = colors,
                paperColor = colors.paper,
                headerStyle = SuraHeaderStyle.fromId(model.mushafHeaderStyle),
                highlightedAyahs = highlights.associateWith { highlightColor },
                onAyahLongClick = ::onAyahLongPressed,
                onPageChanged = { page ->
                    currentPage = page
                    readLog.onPageShown(page)
                },
            )
        }
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

    override fun onResume() {
        super.onResume()
        readLog.load()
        readLog.onPageShown(currentPage)
    }

    override fun onStop() {
        super.onStop()
        model.addLatestread(currentPage)
        readLog.save()
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
