package education.mahmoud.quranyapp.feature.listening_activity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import education.mahmoud.quranyapp.data_layer.local.room.AyahItem;

public class AyahsListen implements Parcelable {

    List<AyahItem> ayahItemList = new ArrayList<>();

    public List<AyahItem> getAyahItemList() {
        return ayahItemList;
    }

    public void setAyahItemList(List<AyahItem> ayahItemList) {
        this.ayahItemList = ayahItemList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.ayahItemList);
    }

    public AyahsListen() {
    }

    protected AyahsListen(Parcel in) {
        this.ayahItemList = in.createTypedArrayList(AyahItem.CREATOR);
    }

    public static final Parcelable.Creator<AyahsListen> CREATOR = new Parcelable.Creator<AyahsListen>() {
        @Override
        public AyahsListen createFromParcel(Parcel source) {
            return new AyahsListen(source);
        }

        @Override
        public AyahsListen[] newArray(int size) {
            return new AyahsListen[size];
        }
    };
}
