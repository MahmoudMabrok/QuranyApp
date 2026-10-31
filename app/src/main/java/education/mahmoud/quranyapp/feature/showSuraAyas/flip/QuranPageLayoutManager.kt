package education.mahmoud.quranyapp.feature.showSuraAyas.flip

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Horizontal pages that keep one neighbor laid out on each side (so the page
 * under the flip is already measured) and refuse to scroll more than one page
 * away from [anchorPosition] during a gesture.
 */
class QuranPageLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {

    /** Adapter position the current finger gesture started on, or [RecyclerView.NO_POSITION]. */
    var anchorPosition: Int = RecyclerView.NO_POSITION

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val anchor = anchorPosition
        val allowed = if (anchor == RecyclerView.NO_POSITION) {
            dx
        } else {
            val anchorView = findViewByPosition(anchor)
            if (anchorView == null || anchorView.width <= 0) {
                dx
            } else {
                BookFlipMath.clampPageDx(anchorView.left, dx, anchorView.width)
            }
        }
        return super.scrollHorizontallyBy(allowed, recycler, state)
    }

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        val page = width
        if (page <= 0 || extraLayoutSpace.size < 2) {
            super.calculateExtraLayoutSpace(state, extraLayoutSpace)
        } else {
            extraLayoutSpace[0] = page
            extraLayoutSpace[1] = page
        }
    }
}
