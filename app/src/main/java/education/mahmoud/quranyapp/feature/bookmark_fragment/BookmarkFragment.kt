package education.mahmoud.quranyapp.feature.bookmark_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.Data
import kotlinx.android.synthetic.main.fragment_bookmark.*
import org.koin.android.ext.android.inject
import java.util.*


class BookmarkFragment : DataLoadingBaseFragment() {
    private val repository: Repository by inject()
    private val bookmarkAdapter by lazy { BookmarkAdapter() }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        initRv()
        retriveBookmarks()
    }

    private fun initRv() {
        rvBookmark.adapter = bookmarkAdapter
        bookmarkAdapter.setIoBookmark { item ->
            val openAcivity = Intent(context, ShowAyahsActivity::class.java)
            openAcivity.putExtra(Constants.PAGE_INDEX, item.pageNum)
            startActivity(openAcivity)
        }
        bookmarkAdapter.setIoBookmarkDelete { item ->
            repository.deleteBookmark(item)
            bookmarkAdapter.deleteItem(item)
            Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && rvBookmark != null) {
            bookmarkAdapter.setBookmarkItemList(repository.bookmarks)
            if (bookmarkAdapter.itemCount > 0) {
                availbaleData()
            } else {
                noData()
            }
        }
    }

    private fun noData() {
        rvBookmark.visibility = View.GONE
        tvNoBookMark.visibility = View.VISIBLE
    }

    private fun availbaleData() {
        rvBookmark.visibility = View.VISIBLE
        tvNoBookMark.visibility = View.GONE
    }

    private fun getIndexOfString(suraName: String): Int {
        val list: List<String> = ArrayList(Arrays.asList(*Data.SURA_NAMES))
        return list.indexOf(suraName)
    }

    private fun retriveBookmarks() {
        bookmarkAdapter.setBookmarkItemList(repository.bookmarks)
        if (bookmarkAdapter.itemCount > 0) {
            availbaleData()
        } else {
            noData()
        }
    }


    companion object {
        private const val TAG = "BookmarkFragment"
    }
}