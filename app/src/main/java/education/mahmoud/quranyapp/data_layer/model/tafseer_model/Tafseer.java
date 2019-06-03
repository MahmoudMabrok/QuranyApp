
package education.mahmoud.quranyapp.data_layer.model.tafseer_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tafseer {

    @SerializedName("code")
    @Expose
    private int code;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("data")
    @Expose
    private Data data;

    /**
     * No args constructor for use in serialization
     */
    public Tafseer() {
    }

    /**
     * @param status
     * @param data
     * @param code
     */
    public Tafseer(int code, String status, Data data) {
        super();
        this.code = code;
        this.status = status;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

}
