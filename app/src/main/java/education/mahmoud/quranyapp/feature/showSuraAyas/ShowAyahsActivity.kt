package education.mahmoud.quranyapp.feature.showSuraAyas

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArraySet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearSnapHelper
import butterknife.ButterKnife
import education.mahmoud.quranyapp.App
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.databinding.ActivityShowAyahsBinding
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.datalayer.local.room.ReadLog
import education.mahmoud.quranyapp.feature.showSuraAyas.PageAdapter.IBookmark
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafOptions
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.DateOperation
import education.mahmoud.quranyapp.utils.EdgeToEdge
import education.mahmoud.quranyapp.utils.log
import education.mahmoud.quranyapp.utils.viewBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject
import java.util.Date

class ShowAyahsActivity : AppCompatActivity() {

    private val name = javaClass.simpleName

    private val model: AyahsViewModel by inject()
    var pageAdapter: PageAdapter? = null
    var pos = 0
    var pageList: List<Page> = listOf()
    var ayahsColor = 0
    var scrollorColor = 0
    private var lastpageShown = 1

    /**
     * list of pages num that contain start of Hizb Qurater
     */
    private val quarterStartsList = mutableListOf<Int>()

    /**
     * hold num of pages that read today
     * will be update(in db) with every exit from activity
     */
    private val pagesReadLogNumber = mutableSetOf<Int>()

    /**
     * hold current date used to retrieve pages and also with updating
     */
    private var currentDate: Long = 0

    /**
     * hold current date used to retrieve pages and also with updating
     */
    private lateinit var currentDateStr: String

    /**
     * hold current readLog item used to retrieve pages and also with updating
     */
    var readLog: ReadLog? = null

    private var toast: Toast? = null

    private val binding by viewBinding(ActivityShowAyahsBinding::inflate)

    private val bookmarkListener = object : IBookmark {
        override fun onBookmarkClicked(item: Page) {
            val bookmarkItem = BookmarkItem()
            bookmarkItem.timemills = Date().time
            // get ayah to retrieve info from it
            val ayahItem = item.ayhas[0]
            bookmarkItem.suraName = getSuraNameFromIndex(ayahItem.surahIndex)
            bookmarkItem.pageNum = item.pageNum
            Log.d(TAG, "onBookmarkClicked: " + bookmarkItem.pageNum)
            model.addBookmark(bookmarkItem)
            showMessage("Saved")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // edge-to-edge: page paper goes behind the bars; icons follow the page colour (night mode)
        val night = model.nightModeState
        EdgeToEdge.enable(this, darkStatusBarBackground = night, nightContent = night)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(EdgeToEdge.SAFE_TYPES)
            pageAdapter?.let { adapter ->
                if (adapter.systemInsets != bars) {
                    adapter.systemInsets = bars
                    val rv = binding.rvAyahsPages
                    for (i in 0 until rv.childCount) {
                        (rv.getChildViewHolder(rv.getChildAt(i)) as? PageViewHolder)?.applyInsets(bars)
                    }
                }
            }
            insets
        }
        ButterKnife.bind(this)

        pos = intent.getIntExtra(Constants.SURAH_INDEX, 1)
        "1$name   onCreate: $pos\"".log()
        pos = getStartPageFromIndex(pos)
        "2$name   onCreate: $pos\"".log()
        //region Description
        if (intent.hasExtra(Constants.SURAH_GO_INDEX)) {
            val surah = intent.getIntExtra(Constants.SURAH_GO_INDEX, 1)
            val ayah = intent.getIntExtra(Constants.AYAH_GO_INDEX, 1)
            Log.d(TAG, "onCreate: ayah  $ayah")
            pos = getPosFromSurahAndAyah(surah, ayah)
        } else if (intent.hasExtra(Constants.LAST_INDEX)) {
            pos = model.getLatestRead() // as it will be decreased
        } else if (intent.hasExtra(Constants.PAGE_INDEX)) { // case bookmark, go to by page
            pos = intent.getIntExtra(Constants.PAGE_INDEX, 1)
        } else if (intent.hasExtra(Constants.JUZ_INDEX)) {
            pos = intent.getIntExtra(Constants.JUZ_INDEX, 1)
            pos = getPageFromJuz(pos)
        }
        pos -= 1
        lastpageShown = pos + 1

        //endregion
        "$name   onCreate: $pos\"".log()
        initRV()

        // subscribe first; relayPages replays the cached pages if they are already loaded
        startObserving()
        (application as? App)?.persistanscePages()
    }

