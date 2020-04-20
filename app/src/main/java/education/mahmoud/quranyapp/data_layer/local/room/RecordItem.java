package education.mahmoud.quranyapp.data_layer.local.room;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import education.mahmoud.quranyapp.utils.Constants;

@Entity(tableName = "records")
public class RecordItem implements Parcelable {

    public static final Parcelable.Creator<RecordItem> CREATOR = new Parcelable.Creator<RecordItem>() {
        @Override
        public RecordItem createFromParcel(Parcel source) {
            return new RecordItem(source);
        }

        @Override
        public RecordItem[] newArray(int size) {
            return new RecordItem[size];
        }
    };
    int result = Constants.NA;

    @PrimaryKey(autoGenerate = true)
    private long id;
    private int startIndex;
    private int endIndex;
    private int duration;
    private String fileName;
    private String filePath;
    private String recognizedText;

    @Ignore
    public RecordItem() {
    }

    @Ignore
    public RecordItem(int startIndex, int endIndex, String fileName, String filePath) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public RecordItem(long id, int startIndex, int endIndex, String fileName, String filePath, String recognizedText, int result) {
        this.id = id;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.fileName = fileName;
        this.filePath = filePath;
        this.recognizedText = recognizedText;
        this.result = result;
    }

    protected RecordItem(Parcel in) {
        this.id = in.readLong();
        this.startIndex = in.readInt();
        this.endIndex = in.readInt();
        this.fileName = in.readString();
        this.filePath = in.readString();
        this.recognizedText = in.readString();
        this.result = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.startIndex);
        dest.writeInt(this.endIndex);
        dest.writeString(this.fileName);
        dest.writeString(this.filePath);
        dest.writeString(this.recognizedText);
        dest.writeInt(this.result);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
