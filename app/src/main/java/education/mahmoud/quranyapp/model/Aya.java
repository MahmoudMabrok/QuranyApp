package education.mahmoud.quranyapp.model;


import com.google.gson.annotations.SerializedName;

public class Aya {

    @SerializedName("num")
    private String num;
    @SerializedName("text")
    private String text;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}