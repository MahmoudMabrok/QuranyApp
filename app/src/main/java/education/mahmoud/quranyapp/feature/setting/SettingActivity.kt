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
import org.koin.android.ext.android.inject

/**
 * Reading settings (night reading mode and page background colour), rendered with
 * Compose and [QuranyTheme] so it matches the home screen. Changes are saved immediately.
 */
class SettingActivity : AppCompatActivity() {

    private val quranRepository: QuranRepository by inject()

    private var nightMode by mutableStateOf(false)
    private var backgroundColor by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nightMode = quranRepository.nightModeState
        backgroundColor = quranRepository.backColorState

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
                    )
                }
            }
        }
    }
}
