package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import android.content.Context

/**
 * Which page renderer the reader uses. On by default: pages are drawn as text by
 * mushaf-text. The old renderer stays reachable from the reader's "old view" button,
 * which switches this off.
 */
object MushafTextPrefs {
    private const val FILE = "mushaf_text_prefs"
    private const val KEY_ENABLED = "mushaf_text_enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
