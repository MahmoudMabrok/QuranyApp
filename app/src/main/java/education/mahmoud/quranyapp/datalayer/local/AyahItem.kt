package education.mahmoud.quranyapp.datalayer.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
class AyahItem {
    @PrimaryKey
    var ayahIndex: Int
    var surahIndex: Int
    var ayahInSurahIndex: Int
    var text: String
    var textClean: String
    var audioPath: String? = null

    constructor(
        ayahIndex: Int,
        surahIndex: Int,
        ayahInSurahIndex: Int,
        text: String,
        textClean: String,
        audioPath: String?
    ) {
        this.ayahIndex = ayahIndex
        this.surahIndex = surahIndex
        this.ayahInSurahIndex = ayahInSurahIndex
        this.text = text
        this.textClean = textClean
        this.audioPath = audioPath
    }

    @Ignore
    constructor(
        ayahIndex: Int,
        surahIndex: Int,
        ayahInSurahIndex: Int,
        text: String,
        textClean: String
    ) {
        this.ayahIndex = ayahIndex
        this.surahIndex = surahIndex
        this.ayahInSurahIndex = ayahInSurahIndex
        this.text = text
        this.textClean = textClean
    }
}
