# Cases 

```
 Java :
 // traverse ayahs to check if it downloaded or not
  for (ayahItem in ayahsToListen) {
      if (ayahItem.audioPath == null) {
          ayahsToDownLoad.add(ayahItem)
      }
  }

  Kotlin:  ayahsToDownLoad = ayahsToListen.filter { it.audioPath == null }
 
```

```
Java :
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

Koltin 
@Parcelize
data class AyahsListen (var ayahItemList:List<AyahItem> = arrayListOf()) :Parcelable





```

  
