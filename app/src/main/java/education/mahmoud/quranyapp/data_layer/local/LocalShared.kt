package education.mahmoud.quranyapp.data_layer.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import education.mahmoud.quranyapp.utils.Constants

class LocalShared(context: Application) {
    private val preferences: SharedPreferences = context.getSharedPreferences("quran_data", Context.MODE_PRIVATE)

    fun addLatestread(index: Int) {
        preferences.edit().putInt(Constants.LAST_INDEX, index).apply()
    }

    fun addLatestreadWithScroll(index: Int) {
        preferences.edit().putInt(Constants.LAST_INDEX_Scroll, index).apply()
    }

    val latestRead: Int
        get() = preferences.getInt(Constants.LAST_INDEX, -1)

    val latestReadWithScroll: Int
        get() = preferences.getInt(Constants.LAST_INDEX_Scroll, -1)

    var permissionState: Boolean
        get() = preferences.getBoolean(Constants.PERMISSION_STATE, false)
        set(state) {
            preferences.edit().putBoolean(Constants.PERMISSION_STATE, state).apply()
        }

    var nightModeState: Boolean
        get() = preferences.getBoolean(Constants.NightMode_STATE, false)
        set(state) {
            preferences.edit().putBoolean(Constants.NightMode_STATE, state).apply()
        }

    var backColorState: Int
        get() = preferences.getInt(Constants.BackColor_STATE, 0)
        set(color) {
            preferences.edit().putInt(Constants.BackColor_STATE, color).apply()
        }

    var score: Long
        get() = preferences.getLong(Constants.SCORE, 0)
        set(score) {
            preferences.edit().putLong(Constants.SCORE, score).apply()
        }

    var userName: String?
        get() = preferences.getString(Constants.USER_NAME, "")
        set(userName) {
            preferences.edit().putString(Constants.USER_NAME, userName).apply()
        }

    var userUUID: String?
        get() = preferences.getString(Constants.USER_UUID, "")
        set(uuid) {
            preferences.edit().putString(Constants.USER_UUID, uuid).apply()
        }

}