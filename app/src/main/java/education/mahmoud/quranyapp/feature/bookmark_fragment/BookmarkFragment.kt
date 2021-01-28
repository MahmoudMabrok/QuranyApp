package education.mahmoud.quranyapp.feature.bookmark_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.bind
import education.mahmoud.quranyapp.utils.show
import kotlinx.android.synthetic.main.fragment_bookmark.*
import org.koin.android.ext.android.inject

class BookmarkFragment : BaseFragment(), BaseFragment.InitListener {
    private val repository: Repository by inject()
    private val bookmarkAdapter by lazy { BookmarkAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setOnInitListeners(this)
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun initViews(view: View) {
        init()
        startLoadingData()
    }

    override fun setClickListeners() {
        bookmarkAdapter.setIoBookmark { item ->
            val ayahsActivity = Intent(context, ShowAyahsActivity::class.java)
            ayahsActivity.putExtra(Constants.PAGE_INDEX, item.pageNum)
            startActivity(ayahsActivity)
        }
        bookmarkAdapter.setIoBookmarkDelete { item ->
            repository.deleteBookmark(item)
            bookmarkAdapter.deleteItem(item)
            context?.show(getString(R.string.deleted))
        }
    }

    private fun init() {
        rvBookmark.adapter = bookmarkAdapter
    }

    private fun startLoadingData() {
        repository.bookmarks.bind()
                .subscribe({
                    if (it.isEmpty()) {
                        noDataState()
                    } else {
                        bookmarkAdapter.setBookmarkItemList(it)
                    }
                }, {
                    val msg = it.message ?: "Error "
                    context?.show(msg)
                })
    }

    private fun noDataState() {
        rvBookmark.visibility = View.GONE
        tvNoBookMark.visibility = View.VISIBLE
        hideLoad()
    }
}