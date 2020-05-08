package education.mahmoud.quranyapp.feature.listening_activity

import android.os.Parcelable
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AyahsListen(var ayahItemList: List<AyahItem> = arrayListOf()) : Parcelable
