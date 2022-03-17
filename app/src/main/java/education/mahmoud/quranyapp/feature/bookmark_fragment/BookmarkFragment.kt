package education.mahmoud.quranyapp.feature.bookmark_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ethanhua.skeleton.Skeleton
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.bind
import education.mahmoud.quranyapp.utils.log
import education.mahmoud.quranyapp.utils.show
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_bookmark.*
import org.koin.android.ext.android.inject

class BookmarkFragment : DataLoadingBaseFragment() {
    private val quranRepository: QuranRepository by inject()
    private val bookmarkAdapter by lazy { BookmarkAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        initRv()
    }

    private fun initRv() {
        rvBookmark.adapter = bookmarkAdapter
        bookmarkAdapter.setIoBookmark { item ->
            val openAcivity = Intent(context, ShowAyahsActivity::class.java)
            openAcivity.putExtra(Constants.PAGE_INDEX, item.pageNum)
            startActivity(openAcivity)
        }
        bookmarkAdapter.setIoBookmarkDelete { item ->
            quranRepository.deleteBookmark(item)
            bookmarkAdapter.deleteItem(item)
            context?.show(getString(R.string.deleted))
        }

        Skeleton.bind(rvBookmark)
                .adapter(bookmarkAdapter)
                .load(R.layout.bookmark_skelton)
                .show()
                .apply {
                    hide()
                    addSketlon(this)
                }
    }

    override fun startLoadingData() {
        super.startLoadingData()
        quranRepository.bookmarks.bind()
                .doAfterNext {
                    hideLoad()
                }
                .subscribe({
                    "$it".log()
                    if (it.isEmpty()) {
                        noDataState()
                    }
                    bookmarkAdapter.setBookmarkItemList(it)
                }, {
                    val msg = it.message ?: "Error "
                    context?.show(msg)
                })
                .addTo(bg)
    }

    override fun noDataState() {
        super.noDataState()
        rvBookmark.visibility = View.GONE
        tvNoBookMark.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "BookmarkFragment"
    }
}
