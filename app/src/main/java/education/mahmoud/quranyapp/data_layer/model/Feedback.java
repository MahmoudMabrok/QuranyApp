package education.mahmoud.quranyapp.data_layer.model;

import androidx.annotation.Keep;

@Keep
public class Feedback {

    private String positives;
    private String negatives;
    private String suggestions;
    private String appVersion;

    public Feedback(String positives, String negatives, String suggestions) {
        this.positives = positives;
        this.negatives = negatives;
        this.suggestions = suggestions;
    }

    public Feedback(String positives, String negatives, String suggestions, String appVersion) {
        this.positives = positives;
        this.negatives = negatives;
        this.suggestions = suggestions;
        this.appVersion = appVersion;
    }

    public String getPositives() {
        return positives;
    }

    public void setPositives(String positives) {
        this.positives = positives;
    }

    public String getNegatives() {
        return negatives;
    }

    public void setNegatives(String negatives) {
        this.negatives = negatives;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
