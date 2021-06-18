package education.mahmoud.quranyapp.feature.ayahs_search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.Util
import java.util.*

class SearchResultsAdapter : RecyclerView.Adapter<SearchResultsAdapter.Holder>() {
  var iOnPlay: IOnPlay? = null
  var iOpenAyahInPage: IOpenAyahInPage? = null
  private var list: MutableList<AyahItem>
  private var wordSearched: String? = null
  fun setiOnPlay(iOnPlay: IOnPlay?) {
    this.iOnPlay = iOnPlay
  }

  fun setiOpenAyahInPage(iOpenAyahInPage: IOpenAyahInPage?) {
    this.iOpenAyahInPage = iOpenAyahInPage
  }

  fun setAyahItemList(newList: List<AyahItem>?, word: String?) {
    wordSearched = word
    list = ArrayList(newList!!)
    notifyDataSetChanged()
  }

  private var iSearchItemClick: ISearchItemClick? = null

  fun setiSearchItemClick(iSearchItemClick: ISearchItemClick?) {
    this.iSearchItemClick = iSearchItemClick
  }

  fun clear() {
    list.clear()
    notifyDataSetChanged()
  }

  fun getList(): List<AyahItem> {
    return list
  }

  override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
    val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_item, viewGroup, false)
    return Holder(view)
  }

  override fun onBindViewHolder(holder: Holder, i: Int) {
    val (_, surahIndex, _, _, _, _, _, ayahInSurahIndex, text, textClean) = list[i]
    val ayahSpanned = Util.getSpanOfText(textClean, wordSearched)
    holder.tvSearchResult!!.setText(ayahSpanned, TextView.BufferType.SPANNABLE)
    holder.tvSearchAyahNum!!.text = "" + ayahInSurahIndex
    holder.tvSearchSuraName!!.text = "" + Data.SURA_NAMES[surahIndex - 1]
    holder.btnShowTashkeel!!.setOnClickListener { holder.tvSearchResult!!.text = text }

    /*
        holder.tvSearchResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iOpenAyahInPage.openPage(item.getPageNum());
            }
        });
        */
  }

  override fun getItemCount(): Int {
    return list.size
  }

  fun getItemIndex(ayahItem: AyahItem?): Int {
    return list.indexOf(ayahItem)
  }

  fun updateItem(ayahItem: AyahItem, itemIndex: Int) {
    list[itemIndex] = ayahItem
    notifyDataSetChanged()
  }

  interface IOnPlay {
    fun onPlayClick(item: AyahItem?)
  }

  interface IOpenAyahInPage {
    fun openPage(index: Int)
  }

  interface ISearchItemClick {
    fun onSearchItemClick(item: AyahItem?)
  }

  inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val tvSearchSuraName:TextView =  itemView.findViewById(R.id.tvSearchSuraName)
    val tvSearchAyahNum:TextView =  itemView.findViewById(R.id.tvSearchAyahNum)
    val btnPlayInSearch:Button =  itemView.findViewById(R.id.btnPlayInSearch)
    val btnShowTafseer:Button =  itemView.findViewById(R.id.btnShowTafseer)
    val btnShowTashkeel:Button =  itemView.findViewById(R.id.btnShowTashkeel)
    val tvSearchResult:TextView =  itemView.findViewById(R.id.tvSearchResult)

    init {
      btnPlayInSearch?.setOnClickListener { iOnPlay?.onPlayClick(list[adapterPosition]) }
      itemView.setOnClickListener { iSearchItemClick?.onSearchItemClick(list[adapterPosition]) }
    }

  }

  init {
    list = ArrayList()
  }
}
