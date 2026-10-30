package education.mahmoud.quranyapp.feature.ayahs_search

import education.mahmoud.quranyapp.utils.EdgeToEdge
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.flipboard.bottomsheet.commons.MenuSheetView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.databinding.ActivityShowSearchResultsBinding
import education.mahmoud.quranyapp.databinding.HeaderSearchBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.listening_activity.AyahsListen
import education.mahmoud.quranyapp.feature.listening_activity.ListenServie
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.KeyboardHelper
import education.mahmoud.quranyapp.utils.NumberHelper
import education.mahmoud.quranyapp.utils.UiFont
import education.mahmoud.quranyapp.utils.Util
import org.koin.java.KoinJavaComponent
import java.util.* // ktlint-disable no-wildcard-imports

class ShowSearchResults : BaseFragment() {

    private var adapter = SearchResultsAdapter()

    private val repository: QuranRepository = KoinJavaComponent.get(QuranRepository::class.java)
    private var ayahItems: List<AyahItem> = listOf()
    private var ayah: String = ""
    var isRunning = false

    private val binding by viewBinding(ActivityShowSearchResultsBinding::bind)
    // header_search is an <include>; bind it from its own root, not the fragment root
    private val searchBinding by viewBinding { view: View -> HeaderSearchBinding.bind(view.findViewById(R.id.toolbar)) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.activity_show_search_results, container, false)
    }

    private val searchHandler = Handler(Looper.getMainLooper())
    private val pendingSearch = Runnable { doSearch(searchBinding.edSearch.text?.toString()) }

    override fun initViews(view: View) {
        super.initViews(view)
        // edge-to-edge: the teal search bar goes behind the status bar, the results list
        // (and the empty state) end above the navigation bar or the keyboard
        EdgeToEdge.applyPadding(searchBinding.root, bottom = false)
        val bottomTypes = EdgeToEdge.SAFE_TYPES or WindowInsetsCompat.Type.ime()
        EdgeToEdge.applyPadding(binding.rvSearch, top = false, types = bottomTypes)
        EdgeToEdge.applyPadding(binding.searchEmpty, top = false, types = bottomTypes)
        // teal search bar -> light status bar icons while this overlay is shown
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, false) }
        UiFont.apply(searchBinding.edSearch, binding.tvSearchCount, binding.tvNotFound)
        UiFont.applyBold(binding.tvSearchEmptyTitle)
        initRv()
        adapterListeners()
        editWatcher()
        setClickListeners()
        defaultState()
        searchBinding.edSearch.requestFocus()
        KeyboardHelper.showKeyboard(searchBinding.edSearch)
    }

    override fun setClickListeners() {
        super.setClickListeners()

        searchBinding.imSearch.setOnClickListener { searchNow() }
        searchBinding.imClear.setOnClickListener {
            searchBinding.edSearch.setText("")
            searchBinding.edSearch.requestFocus()
        }
        searchBinding.edSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchNow()
                true
            } else {
                false
            }
        }
    }

    private fun searchNow() {
        searchHandler.removeCallbacks(pendingSearch)
        doSearch(searchBinding.edSearch.text?.toString())
        KeyboardHelper.hideKeyboard(searchBinding.edSearch)
    }

    private fun editWatcher() {
        searchBinding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                searchBinding.imClear.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                // small debounce: don't query the whole Quran on every keystroke
                searchHandler.removeCallbacks(pendingSearch)
                searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS)
            }
        })
    }

    private fun doSearch(text: String?) {
        if (view == null) return
        ayah = SearchText.normalizeQuery(text)
        if (ayah.isEmpty()) {
            defaultState()
            return
        }
        ayahItems = repository.getAyahByAyahText(ayah)
        Log.d(TAG, "doSearch: " + ayahItems.size)
        val count = ayahItems.size
        if (count > 0) {
            binding.tvSearchCount.text = getString(R.string.search_results_count, NumberHelper.getArabicNumber(count))
            adapter.setAyahItemList(ayahItems, ayah)
            binding.rvSearch.scrollToPosition(0)
            foundState()
        } else {
            notFoundState(ayah)
        }
    }

    private fun setUpBottomSheet(ayahItem: AyahItem) { // bottom sheet
        val menuSheetView = MenuSheetView(
            requireContext(), MenuSheetView.MenuType.LIST, getString(R.string.search_options),
            MenuSheetView.OnMenuItemClickListener { item ->
                if (binding.bottomSearch.isSheetShowing) {
                    binding.bottomSearch.dismissSheet()
                }
                when (item.itemId) {
                    R.id.menuOpen -> openPage(ayahItem.pageNum)
                    R.id.menuPlaySound -> playAudio(ayahItem)
                    R.id.menuTafser -> showTafseer(ayahItem)
                }
                true
            }
        )
        menuSheetView.inflateMenu(R.menu.menu_sheet_search)
        // keep the sheet items above the navigation bar
        EdgeToEdge.applyPadding(menuSheetView, top = false)
        binding.bottomSearch.showWithSheetView(menuSheetView)
    }

    private fun showTafseer(ayahItem: AyahItem) {
        val title = this.getString(
            R.string.tafserr_info,
            ayahItem.ayahInSurahIndex,
            ayahItem.pageNum,
            ayahItem.juz
        )
        Util.getDialog(requireContext(), ayahItem.tafseer, title).show()
    }

    private fun adapterListeners() {
        adapter.setiSearchItemClick(object : SearchResultsAdapter.ISearchItemClick {
            override fun onSearchItemClick(item: AyahItem?) {
                item?.let { setUpBottomSheet(item) }
                Log.d(TAG, "onSearchItemClick: ")
            }
        })
    }

    private fun openPage(pageNum: Int) {
        activity?.let {
            val openAcivity = Intent(requireContext(), ShowAyahsActivity::class.java)
            openAcivity.putExtra(Constants.PAGE_INDEX, pageNum)
            startActivity(openAcivity)
        }
    }

    /**
     * state with no search performed
     */
    private fun defaultState() {
        adapter.clear()
        binding.rvSearch.visibility = View.GONE
        binding.tvSearchCount.visibility = View.GONE
        binding.searchEmpty.visibility = View.VISIBLE
        binding.tvSearchEmptyTitle.setText(R.string.search_empty_title)
        binding.tvNotFound.setText(R.string.search_empty_hint)
    }

    private fun initRv() {
        binding.rvSearch.adapter = adapter
        binding.rvSearch.setHasFixedSize(true)
        // hide the keyboard once the user starts scrolling the results
        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) KeyboardHelper.hideKeyboard(recyclerView)
            }
        })
    }

    private fun foundState() {
        binding.rvSearch.visibility = View.VISIBLE
        binding.tvSearchCount.visibility = View.VISIBLE
        binding.searchEmpty.visibility = View.GONE
    }

    private fun notFoundState(query: String) {
        adapter.clear()
        binding.rvSearch.visibility = View.GONE
        binding.tvSearchCount.visibility = View.GONE
        binding.searchEmpty.visibility = View.VISIBLE
        binding.tvSearchEmptyTitle.setText(R.string.search_not_found_title)
        binding.tvNotFound.text = getString(R.string.search_not_found_hint, query)
    }

    var serviceIntent: Intent? = null

    private fun playAudio(item: AyahItem) {
        if (item.audioPath != null) {
            Log.d(TAG, "playAudio: isRunning  $isRunning")
            val ayahItems: MutableList<AyahItem> = ArrayList()
            ayahItems.add(item)
            val ayahsListen = AyahsListen()
            ayahsListen.ayahItemList = ayahItems
            if (serviceIntent != null) {
                requireContext().stopService(serviceIntent)
            }
            serviceIntent = ListenServie.createService(
                requireActivity().applicationContext,
                ayahsListen
            )
        } else {
            showMessage(getString(R.string.not_downlod_audio))
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "ShowSearchResults"
        private const val RC_STORAGE = 10002
        private const val SEARCH_DEBOUNCE_MS = 250L
    }

    override fun onDestroyView() {
        searchHandler.removeCallbacks(pendingSearch)
        // the view-binding delegate is already cleared here; use the fragment view directly
        view?.let { KeyboardHelper.hideKeyboard(it) }
        // back to the home teal app bar: white icons (dark icons in dark theme)
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, EdgeToEdge.isSystemNight(it)) }
        super.onDestroyView()
    }
}
