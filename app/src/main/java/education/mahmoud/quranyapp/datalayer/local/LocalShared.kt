package education.mahmoud.quranyapp.datalayer.local

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import education.mahmoud.quranyapp.feature.listen.playback.PlaybackSpeed
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

    /** Sura header style on reading pages, see SuraHeaderStyle (0 classic, 1 modern, 2 plain). */
    var mushafHeaderStyle: Int
        get() = preferences.getInt(MUSHAF_HEADER_STYLE, 0)
        set(style) {
            preferences.edit().putInt(MUSHAF_HEADER_STYLE, style).apply()
        }

    /** Draw the ornamental Mushaf frame around reading pages. */
    var mushafPageFrame: Boolean
        get() = preferences.getBoolean(MUSHAF_PAGE_FRAME, true)
        set(enabled) {
            preferences.edit().putBoolean(MUSHAF_PAGE_FRAME, enabled).apply()
        }

    /** Book-flip page turns in the reader. On unless the reader switches it off. */
    var bookFlipEnabled: Boolean
        get() = preferences.getBoolean(BOOK_FLIP_ENABLED, true)
        set(enabled) {
            preferences.edit().putBoolean(BOOK_FLIP_ENABLED, enabled).apply()
        }

    var score: Long
        get() = preferences.getLong(Constants.SCORE, 0)
        set(score) {
            preferences.edit().putLong(Constants.SCORE, score).apply()
        }

    var listenReciter: String?
        get() = preferences.getString(LISTEN_RECITER, null)
        set(id) {
            preferences.edit().putString(LISTEN_RECITER, id).apply()
        }

    /** Playback speed for every listening screen (stored as a float, e.g. 1.25). */
    var playbackSpeed: Float
        get() = PlaybackSpeed.normalize(preferences.getFloat(PLAYBACK_SPEED, PlaybackSpeed.DEFAULT))
        set(speed) {
            preferences.edit().putFloat(PLAYBACK_SPEED, PlaybackSpeed.normalize(speed)).apply()
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

    /**
     * Ayahs the reader highlighted as reference points, by their global ayah index (1..6236).
     * A copy is returned; use [setAyahHighlighted] to change it.
     */
    val highlightedAyahs: Set<Int>
        get() = preferences.getStringSet(HIGHLIGHTED_AYAHS, null)
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()

    fun setAyahHighlighted(ayahIndex: Int, highlighted: Boolean) {
        val current = highlightedAyahs.toMutableSet()
        if (highlighted) current.add(ayahIndex) else current.remove(ayahIndex)
        // always write a new set: SharedPreferences must not get back the instance it returned
        preferences.edit().putStringSet(HIGHLIGHTED_AYAHS, current.map { it.toString() }.toSet()).apply()
    }

    private companion object {
        const val HIGHLIGHTED_AYAHS = "highlighted_ayahs"
        const val LISTEN_RECITER = "listen_reciter"
        const val PLAYBACK_SPEED = "playback_speed"
        const val MUSHAF_HEADER_STYLE = "mushaf_header_style"
        const val MUSHAF_PAGE_FRAME = "mushaf_page_frame"
        const val BOOK_FLIP_ENABLED = "book_flip_enabled"
    }
}