    private fun startObserving() {
        Log.d(TAG, "stratObserving:qw ${Thread.currentThread()}")
        (application as? App)?.relayPages
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.doAfterNext {
                hideLoading()
            }
            ?.subscribe(
                {
                    "data found ${it.size}".log()
                    pageAdapter?.setPageList(it)
                    binding.rvAyahsPages.scrollToPosition(pos)
                    // the page the reader opens on counts as read
                    onPageShown(pos)
                    "data pageAdapter ${pageAdapter?.itemCount} , ${Thread.currentThread()}".log()
                    foundState()
                },
                {
                    "error ${it.message}".log()
                }
            )
            ?.addTo(model.bg)
    }

    private fun hideLoading() {
        binding.group2.visibility = View.GONE
    }

    private fun addToReadLog(pageNum: Int) {
        if (pageNum in 1..LAST_PAGE) pagesReadLogNumber.add(pageNum)
    }

    /**
     * Called with the adapter position of the page that is now on screen: remembers it as the
     * latest read page and adds it to today's reading log. (This used to be wired through
     * PageAdapter.setPageShown, which was commented out, so the log stayed empty.)
     */
    private fun onPageShown(position: Int) {
        val adapter = pageAdapter ?: return
        if (position < 0 || position >= adapter.itemCount) return
        val pageNum = adapter.getPage(position).pageNum
        lastpageShown = pageNum
        addToReadLog(pageNum)
    }

    private fun getPosFromSurahAndAyah(surah: Int, ayah: Int): Int {
        return model.getPageFromSurahAndAyah(surah, ayah)
    }

    private fun getPageFromJuz(pos: Int): Int {
        return model.getPageFromJuz(pos)
    }

    private fun getStartPageFromIndex(pos: Int): Int {
        return model.getSuraStartpage(pos)
    }

