
package education.mahmoud.quranyapp.data_layer.model.tafseer_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Edition {

    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("englishName")
    @Expose
    private String englishName;
    @SerializedName("format")
    @Expose
    private String format;
    @SerializedName("type")
    @Expose
    private String type;

    /**
     * No args constructor for use in serialization
     */
    public Edition() {
    }

    /**
     * @param englishName
     * @param name
     * @param format
     * @param language
     * @param type
     * @param identifier
     */
    public Edition(String identifier, String language, String name, String englishName, String format, String type) {
        super();
        this.identifier = identifier;
        this.language = language;
        this.name = name;
        this.englishName = englishName;
        this.format = format;
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
