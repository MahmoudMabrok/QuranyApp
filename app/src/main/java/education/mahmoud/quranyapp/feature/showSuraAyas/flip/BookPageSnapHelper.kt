package education.mahmoud.quranyapp.feature.showSuraAyas.flip

import android.util.DisplayMetrics
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Snaps to the page [BookFlipMath.targetIndex] chooses — one step from the
 * gesture anchor — and settles a full turn in about half a second.
 *
 * [createScroller] replaces [PagerSnapHelper]'s fling scroller, which caps the
 * animation at 100ms. The snap-to-center math of the parent is kept.
 */
class BookPageSnapHelper : PagerSnapHelper() {

    private val decelerate = DecelerateInterpolator()
    private var bound: RecyclerView? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        bound = recyclerView
        super.attachToRecyclerView(recyclerView)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val linear = layoutManager as? QuranPageLayoutManager ?: return super.findSnapView(layoutManager)
        val anchor = linear.anchorPosition
        if (anchor == RecyclerView.NO_POSITION) return super.findSnapView(layoutManager)
        val anchorView = linear.findViewByPosition(anchor) ?: return super.findSnapView(layoutManager)
        val width = anchorView.width
        if (width <= 0) return anchorView
        val fraction = anchorView.left.toFloat() / width.toFloat()
        val target = BookFlipMath.targetIndex(
            anchor = anchor,
            offsetFraction = fraction,
            velocityX = 0,
            minFlingVelocity = Int.MAX_VALUE,
            lastIndex = linear.itemCount - 1,
        )
        return linear.findViewByPosition(target) ?: super.findSnapView(layoutManager)
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int,
    ): Int {
        val linear = layoutManager as? QuranPageLayoutManager ?: return RecyclerView.NO_POSITION
        if (linear.itemCount == 0) return RecyclerView.NO_POSITION
        val anchor = linear.anchorPosition.takeIf { it != RecyclerView.NO_POSITION }
            ?: linear.findFirstCompletelyVisibleItemPosition().takeIf { it != RecyclerView.NO_POSITION }
            ?: linear.findFirstVisibleItemPosition()
        if (anchor == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION
        val anchorView = linear.findViewByPosition(anchor)
        val fraction = if (anchorView != null && anchorView.width > 0) {
            anchorView.left.toFloat() / anchorView.width
        } else {
            0f
        }
        val minFling = bound?.minFlingVelocity ?: 0
        return BookFlipMath.targetIndex(anchor, fraction, velocityX, minFling, linear.itemCount - 1)
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): RecyclerView.SmoothScroller? {
        val rv = bound ?: return null
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) return null
        return object : LinearSmoothScroller(rv.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
                val manager = rv.layoutManager ?: return
                val snap = calculateDistanceToFinalSnap(manager, targetView) ?: return
                val dx = snap[0]
                val dy = snap[1]
                val time = calculateTimeForDeceleration(maxOf(abs(dx), abs(dy)))
                if (time > 0) action.update(dx, dy, time, decelerate)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLIS_PER_INCH / displayMetrics.densityDpi
            }

            override fun calculateTimeForDeceleration(dx: Int): Int {
                val distance = abs(dx)
                if (distance == 0) return 0
                val ms = super.calculateTimeForDeceleration(dx)
                val width = rv.width
                return if (width > 0 && distance > width / FULL_TURN_PORTION) {
                    ms.coerceIn(FULL_TURN_MIN_MS, FULL_TURN_MAX_MS)
                } else {
                    ms.coerceAtMost(SHORT_SETTLE_MAX_MS)
                }
            }
        }
    }

    private companion object {
        const val MILLIS_PER_INCH = 140f
        const val FULL_TURN_PORTION = 3
        const val FULL_TURN_MIN_MS = 320
        const val FULL_TURN_MAX_MS = 480
        const val SHORT_SETTLE_MAX_MS = 220
    }
}
