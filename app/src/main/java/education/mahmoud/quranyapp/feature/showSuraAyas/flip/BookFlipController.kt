package education.mahmoud.quranyapp.feature.showSuraAyas.flip

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.showSuraAyas.PageViewHolder
import kotlin.math.abs

/**
 * Attaches the book-flip to the reading [RecyclerView].
 *
 * The snap helper is registered before [scrollListener] on purpose: on idle it
 * may start the settle scroll (and flip the state to SETTLING) before we clear
 * the gesture anchor or record the page that landed.
 */
class BookFlipController(
    private val onPageSettled: (Int) -> Unit,
) {
    private var recyclerView: RecyclerView? = null
    private var layoutManager: QuranPageLayoutManager? = null
    private var applying = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
            val lm = layoutManager ?: return
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING && lm.anchorPosition == RecyclerView.NO_POSITION) {
                lm.anchorPosition = nearestPage(maxDistance = null)
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                // A snap started from an earlier listener keeps the anchor until that scroll ends.
                if (rv.scrollState != RecyclerView.SCROLL_STATE_IDLE) return
                lm.anchorPosition = RecyclerView.NO_POSITION
                applyTransforms()
                val settled = nearestPage(maxDistance = SETTLE_SLOP_PX)
                if (settled != RecyclerView.NO_POSITION) onPageSettled(settled)
            } else {
                applyTransforms()
            }
        }

        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            applyTransforms()
        }
    }

    private val childAttachListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            val scrolling = recyclerView?.scrollState != RecyclerView.SCROLL_STATE_IDLE
            if (scrolling && view.layerType != View.LAYER_TYPE_HARDWARE) {
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
            clearTurn(view)
        }
    }

    fun attach(rv: RecyclerView) {
        recyclerView = rv
        rv.itemAnimator = null
        rv.clipChildren = false
        rv.clipToPadding = false
        rv.overScrollMode = View.OVER_SCROLL_NEVER
        (rv.parent as? ViewGroup)?.clipChildren = false
        rv.layoutDirection = View.LAYOUT_DIRECTION_RTL
        rv.setItemViewCacheSize(PAGE_CACHE)
        val lm = QuranPageLayoutManager(rv.context)
        layoutManager = lm
        rv.layoutManager = lm
        // Snap helper first, so its idle callback runs before [scrollListener].
        BookPageSnapHelper().attachToRecyclerView(rv)
        rv.setChildDrawingOrderCallback { childCount, i ->
            val lefts = IntArray(childCount) { index -> rv.getChildAt(index)?.left ?: 0 }
            BookFlipMath.drawingOrder(childCount, i, lefts)
        }
        rv.addOnScrollListener(scrollListener)
        rv.addOnChildAttachStateChangeListener(childAttachListener)
        rv.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyTransforms()
        }
    }

    private fun applyTransforms() {
        val rv = recyclerView ?: return
        if (applying) return
        applying = true
        try {
            val scrolling = rv.scrollState != RecyclerView.SCROLL_STATE_IDLE
            val density = rv.resources.displayMetrics.density
            for (i in 0 until rv.childCount) {
                val child = rv.getChildAt(i) ?: continue
                applyTo(rv, child, density, scrolling)
            }
        } finally {
            applying = false
        }
    }

    private fun applyTo(rv: RecyclerView, child: View, density: Float, scrolling: Boolean) {
        // Once the gesture has settled, leave every child untransformed so a page
        // that is one pixel off its slot cannot stay hinged over the page beneath it.
        val pose = if (scrolling) BookFlipMath.pose(child.left, child.width, density) else BookFlipMath.PagePose.REST
        if (pose.cameraDistance > 0f) child.cameraDistance = pose.cameraDistance
        if (pose.rotationY != 0f) {
            child.pivotX = pose.pivotX
            child.pivotY = (child.height / 2).toFloat()
        }
        child.rotationY = pose.rotationY
        child.translationX = pose.translationX
        if (child.clipToOutline) child.clipToOutline = false
        (rv.findContainingViewHolder(child) as? PageViewHolder)?.setPageTurnShade(pose.shade)
        val want = if (scrolling && pose.needsLayer) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_NONE
        if (child.layerType != want) child.setLayerType(want, null)
    }

    private fun nearestPage(maxDistance: Int?): Int {
        val lm = layoutManager ?: return RecyclerView.NO_POSITION
        var bestPos = RecyclerView.NO_POSITION
        var bestDist = Int.MAX_VALUE
        for (i in 0 until lm.childCount) {
            val child = lm.getChildAt(i) ?: continue
            val pos = lm.getPosition(child)
            if (pos == RecyclerView.NO_POSITION) continue
            val dist = abs(child.left)
            if (dist < bestDist) {
                bestDist = dist
                bestPos = pos
            }
        }
        val within = maxDistance == null || bestDist <= maxDistance
        return if (bestPos != RecyclerView.NO_POSITION && within) bestPos else RecyclerView.NO_POSITION
    }

    private fun clearTurn(view: View) {
        view.translationX = 0f
        view.rotationY = 0f
        if (view.layerType != View.LAYER_TYPE_NONE) view.setLayerType(View.LAYER_TYPE_NONE, null)
        val holder = recyclerView?.findContainingViewHolder(view) as? PageViewHolder
        if (holder != null) {
            holder.setPageTurnShade(0f)
        } else {
            view.findViewById<View>(R.id.pageTurnShade)?.let { shade ->
                shade.alpha = 0f
                shade.visibility = View.INVISIBLE
            }
        }
    }

    private companion object {
        /** A page counts as landed when its layout edge is within a few pixels of rest. */
        const val SETTLE_SLOP_PX = 4
        const val PAGE_CACHE = 4
    }
}
