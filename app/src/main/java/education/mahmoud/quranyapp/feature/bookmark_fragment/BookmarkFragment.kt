package education.mahmoud.quranyapp.feature.bookmark_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ethanhua.skeleton.Skeleton
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.databinding.FragmentBookmarkBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.bind
import education.mahmoud.quranyapp.utils.log
import education.mahmoud.quranyapp.utils.show
import io.reactivex.rxkotlin.addTo
import org.koin.android.ext.android.inject

class BookmarkFragment : DataLoadingBaseFragment() {
    private val quranRepository: QuranRepository by inject()
    private val bookmarkAdapter by lazy { BookmarkAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        initRv()
    }

    private val binding by viewBinding(FragmentBookmarkBinding::bind)

    private fun initRv() {
        binding.rvBookmark.adapter = bookmarkAdapter
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

        Skeleton.bind(binding.rvBookmark)
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
        binding.rvBookmark.visibility = View.GONE
        binding.tvNoBookMark.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "BookmarkFragment"
    }
}
