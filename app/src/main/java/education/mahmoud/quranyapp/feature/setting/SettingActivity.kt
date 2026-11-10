package education.mahmoud.quranyapp.feature.setting

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.setting.compose.SettingsScreen
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafReaderSettings
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafSpreadMode
import org.koin.android.ext.android.inject
import education.mahmoud.quranyapp.utils.EdgeToEdge
import education.mahmoud.quranyapp.utils.StoreReview

/**
 * Reading settings (night mode, mushaf look, header, frame, page turn, tajweed), rendered with
 * Compose and [QuranyTheme] so it matches the home screen. Changes are saved immediately.
 * Existing header-style and page-frame values are kept; new 1.6.0 options default to the previous reader.
 */
class SettingActivity : AppCompatActivity() {

    private val quranRepository: QuranRepository by inject()

    private var nightMode by mutableStateOf(false)
    private var backgroundColor by mutableStateOf(0)
    private var reader by mutableStateOf(MushafReaderSettings())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        nightMode = quranRepository.nightModeState
        backgroundColor = quranRepository.backColorState
        reader = MushafReaderSettings(
            headerStyle = SuraHeaderStyle.fromId(quranRepository.mushafHeaderStyle),
            pageFrame = quranRepository.mushafPageFrame,
            look = MushafLook.fromId(quranRepository.mushafLook),
            divisionMarks = quranRepository.mushafDivisionMarks,
            pageTurn = MushafPageTurn.fromId(quranRepository.mushafPageTurn),
            spread = MushafSpreadMode.fromId(quranRepository.mushafSpread),
            tajweed = quranRepository.mushafTajweed,
            naturalMadd = quranRepository.mushafNaturalMadd,
        )

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranyTheme {
                    SettingsScreen(
                        nightMode = nightMode,
                        backgroundColor = backgroundColor,
                        onNightModeChange = {
                            nightMode = it
                            quranRepository.nightModeState = it
                        },
                        onBackgroundColorChange = {
                            backgroundColor = it
                            quranRepository.backColorState = it
                        },
                        onBack = ::finish,
                        reader = reader,
                        onReaderChange = ::saveReader,
                        versionName = StoreReview.versionName(this@SettingActivity),
                        onRate = { StoreReview.openStore(this@SettingActivity) },
                        onUpdate = { StoreReview.openListing(this@SettingActivity) },
                    )
                }
            }
        }
    }

    private fun saveReader(next: MushafReaderSettings) {
        reader = next
        quranRepository.mushafHeaderStyle = next.headerStyle.id
        quranRepository.mushafPageFrame = next.pageFrame
        quranRepository.mushafLook = next.look.id
        quranRepository.mushafDivisionMarks = next.divisionMarks
        quranRepository.mushafPageTurn = next.pageTurn.id
        quranRepository.mushafSpread = next.spread.id
        quranRepository.mushafTajweed = next.tajweed
        quranRepository.mushafNaturalMadd = next.naturalMadd
    }
}
