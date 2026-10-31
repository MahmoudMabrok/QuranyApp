package education.mahmoud.quranyapp.feature.showSuraAyas.flip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookFlipMathTest {

    @Test
    fun restingAndFullyOffPagesAreFlat() {
        val width = 1000
        for (left in intArrayOf(0, width, -width, width + 20, -width - 5)) {
            val pose = BookFlipMath.pose(left, width, density = 2.75f)
            assertEquals(0f, pose.translationX, 0.01f)
            assertEquals(0f, pose.rotationY, 0.01f)
            assertEquals(0f, pose.shade, 0.01f)
        }
    }

    @Test
    fun pageOnTheRightRotatesAroundTheRightEdgeAndDarkens() {
        val pose = BookFlipMath.pose(left = 500, pageWidth = 1000, density = 2f)
        // Pinned so the hinge stays on the right edge of the screen.
        assertEquals(-500f, pose.translationX, 0.01f)
        assertEquals(1000f, pose.pivotX, 0.01f)
        // Negative rotationY: the left edge recedes, it does not swing into the camera.
        assertEquals(-45f, pose.rotationY, 0.01f)
        assertEquals(BookFlipMath.MAX_SHADE * 0.5f, pose.shade, 0.01f)
        assertTrue(pose.needsLayer)
        assertEquals(BookFlipMath.cameraDistance(1000, 2f), pose.cameraDistance, 0.01f)
    }

    @Test
    fun pageOnTheLeftStaysFlatUnderneath() {
        val pose = BookFlipMath.pose(left = -500, pageWidth = 1000, density = 2f)
        assertEquals(500f, pose.translationX, 0.01f)
        assertEquals(0f, pose.rotationY, 0.01f)
        assertEquals(0f, pose.shade, 0.01f)
        assertTrue(pose.needsLayer)
    }

    @Test
    fun cameraDistanceGrowsWithThePageAndDensity() {
        assertEquals(16_000f, BookFlipMath.cameraDistance(1000, 2f), 0.01f)
        assertTrue(BookFlipMath.cameraDistance(2000, 3f) > BookFlipMath.cameraDistance(1000, 2f))
    }

    @Test
    fun clampKeepsTheAnchorWithinOnePage() {
        val width = 1000
        assertEquals(-100, BookFlipMath.clampPageDx(anchorLeft = 0, dx = -100, pageWidth = width))
        assertEquals(-width, BookFlipMath.clampPageDx(anchorLeft = 0, dx = -5_000, pageWidth = width))
        assertEquals(width, BookFlipMath.clampPageDx(anchorLeft = 0, dx = 5_000, pageWidth = width))
        assertEquals(-600, BookFlipMath.clampPageDx(anchorLeft = 400, dx = -700, pageWidth = width))
        assertEquals(600, BookFlipMath.clampPageDx(anchorLeft = -400, dx = 700, pageWidth = width))
        assertEquals(40, BookFlipMath.clampPageDx(anchorLeft = 10, dx = 40, pageWidth = 0))
    }

    @Test
    fun negativeVelocityXTurnsToTheNextPage() {
        // Finger moving right → RecyclerView velocityX < 0 → next page in this RTL reader.
        assertEquals(6, target(anchor = 5, offset = 0f, velocityX = -2_000))
        assertEquals(4, target(anchor = 5, offset = 0f, velocityX = 2_000))
        assertEquals(0, target(anchor = 0, offset = -0.9f, velocityX = 2_000))
        assertEquals(603, target(anchor = 603, offset = 0.9f, velocityX = -2_000))
    }

    @Test
    fun slowDragUsesAQuarterPageThreshold() {
        assertEquals(6, target(anchor = 5, offset = 0.25f, velocityX = 0))
        assertEquals(4, target(anchor = 5, offset = -0.25f, velocityX = 0))
        assertEquals(5, target(anchor = 5, offset = 0.24f, velocityX = -40))
        // A real fling wins even when the finger had started the other way.
        assertEquals(6, target(anchor = 5, offset = -0.4f, velocityX = -200))
    }

    private fun target(anchor: Int, offset: Float, velocityX: Int): Int {
        return BookFlipMath.targetIndex(
            anchor = anchor,
            offsetFraction = offset,
            velocityX = velocityX,
            minFlingVelocity = 100,
            lastIndex = 603,
        )
    }

    @Test
    fun drawingOrderPaintsTheHingePageLast() {
        val lefts = intArrayOf(100, -50, 0)
        assertEquals(1, BookFlipMath.drawingOrder(3, 0, lefts))
        assertEquals(2, BookFlipMath.drawingOrder(3, 1, lefts))
        assertEquals(0, BookFlipMath.drawingOrder(3, 2, lefts))
        val seen = (0 until 3).map { BookFlipMath.drawingOrder(3, it, lefts) }.toSet()
        assertEquals(setOf(0, 1, 2), seen)
        // Equal lefts stay in child-index order.
        val tied = intArrayOf(5, 5)
        assertEquals(0, BookFlipMath.drawingOrder(2, 0, tied))
        assertEquals(1, BookFlipMath.drawingOrder(2, 1, tied))
    }
}
