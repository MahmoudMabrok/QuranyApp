package education.mahmoud.quranyapp.feature.ayahs_search

import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.UiFont

class SearchResultsAdapter : RecyclerView.Adapter<SearchResultsAdapter.Holder>() {

    private var list: MutableList<AyahItem> = ArrayList()
    private var wordSearched: String = ""
    private var iSearchItemClick: ISearchItemClick? = null

    fun setAyahItemList(newList: List<AyahItem>?, word: String?) {
        wordSearched = word ?: ""
        list = ArrayList(newList.orEmpty())
        notifyDataSetChanged()
    }

    fun setiSearchItemClick(iSearchItemClick: ISearchItemClick?) {
        this.iSearchItemClick = iSearchItemClick
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun getList(): List<AyahItem> = list

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_item, viewGroup, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, i: Int) {
        val item = list[i]
        val context = holder.itemView.context
        val text = item.textClean.ifEmpty { item.text }
        holder.tvSearchResult.setText(highlight(text, holder), TextView.BufferType.SPANNABLE)
        holder.tvSearchAyahNum.text = context.getString(
            R.string.search_ayah_num,
            NumberHelper.getArabicNumber(item.ayahInSurahIndex),
        )
        holder.tvSearchSuraName.text = Data.SURA_NAMES.getOrNull(item.surahIndex - 1).orEmpty()
        holder.tvSearchPage.text = context.getString(
            R.string.search_page_num,
            NumberHelper.getArabicNumber(item.pageNum),
        )
    }

    private fun highlight(text: String, holder: Holder): CharSequence {
        // colour spans only: a bold span splits the Arabic shaping run (the match detaches
        // from its word, e.g. "ب الرحمن") and Amiri has no bold face, so lines overflow
        val ranges = SearchText.matchRanges(text, wordSearched)
        if (ranges.isEmpty()) return text
        val context = holder.itemView.context
        val bg = ContextCompat.getColor(context, R.color.search_highlight_bg)
        val fg = ContextCompat.getColor(context, R.color.search_highlight_fg)
        return SpannableString(text).apply {
            ranges.forEach { r ->
                val end = r.last + 1
                setSpan(BackgroundColorSpan(bg), r.first, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(ForegroundColorSpan(fg), r.first, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun getItemIndex(ayahItem: AyahItem?): Int = list.indexOf(ayahItem)

    fun updateItem(ayahItem: AyahItem, itemIndex: Int) {
        list[itemIndex] = ayahItem
        notifyItemChanged(itemIndex)
    }

    interface ISearchItemClick {
        fun onSearchItemClick(item: AyahItem?)
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchSuraName: TextView = itemView.findViewById(R.id.tvSearchSuraName)
        val tvSearchAyahNum: TextView = itemView.findViewById(R.id.tvSearchAyahNum)
        val tvSearchPage: TextView = itemView.findViewById(R.id.tvSearchPage)
        val tvSearchResult: TextView = itemView.findViewById(R.id.tvSearchResult)

        init {
            UiFont.applyBold(tvSearchSuraName)
            UiFont.apply(tvSearchAyahNum, tvSearchPage)
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) iSearchItemClick?.onSearchItemClick(list[pos])
            }
        }
    }
}