    private fun initRV() {
        prepareColors()
        with(binding) {
            rvAyahsPages.setHasFixedSize(true)
            val mushafOptions = MushafOptions(
                headerStyle = SuraHeaderStyle.fromId(model.mushafHeaderStyle),
                pageFrame = model.mushafPageFrame,
                nightMode = model.nightModeState,
            )
            pageAdapter = PageAdapter(ayahsColor, scrollorColor, iBookmark = bookmarkListener, mushafOptions = mushafOptions)
            rvAyahsPages.adapter = pageAdapter
            rvAyahsPages.itemAnimator = DefaultItemAnimator()
            LinearSnapHelper().attachToRecyclerView(rvAyahsPages)
            rvAyahsPages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                    val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    var shown = lm.findFirstCompletelyVisibleItemPosition()
                    if (shown == RecyclerView.NO_POSITION) shown = lm.findFirstVisibleItemPosition()
                    onPageShown(shown)
                }
            })
            /*        pageAdapter.setPageShown(object : PageShown {
                        override fun onDiplayed(pos: Int, holder: PageAdapter.Holder) {
                            // items start from 0 increase 1 to get real page num,
                            // will be used in bookmark
                            lastpageShown = pos + 1
                            // add page to read log
                            addToReadLog(lastpageShown)
                            // calculate Hizb info.
                            val page = pageAdapter?.getPage(pos)
                            if (quraterSStart.contains(page.pageNum)) { // get last ayah to extract info from it
                                val ayahItem = page.ayhas[page.ayhas.size - 1]
                                var rub3Num = ayahItem.hizbQuarter
                                rub3Num-- // as first one must be 0
                                if (rub3Num % 8 == 0) {
                                    showMessage(getString(R.string.juz_to_display, ayahItem.juz))
                                } else if (rub3Num % 4 == 0) {
                                    showMessage(getString(R.string.hizb_to_display, rub3Num / 4))
                                } else {
                                    var part = rub3Num % 4
                                    part-- // 1/4 is first element which is 0
                                    val parts = resources.getStringArray(R.array.parts)
                                    showMessage(getString(R.string.part_to_display, parts[part], rub3Num / 4 + 1))
                                }
                            }
                        }
                    })*/

            // to preserver quran direction from right to left
            rvAyahsPages.layoutDirection = View.LAYOUT_DIRECTION_RTL
            /*        pageAdapter?.setiOnClick(IOnClick { pos ->
                        // pos represent page and need to be updated by 1 to be as recyclerview
                        // +2 to be as Mushaf
                        rvAyahsPages.scrollToPosition(pos + 1)
                        //   addToReadLog(pos + 2);
                    })*/
        }
    }
    // </editor-fold>
    /**
     * @param surahIndex in quran
     * @return
     */
    private fun getSuraNameFromIndex(surahIndex: Int): String {
        return Data.SURA_NAMES[surahIndex - 1]
    }

    override fun onResume() {
        super.onResume()
        loadData()
        loadPagesReadLoge()
    }

    // <editor-fold desc="prepare colors">
    private fun prepareColors() {
        // check Night Mode
        if (model.nightModeState) {
            ayahsColor = ContextCompat.getColor(this, R.color.ayas_color_night_mode)
            scrollorColor = ContextCompat.getColor(this, R.color.bg_ays_night_mode)
        } else {
            ayahsColor = ContextCompat.getColor(this, R.color.ayas_color)
            // check user color for background
            scrollorColor = when (model.backColorState) {
                Constants.GREEN -> ContextCompat.getColor(this, R.color.bg_green)
                Constants.YELLOW -> ContextCompat.getColor(this, R.color.bg_yellow)
                else -> {
                    ContextCompat.getColor(this, R.color.bg_white)
                }
            }
        }
    }

    private fun loadData() {
        Thread { generateListOfPagesStartWithHizbQurater() }.start()
    }

    private fun loadPagesReadLoge() {
        currentDate = DateOperation.getCurrentDateExact().time
        currentDateStr = DateOperation.getCurrentDateAsString()
        val saved = model.getLReadLogByDate(currentDateStr)
        // pages from an earlier session were saved in onStop; start from what is stored today
        pagesReadLogNumber.clear()
        saved?.pages?.let { pagesReadLogNumber.addAll(it) }
        // first reading today: create the row; it is inserted on the first save
        readLog = saved ?: ReadLog(currentDate, currentDateStr, ArraySet())
    }

    /**
     * retrieve list of pages that contain start of Hizb Quaters.
     */
    private fun generateListOfPagesStartWithHizbQurater() {
        quarterStartsList.clear()
        quarterStartsList.addAll(model.hizbQuaterStart)
        // logData(quraterSStart);
    }

    private fun foundState() {
        with(binding) {
            spShowAyahs.visibility = View.GONE
            rvAyahsPages.visibility = View.VISIBLE
            "found".log()
        }
    }

    private fun notFound() {
        with(binding) {
            spShowAyahs.visibility = View.GONE
            tvNoQuranData.visibility = View.VISIBLE
            rvAyahsPages.visibility = View.GONE
        }
    }

    private fun showMessage(message: String) {
        toast?.cancel()
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    override fun onStop() {
        super.onStop()
        model.addLatestread(lastpageShown)
        saveReadLog()
    }

    private fun saveReadLog() {
        if (pagesReadLogNumber.isEmpty()) return
        readLog?.let {
            it.pages = ArraySet<Int>().apply { addAll(pagesReadLogNumber) }
            // exception used to indicate its update or add case when update it will make exception as there is item in db
            try {
                model.addReadLog(it)
            } catch (e: Exception) {
                model.updateReadLog(it)
            }
        }
    }

    companion object {
        private const val LAST_PAGE = 604
        private const val TAG = "TestApp"
    }
}
