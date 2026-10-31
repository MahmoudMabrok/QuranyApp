package education.mahmoud.quranyapp.feature.showSuraAyas.flip

import kotlin.comparisons.compareBy
import kotlin.math.abs

/**
 * Pure geometry for the mushaf page turn.
 *
 * The reader is RTL: finger movement toward the right brings the next page in
 * from the left. Arabic bindings sit on the right, so the hinge stays on the
 * right edge of the screen (the same side for every page — alternating it with
 * the odd/even frame would make the spine jump). [PagePose.rotationY] is
 * negative so the free (left) edge recedes into the screen instead of swinging
 * toward the camera, which is what clips a full-bleed page.
 *
 * RecyclerView reports fling [velocityX] already negated: negative when the
 * finger moves right. That direction is the next page.
 */
object BookFlipMath {

    /** How far a slow drag must travel, as a fraction of the page, before it commits. */
    const val SWIPE_FRACTION = 0.25f

    const val MAX_ROTATION_DEGREES = 90f

    /** Black overlay strength on the turning page at the end of the turn. */
    const val MAX_SHADE = 0.92f

    /**
     * Camera distance passed to [android.view.View.setCameraDistance], which
     * multiplies by density itself. Scaled to the page width so a full-screen
     * 90° turn stays off the near clip plane without going orthographic.
     */
    const val CAMERA_PAGE_MULTIPLIER = 8f
    const val MIN_CAMERA_DISTANCE = 8_000f

    data class PagePose(
        val translationX: Float,
        val rotationY: Float,
        val pivotX: Float,
        val shade: Float,
        val cameraDistance: Float,
    ) {
        val needsLayer: Boolean get() = translationX != 0f || rotationY != 0f

        companion object {
            val REST = PagePose(0f, 0f, 0f, 0f, 0f)
        }
    }

    fun cameraDistance(pageWidth: Int, density: Float): Float {
        val base = (pageWidth.coerceAtLeast(1) * CAMERA_PAGE_MULTIPLIER).coerceAtLeast(MIN_CAMERA_DISTANCE)
        return base * density.coerceAtLeast(1f)
    }

    /**
     * @param left layout left of the child, in pixels. Positive means the page
     * sits to the right of its resting place (the turning page, on the hinge).
     */
    fun pose(left: Int, pageWidth: Int, density: Float): PagePose {
        if (pageWidth <= 0) return PagePose.REST
        val fraction = left.toFloat() / pageWidth.toFloat()
        // Fully off to either side: leave it where the layout put it.
        if (fraction <= -1f || fraction >= 1f || fraction == 0f) return PagePose.REST
        val turning = fraction > 0f
        return PagePose(
            translationX = -left.toFloat(),
            rotationY = if (turning) -MAX_ROTATION_DEGREES * fraction else 0f,
            pivotX = pageWidth.toFloat(),
            shade = if (turning) MAX_SHADE * fraction else 0f,
            cameraDistance = cameraDistance(pageWidth, density),
        )
    }

    /**
     * Limits a drag/fling so the anchor page's left stays inside one page of rest.
     * Positive [dx] is the RecyclerView delta (children move left, [anchorLeft] decreases).
     */
    fun clampPageDx(anchorLeft: Int, dx: Int, pageWidth: Int): Int {
        if (pageWidth <= 0) return dx
        val newLeft = anchorLeft.toLong() - dx.toLong()
        val clampedLeft = newLeft.coerceIn(-pageWidth.toLong(), pageWidth.toLong())
        return (anchorLeft.toLong() - clampedLeft).toInt()
    }

    /**
     * Adapter index after the gesture. A fling wins over the distance already
     * dragged; a slow release uses [SWIPE_FRACTION].
     */
    fun targetIndex(
        anchor: Int,
        offsetFraction: Float,
        velocityX: Int,
        minFlingVelocity: Int,
        lastIndex: Int,
    ): Int {
        if (lastIndex < 0) return 0
        val safeAnchor = anchor.coerceIn(0, lastIndex)
        val flung = minFlingVelocity > 0 && abs(velocityX) > minFlingVelocity
        val delta = if (flung) {
            if (velocityX < 0) 1 else -1
        } else {
            when {
                offsetFraction >= SWIPE_FRACTION -> 1
                offsetFraction <= -SWIPE_FRACTION -> -1
                else -> 0
            }
        }
        return (safeAnchor + delta).coerceIn(0, lastIndex)
    }

    /**
     * Child drawing order, bottom to top: smaller layout-left first, so the
     * page on the hinge (positive left) is painted above the page it reveals.
     * Returns a permutation of `0 until childCount`.
     */
    fun drawingOrder(childCount: Int, drawingPosition: Int, lefts: IntArray): Int {
        if (childCount <= 1) return 0
        val position = drawingPosition.coerceIn(0, childCount - 1)
        val indices = Array(childCount) { it }
        indices.sortWith(
            compareBy(
                { index -> if (index < lefts.size) lefts[index] else 0 },
                { index -> index },
            ),
        )
        return indices[position]
    }
}
