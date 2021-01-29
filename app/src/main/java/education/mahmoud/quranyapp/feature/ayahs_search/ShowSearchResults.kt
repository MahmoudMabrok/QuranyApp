package education.mahmoud.quranyapp.feature.ayahs_search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.flipboard.bottomsheet.commons.MenuSheetView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.listening_activity.AyahsListen
import education.mahmoud.quranyapp.feature.listening_activity.ListenServie
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.Util
import kotlinx.android.synthetic.main.activity_show_search_results.*
import kotlinx.android.synthetic.main.header_search.*
import org.koin.java.KoinJavaComponent
import java.util.* // ktlint-disable no-wildcard-imports

class ShowSearchResults : BaseFragment() {

  private var adapter = SearchResultsAdapter()

  private val repository = KoinJavaComponent.get(QuranRepository::class.java)
  private var ayahItems: List<AyahItem> = listOf()
  private var ayah: String = ""
  var isRunning = false

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.activity_show_search_results, container, false)
  }

  override fun initViews(view: View) {
    super.initViews(view)
    initRv()
    adapterListeners()
    editWatcher()
    setClickListeners()
  }

  override fun setClickListeners() {
    super.setClickListeners()

    imSearch.setOnClickListener {
      doSearch("${edSearch.text}")
    }
  }

  private fun editWatcher() {
    edSearch?.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
      override fun afterTextChanged(editable: Editable) {
        doSearch(editable.toString())
      }
    })
  }

  private fun doSearch(text: String?) {
    ayah = text ?: ""
    ayah = ayah.replace(" +".toRegex(), " ")
    if (ayah.isNotEmpty()) {
      ayahItems = repository.getAyahByAyahText(ayah)
      Log.d(TAG, "afterTextChanged: " + ayahItems.size)
      val count = ayahItems.size // n of results
      tvSearchCount?.text = getString(R.string.times, count)
      if (count > 0) {
        adapter.setAyahItemList(ayahItems, ayah)
        foundState()
      } else {
        notFoundState()
      }
    } else {
      defaultState()
    }
  }

  private fun setUpBottomSheet(ayahItem: AyahItem) { // bottom sheet
    val menuSheetView = MenuSheetView(
        requireContext(), MenuSheetView.MenuType.LIST, "Options",
        MenuSheetView.OnMenuItemClickListener { item ->
          if (bottomSearch.isSheetShowing) {
            bottomSearch?.dismissSheet()
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
    bottomSearch?.showWithSheetView(menuSheetView)
  }

  private fun showTafseer(ayahItem: AyahItem) {
    val title = this.getString(R.string.tafserr_info, ayahItem.ayahInSurahIndex, ayahItem.pageNum, ayahItem.juz)
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
    tvNotFound?.visibility = View.GONE
  }

  private fun initRv() {
    rvSearch?.adapter = adapter
    rvSearch?.setHasFixedSize(true)
  }

  private fun foundState() {
    rvSearch?.visibility = View.VISIBLE
    tvNotFound?.visibility = View.GONE
  }

  private fun notFoundState() {
    rvSearch?.visibility = View.GONE
    tvNotFound?.visibility = View.VISIBLE
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
  }
}
