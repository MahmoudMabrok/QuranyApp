package education.mahmoud.quranyapp.utils

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import education.mahmoud.quranyapp.R

/**
 * Edge-to-edge helpers. targetSdk 35+ forces edge-to-edge on Android 15+, so every screen
 * draws behind the status and navigation bars; we enable it on all versions for one look
 * and pad the content with the system bar + display cutout insets.
 */
object EdgeToEdge {

    /** Insets every screen must keep its content out of: status/nav/gesture bars and cutouts. */
    @JvmStatic
    val SAFE_TYPES: Int = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()

    // same scrims androidx.activity uses for 3-button navigation
    private val LIGHT_SCRIM = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
    private val DARK_SCRIM = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

    /**
     * Enables edge-to-edge with transparent bars.
     * @param darkStatusBarBackground true when the content behind the status bar is dark/coloured,
     * so the status bar icons are white. null = the themed app bar: teal in the light theme
     * (white icons), light green in the dark theme (dark icons).
     * @param nightContent whether the screen content is dark (night mode); picks the nav bar icon colour.
     * null follows the system dark theme.
     */
    @JvmStatic
    @JvmOverloads
    fun enable(activity: ComponentActivity, darkStatusBarBackground: Boolean? = null, nightContent: Boolean? = null) {
        val night = nightContent ?: isSystemNight(activity)
        val darkBehindStatusBar = darkStatusBarBackground ?: !isSystemNight(activity)
        activity.enableEdgeToEdge(
            statusBarStyle = if (darkBehindStatusBar) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
            navigationBarStyle = if (night) SystemBarStyle.dark(DARK_SCRIM) else SystemBarStyle.light(LIGHT_SCRIM, DARK_SCRIM),
        )
    }

    /** Light status bar = dark icons (for light content behind the status bar). */
    @JvmStatic
    fun setStatusBarIconsDark(activity: Activity, dark: Boolean) {
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars = dark
    }

    @JvmStatic
    fun isSystemNight(activity: Activity): Boolean =
        (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /**
     * Pads [view] by the system bar + cutout insets on the chosen sides, on top of its own
     * padding from the layout. The view background still extends behind the bars.
     * Insets are not consumed, so sibling views get them too.
     */
    @JvmStatic
    @JvmOverloads
    fun applyPadding(
        view: View,
        left: Boolean = true,
        top: Boolean = true,
        right: Boolean = true,
        bottom: Boolean = true,
        types: Int = SAFE_TYPES,
    ) {
        val initial = (view.getTag(R.id.tag_initial_padding) as? Insets) ?: Insets.of(
            view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom,
        ).also { view.setTag(R.id.tag_initial_padding, it) }
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val bars = windowInsets.getInsets(types)
            v.setPadding(
                initial.left + if (left) bars.left else 0,
                initial.top + if (top) bars.top else 0,
                initial.right + if (right) bars.right else 0,
                initial.bottom + if (bottom) bars.bottom else 0,
            )
            windowInsets
        }
        requestApplyInsetsWhenAttached(view)
    }

    /**
     * Applies padding to the root of an activity content view (all sides), including the
     * keyboard, since adjustResize no longer resizes edge-to-edge windows.
     */
    @JvmStatic
    fun applyPaddingToContent(activity: Activity) {
        val content = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
        val root = content?.getChildAt(0) ?: return
        applyPadding(root, types = SAFE_TYPES or WindowInsetsCompat.Type.ime())
    }

    /**
     * Gives the same insets to every child of [parent]. Needed when a child (e.g. a ComposeView,
     * which consumes insets) would otherwise hide them from overlay siblings.
     */
    @JvmStatic
    fun dispatchToAllChildren(parent: android.view.ViewGroup) {
        ViewCompat.setOnApplyWindowInsetsListener(parent) { _, insets ->
            for (i in 0 until parent.childCount) ViewCompat.dispatchApplyWindowInsets(parent.getChildAt(i), insets)
            WindowInsetsCompat.CONSUMED
        }
    }

    @JvmStatic
    fun requestApplyInsetsWhenAttached(view: View) {
        if (view.isAttachedToWindow) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(v)
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            })
        }
    }
}
