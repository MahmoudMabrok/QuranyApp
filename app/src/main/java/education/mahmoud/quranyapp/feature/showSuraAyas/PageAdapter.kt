package education.mahmoud.quranyapp.feature.showSuraAyas

import android.view.LayoutInflater
import androidx.core.graphics.Insets
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.databinding.PageItemBinding
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafOptions

class PageAdapter(
    private val ayahsColor: Int,
    private val scrollColor: Int,
    val iOnClick: IOnClick? = null,
    private val pageShown: PageShown? = null,
    private val iBookmark: IBookmark? = null,
    private val mushafOptions: MushafOptions = MushafOptions(),
) : RecyclerView.Adapter<PageViewHolder>() {

    private val list: ArrayList<Page> = arrayListOf()

    /** System bar + cutout insets; each page pads itself so its paper colour goes behind the bars. */
    var systemInsets: Insets = Insets.NONE

    fun setPageList(newList: List<Page>) {
        val oldSize = list.size
        list.clear()
        list.addAll(newList)
        notifyItemRangeRemoved(0, oldSize)
        notifyItemRangeInserted(0, newList.size)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PageViewHolder {
        val binding =
            PageItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return PageViewHolder(binding, iBookmark)
    }

    override fun onBindViewHolder(holder: PageViewHolder, index: Int) {
        val page = list[index]
        holder.bind(page, ayahsColor, scrollColor, mushafOptions)
        holder.applyInsets(systemInsets)
    }

    private fun flipState(holder: PageViewHolder) {
        /*  vis = holder.topLinear?.visibility
          vis = if (vis == View.VISIBLE) View.INVISIBLE else View.VISIBLE
          holder.BottomLinear?.visibility = vis
          holder.topLinear?.visibility = vis*/
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewAttachedToWindow(holder: PageViewHolder) {
        super.onViewAttachedToWindow(holder)
        pageShown?.onDiplayed(holder.adapterPosition, holder)
    }

    override fun onViewRecycled(holder: PageViewHolder) {
        holder.resetPageTurn()
        super.onViewRecycled(holder)
    }

    fun getPage(pos: Int): Page {
        return list[pos]
    }

    interface PageShown {
        fun onDiplayed(pos: Int, holder: PageViewHolder)
    }

    interface IBookmark {
        fun onBookmarkClicked(item: Page)
    }

    companion object {
        private const val TAG = "PageAdapter"
    }
}
