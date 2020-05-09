package education.mahmoud.quranyapp.feature.showSuraAyas

import android.os.Build
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.Util
import education.mahmoud.quranyapp.utils.log

class PageAdapter(var ayahsColor: Int, var scrollColor: Int) : RecyclerView.Adapter<PageAdapter.Holder>() {
    var vis = View.INVISIBLE

    var list: ArrayList<Page> = arrayListOf()

    private var iOnClick: IOnClick? = null
    private var pageShown: PageShown? = null
    private var iBookmark: IBookmark? = null
    fun setPageShown(pageShown: PageShown?) {
        this.pageShown = pageShown
    }

    fun setiBookmark(iBookmark: IBookmark?) {
        this.iBookmark = iBookmark
    }

    fun setiOnClick(iOnClick: IOnClick?) {
        this.iOnClick = iOnClick
    }

    fun setPageList(newList: List<Page>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.page_item, viewGroup, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, index: Int) {
        val item = list[index]
        // set Colors
        holder.tvAyahs?.setTextColor(ayahsColor)
        holder.tvJuz?.setTextColor(ayahsColor)
        holder.tvPageNumShowAyahs?.setTextColor(ayahsColor)
        holder.tvSurahName?.setTextColor(ayahsColor)
        holder.mainLayout?.setBackgroundColor(scrollColor)

        val ayah = item.getText { return@getText getSuraNameFromIndex(it) }
        "ayah $ayah".log()

        val suraName = getSuraNameFromIndex(item.ayahItems[0].surahIndex)

        holder.tvAyahs?.setText(Util.getSpannable(ayah), TextView.BufferType.SPANNABLE)

        // text justifivation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvAyahs?.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
        }
        // top - bottom details
        holder.tvPageNumShowAyahs?.text = item.pageNum.toString()
        holder.tvSurahName?.text = suraName
        holder.tvJuz?.text = item.juz.toString()
        // <editor-fold desc="listeners">
        holder.imBookmark?.setOnClickListener { iBookmark?.onBookmarkClicked(item) }
    }

    private fun flipState(holder: Holder) {
        /*  vis = holder.topLinear?.visibility
          vis = if (vis == View.VISIBLE) View.INVISIBLE else View.VISIBLE
          holder.BottomLinear?.visibility = vis
          holder.topLinear?.visibility = vis*/
    }

    /**
     * @param surahIndex in quran
     * @return
     */
    private fun getSuraNameFromIndex(surahIndex: Int): String {
        return Data.SURA_NAMES[surahIndex - 1]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onViewAttachedToWindow(holder: Holder) {
        super.onViewAttachedToWindow(holder)
        pageShown?.onDiplayed(holder.adapterPosition, holder)
    }

    fun getPage(pos: Int): Page {
        return list[pos]
    }

    interface PageShown {
        fun onDiplayed(pos: Int, holder: Holder)
    }

    interface IBookmark {
        fun onBookmarkClicked(item: Page)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvAyahs: TextView? = itemView.findViewById(R.id.tvAyahs)
        var tvSurahName: TextView? = itemView.findViewById(R.id.tvSurahName)
        var tvJuz: TextView? = itemView.findViewById(R.id.tvJuz)
        var tvPageNumShowAyahs: TextView? = itemView.findViewById(R.id.tvPageNumShowAyahs)
        var imBookmark: ImageView? = itemView.findViewById(R.id.imBookmark)

        var topLinear: LinearLayout? = itemView.findViewById(R.id.topLinear)
        var mainLayout: ConstraintLayout? = itemView.findViewById(R.id.ayahsLayout)
    }

    companion object {
        private const val TAG = "PageAdapter"
    }
}