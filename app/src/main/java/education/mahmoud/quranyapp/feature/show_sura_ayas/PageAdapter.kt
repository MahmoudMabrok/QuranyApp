package education.mahmoud.quranyapp.feature.show_sura_ayas

import android.os.Build
import android.os.CountDownTimer
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.Util
import java.text.MessageFormat

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
        holder.topLinear?.visibility = View.VISIBLE
        holder.BottomLinear?.visibility = View.VISIBLE
        // set Colors
        holder.tvAyahs?.setTextColor(ayahsColor)
        holder.scAyahsText?.setBackgroundColor(scrollColor)
        // <editor-fold desc="Create Text">
        val builder = StringBuilder()
        var aya: String
        var suraName = ""
        suraName = getSuraNameFromIndex(item.ayahItems[0].surahIndex)
        var tempSuraName: String
        var isFirst = true
        for (ayahItem in item.ayahItems) {
            aya = ayahItem.text
            // add sura name
            if (ayahItem.ayahInSurahIndex == 1) {
                tempSuraName = getSuraNameFromIndex(ayahItem.surahIndex)
                if (isFirst) { // handle first name in page that not need a previous new line
                    builder.append(tempSuraName + "\n")
                } else {
                    builder.append("\n" + tempSuraName + "\n")
                }
                // AlFatiha(index = 1 ) has a Basmallah in first ayah.
                if (ayahItem.surahIndex != 1) {
                    var pos = aya.indexOf("ٱلرَّحِيم")
                    Log.d(TAG, "onBindViewHolder: pos $pos")
                    pos += "ٱلرَّحِيم".length
                    Log.d(TAG, "onBindViewHolder: last text after bsmallah " + aya.substring(pos))
                    // insert  البسملة
                    builder.append(aya.substring(0, pos + 1)) // +1 as substring upper bound is excluded
                    builder.append("\n")
                    // cute ayah
                    aya = aya.substring(pos + 1) // +1 to start with new character after البسملة
                }
            }
            isFirst = false
            builder.append(MessageFormat.format("{0} ﴿ {1} ﴾ ", aya, ayahItem.ayahInSurahIndex))
        }
        // </editor-fold>
        holder.tvAyahs?.setText(Util.getSpannable(builder.toString()), TextView.BufferType.SPANNABLE)
        // text justifivation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvAyahs?.justificationMode = Layout.JUSTIFICATION_MODE_NONE
        }
        // top - bottom details
        holder.tvPageNumShowAyahs?.text = item.pageNum.toString()
        holder.tvSurahName?.text = suraName
        holder.tvJuz?.text = item.juz.toString()
        // <editor-fold desc="listeners">
        holder.imBookmark?.setOnClickListener { iBookmark?.onBookmarkClicked(item) }
        /*holder.ayahsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: frameLayout");
                flipState(holder);
            }
        });
        */holder.btnNext?.setOnClickListener { iOnClick?.onClick(holder.adapterPosition) }
        holder.btnPrev?.setOnClickListener {
            iOnClick?.onClick(holder.adapterPosition - 2) // there will be update by one
        }
        // </editor-fold>
        // <editor-fold desc="configure next/prev buttons">
        if (index == 0) {
            holder.btnNext?.visibility = View.VISIBLE
            holder.btnPrev?.visibility = View.INVISIBLE
        } else if (index == 603) {
            holder.btnNext?.visibility = View.INVISIBLE
            holder.btnPrev?.visibility = View.VISIBLE
        } else {
            holder.btnNext?.visibility = View.VISIBLE
            holder.btnPrev?.visibility = View.VISIBLE
        }
        // </editor-fold>

        holder.tvAyahs?.setOnClickListener { Log.d(TAG, "onClick: ") }
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
        // <editor-fold desc="timer to hide">
        object : CountDownTimer(50, 100) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                vis = View.VISIBLE
                holder.BottomLinear?.visibility = vis
                holder.topLinear?.visibility = vis
            }
        }.start()
        // </editor-fold>
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
        @JvmField
        @BindView(R.id.tvAyahs)
        var tvAyahs: TextView? = null
        @JvmField
        @BindView(R.id.sc_ayahs_text)
        var scAyahsText: ScrollView? = null
        @JvmField
        @BindView(R.id.tvSurahName)
        var tvSurahName: TextView? = null
        @JvmField
        @BindView(R.id.tvJuz)
        var tvJuz: TextView? = null
        @JvmField
        @BindView(R.id.imBookmark)
        var imBookmark: ImageView? = null
        @JvmField
        @BindView(R.id.topLinear)
        var topLinear: LinearLayout? = null
        @JvmField
        @BindView(R.id.btnNext)
        var btnNext: ImageButton? = null
        @JvmField
        @BindView(R.id.tvPageNumShowAyahs)
        var tvPageNumShowAyahs: TextView? = null
        @JvmField
        @BindView(R.id.btnPrev)
        var btnPrev: ImageButton? = null
        @JvmField
        @BindView(R.id.BottomLinear)
        var BottomLinear: LinearLayout? = null
        @JvmField
        @BindView(R.id.ayahsLayout)
        var ayahsLayout: FrameLayout? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    companion object {
        private const val TAG = "PageAdapter"
    }
}