package education.mahmoud.quranyapp.asr

import android.content.Context
import org.json.JSONObject

/**
 * Expected phonemes for each Quran word, from the same alphabet the recognizer writes.
 * Each word is (spoken phonemes, Arabic spelling).
 */
object AyahReference {

    data class Word(val phonemes: String, val arabic: String, val newAyah: Boolean)

    private val lock = Any()
    private var cached: JSONObject? = null

    fun words(
        context: Context,
        startSura: Int,
        startAyah: Int,
        endSura: Int,
        endAyah: Int,
    ): List<Word> {
        val root = synchronized(lock) {
            cached ?: JSONObject(ModelDownloader.getCorpusFile(context).readText()).also { cached = it }
        }
        val fromSura = startSura.coerceAtLeast(1)
        val fromAyah = startAyah.coerceAtLeast(1)
        val toSura = if (endSura > 0) endSura else fromSura
        val toAyah = if (endAyah > 0) endAyah else fromAyah
        val words = ArrayList<Word>()
        val surahs = root.getJSONArray("surahs")
        for (s in 0 until surahs.length()) {
            val surah = surahs.getJSONObject(s)
            val surahNumber = surah.getInt("n")
            if (surahNumber < fromSura || surahNumber > toSura) continue
            val ayahs = surah.getJSONArray("ayahs")
            for (a in 0 until ayahs.length()) {
                val ayah = ayahs.getJSONObject(a)
                val ayahNumber = ayah.getInt("n")
                if (surahNumber == fromSura && ayahNumber < fromAyah) continue
                if (surahNumber == toSura && ayahNumber > toAyah) continue
                val list = ayah.getJSONArray("w")
                for (w in 0 until list.length()) {
                    val triple = list.getJSONArray(w)
                    words.add(
                        Word(
                            phonemes = triple.getString(1),
                            arabic = triple.getString(2),
                            newAyah = w == 0 && words.isNotEmpty(),
                        ),
                    )
                }
            }
        }
        return words
    }
}
