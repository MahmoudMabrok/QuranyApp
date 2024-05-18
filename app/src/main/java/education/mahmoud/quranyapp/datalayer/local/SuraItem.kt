package education.mahmoud.quranyapp.datalayer.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "surah")
class SuraItem {
    // surah start from 1 to 114
    @PrimaryKey
    var index: Int
    var startIndex: Int
    var numOfAyahs: Int
    var name: String
    var audioPath: String? = null

    constructor(index: Int, startIndex: Int, numOfAyahs: Int, name: String, audioPath: String?) {
        this.index = index
        this.startIndex = startIndex
        this.numOfAyahs = numOfAyahs
        this.name = name
        this.audioPath = audioPath
    }

    @Ignore
    constructor(index: Int, startIndex: Int, numOfAyahs: Int, name: String) {
        this.index = index
        this.startIndex = startIndex
        this.numOfAyahs = numOfAyahs
        this.name = name
    }
}
