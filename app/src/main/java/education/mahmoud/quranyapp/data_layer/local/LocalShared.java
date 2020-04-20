package education.mahmoud.quranyapp.data_layer.local;

import android.content.Context;
import android.content.SharedPreferences;

import education.mahmoud.quranyapp.utils.Constants;

public class LocalShared {

    private SharedPreferences preferences;

    public LocalShared(Context context) {
        preferences = context.getSharedPreferences("quran_data", Context.MODE_PRIVATE);
//        editor = preferences.edit();
    }

    public void addLatestread(int index) {
        preferences.edit().putInt(Constants.LAST_INDEX, index).apply();
    }

    public void addLatestreadWithScroll(int index) {
        preferences.edit().putInt(Constants.LAST_INDEX_Scroll, index).apply();
    }

    public int getLatestRead() {
        return preferences.getInt(Constants.LAST_INDEX, -1);
    }

    public int getLatestReadWithScroll() {
        return preferences.getInt(Constants.LAST_INDEX_Scroll, -1);
    }

    public boolean getPermissionState() {
        return preferences.getBoolean(Constants.PERMISSION_STATE, false);
    }

    public void setPermissionState(boolean state) {
        preferences.edit().putBoolean(Constants.PERMISSION_STATE, state).apply();
    }

    public boolean getNightModeState() {
        return preferences.getBoolean(Constants.NightMode_STATE, false);
    }

    public void setNightModeState(boolean state) {
        preferences.edit().putBoolean(Constants.NightMode_STATE, state).apply();
    }

    public int getBackColorState() {
        return preferences.getInt(Constants.BackColor_STATE, 0);
    }

    public void setBackColorState(int color) {
        preferences.edit().putInt(Constants.BackColor_STATE, color).apply();
    }

    public long getScore() {
        return preferences.getLong(Constants.SCORE, 0);
    }

    public void setScore(long score) {
        preferences.edit().putLong(Constants.SCORE, score).apply();
    }

    public String getUserName() {
        return preferences.getString(Constants.USER_NAME, null);
    }

    public void setUserName(String userName) {
        preferences.edit().putString(Constants.USER_NAME, userName).apply();
    }

    public String getUserUUID() {
        return preferences.getString(Constants.USER_UUID, null);
    }

    public void setUserUUID(String uuid) {
        preferences.edit().putString(Constants.USER_UUID, uuid).apply();
    }
}
