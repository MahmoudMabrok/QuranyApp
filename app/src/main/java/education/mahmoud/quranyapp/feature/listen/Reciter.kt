package education.mahmoud.quranyapp.feature.listen

import androidx.annotation.StringRes
import education.mahmoud.quranyapp.R
import java.io.File

private const val BITRATE_HIGH = 128
private const val BITRATE_LOW = 64

/**
 * Reciters served per ayah by the alquran.cloud / islamic.network CDN.
 * (The old cdn.alquran.cloud/media/audio/ayah URL now answers HTTP 500.)
 */
enum class Reciter(val id: String, private val bitrate: Int, @StringRes val nameRes: Int) {
    ALAFASY("ar.alafasy", BITRATE_HIGH, R.string.reciter_alafasy),
    HUSARY("ar.husary", BITRATE_HIGH, R.string.reciter_husary),
    MINSHAWI("ar.minshawi", BITRATE_HIGH, R.string.reciter_minshawi),
    ABDUL_BASIT("ar.abdulbasitmurattal", BITRATE_LOW, R.string.reciter_abdul_basit),
    SUDAIS("ar.abdurrahmaansudais", BITRATE_LOW, R.string.reciter_sudais),
    MAHER("ar.mahermuaiqly", BITRATE_HIGH, R.string.reciter_maher),
    ;

    fun audioUrl(ayahIndex: Int): String = "https://cdn.islamic.network/quran/audio/$bitrate/$id/$ayahIndex.mp3"

    /** Cached file inside app-specific storage, so no storage permission is needed. */
    fun cacheFile(root: File, ayahIndex: Int): File = File(File(root, id), "$ayahIndex.mp3")

    companion object {
        fun fromId(id: String?): Reciter = values().firstOrNull { it.id == id } ?: ALAFASY
    }
}
