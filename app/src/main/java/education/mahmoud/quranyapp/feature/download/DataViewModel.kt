package education.mahmoud.quranyapp.feature.download

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.listen.AudioStorage
import education.mahmoud.quranyapp.feature.listen.Reciter
import education.mahmoud.quranyapp.feature.listen.ReciterDownloads
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

/** Offline data status: Quran text and tafseer (bundled, imported on first launch) and recitations. */
class DataViewModel(app: Application) : AndroidViewModel(app), KoinComponent {

    private val repository: QuranRepository by inject()
    private val storage = AudioStorage(app.getExternalFilesDir("audio") ?: File(app.filesDir, "audio"))

    var ayahs by mutableStateOf<Int?>(null)
        private set
    var tafseer by mutableStateOf<Int?>(null)
        private set
    var downloads by mutableStateOf<List<ReciterDownloads>>(emptyList())
        private set

    /** String resource of a one-shot message for the snackbar. */
    var message by mutableStateOf<Int?>(null)
        private set

    fun refresh() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Triple(repository.totlaAyahs, repository.totalTafseerDownloaded, storage.usage())
            }.let { (a, t, d) ->
                ayahs = a
                tafseer = t
                downloads = d
            }
        }
    }

    fun deleteDownloads(target: Reciter?) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { if (target == null) storage.deleteAll() else storage.delete(target) }
            message = if (ok) R.string.downloads_deleted else R.string.error
            refresh()
        }
    }

    fun consumeMessage() {
        message = null
    }

    companion object {
        const val TOTAL_AYAHS = 6236
    }
}
