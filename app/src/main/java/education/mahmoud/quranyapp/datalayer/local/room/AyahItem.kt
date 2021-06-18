package education.mahmoud.quranyapp.datalayer.local.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "ayahs")
@Parcelize
data class AyahItem(
    @PrimaryKey
    var ayahIndex: Int = 0,
    var surahIndex: Int = 0,
    var pageNum: Int = 0,
    var juz: Int = 0,
    var hizbQuarter: Int = 0,
    var isSajda: Boolean = false,
    var manzil: Int = 0,
    var ayahInSurahIndex: Int = 0,
    var text: String,
    var textClean: String = "",
    var tafseer: String = "",
    var audioPath: String? = null
) : Parcelable
