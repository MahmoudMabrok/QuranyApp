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

    /**
     * Printed look of the text mushaf. See [education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook].
     * 0 keeps the header style and page frame above (the pre-1.6.0 reader).
     */
    var mushafLook: Int
        get() = preferences.getInt(MUSHAF_LOOK, 0)
        set(look) {
            preferences.edit().putInt(MUSHAF_LOOK, look).apply()
        }

    /** Juz / hizb / sajdah medallions. Off by default so existing frames stay as they were. */
    var mushafDivisionMarks: Boolean
        get() = preferences.getBoolean(MUSHAF_DIVISION_MARKS, false)
        set(enabled) {
            preferences.edit().putBoolean(MUSHAF_DIVISION_MARKS, enabled).apply()
        }

    /** Library page turn. 0 slide, 1 curl, 2 flip, 3 fade, 4 cover. See MushafPageTurn. */
    var mushafPageTurn: Int
        get() = preferences.getInt(MUSHAF_PAGE_TURN, 0)
        set(turn) {
            preferences.edit().putInt(MUSHAF_PAGE_TURN, turn).apply()
        }

    /** 0 = one page, 1 = two-page spread on wide screens. See MushafSpreadMode. */
    var mushafSpread: Int
        get() = preferences.getInt(MUSHAF_SPREAD, 0)
        set(spread) {
            preferences.edit().putInt(MUSHAF_SPREAD, spread).apply()
        }

    /** Colour tajweed rules in the text mushaf. Off by default. */
    var mushafTajweed: Boolean
        get() = preferences.getBoolean(MUSHAF_TAJWEED, false)
        set(enabled) {
            preferences.edit().putBoolean(MUSHAF_TAJWEED, enabled).apply()
        }

    /** Also colour natural madd. Stored independently; the reader applies it only while tajweed is on. */
    var mushafNaturalMadd: Boolean
        get() = preferences.getBoolean(MUSHAF_NATURAL_MADD, false)
        set(enabled) {
            preferences.edit().putBoolean(MUSHAF_NATURAL_MADD, enabled).apply()
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
        const val MUSHAF_LOOK = "mushaf_look"
        const val MUSHAF_DIVISION_MARKS = "mushaf_division_marks"
        const val MUSHAF_PAGE_TURN = "mushaf_page_turn"
        const val MUSHAF_SPREAD = "mushaf_spread"
        const val MUSHAF_TAJWEED = "mushaf_tajweed"
        const val MUSHAF_NATURAL_MADD = "mushaf_natural_madd"
    }
}
