package education.mahmoud.quranyapp.feature.read_log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.DataLoadingBaseFragment
import education.mahmoud.quranyapp.databinding.FragmentReadLogBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.utils.EdgeToEdge
import education.mahmoud.quranyapp.utils.UiFont
import org.koin.android.ext.android.inject

/**
 * Reading log: one card per day with the pages read in the Mushaf that day.
 */
class ReadLogFragment : DataLoadingBaseFragment() {

    private val quranRepository: QuranRepository by inject()
    private var logAdapter: ReadLogAdapter = ReadLogAdapter()

    private val binding by viewBinding(FragmentReadLogBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_read_log, container, false)
    }

    override fun initViews(view: View) {
        super.initViews(view)
        with(binding) {
            // edge-to-edge: teal bar behind the status bar, list and empty state above the nav bar
            EdgeToEdge.applyPadding(readLogToolbar, bottom = false)
            EdgeToEdge.applyPadding(rvReadLog, top = false)
            EdgeToEdge.applyPadding(readLogEmpty, top = false)
            UiFont.applyBold(readLogTitle, readLogEmptyTitle)
            UiFont.apply(readLogSummary, readLogEmptyHint)
            readLogBack.setOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
        }
        // teal bar -> light status bar icons while shown
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, false) }
        initRV()
        loadData()
    }

    private fun initRV() {
        with(binding) {
            rvReadLog.adapter = logAdapter
            rvReadLog.itemAnimator = DefaultItemAnimator()

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val pos = viewHolder.adapterPosition
                    if (pos == RecyclerView.NO_POSITION) return
                    val readLog = logAdapter.getItem(pos)
                    quranRepository.deleteReadLog(readLog)
                    logAdapter.deleteitem(pos)
                    showMessage(getString(R.string.msg_deleted))
                    updateState()
                }
            }).attachToRecyclerView(rvReadLog)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        logAdapter.setReadLogList(quranRepository.readLog)
        updateState()
    }

    /** list vs. empty state, plus the totals line above the list */
    private fun updateState() {
        val logs = logAdapter.list
        val empty = logs.isEmpty()
        with(binding) {
            readLogEmpty.visibility = if (empty) View.VISIBLE else View.GONE
            rvReadLog.visibility = if (empty) View.GONE else View.VISIBLE
            readLogSummary.visibility = if (empty) View.GONE else View.VISIBLE
            if (!empty) {
                val pages = logs.sumOf { it.pages?.size ?: 0 }
                readLogSummary.text = getString(
                    R.string.read_log_summary,
                    ReadLogFormat.pagesCount(pages),
                    ReadLogFormat.daysCount(logs.size),
                )
            }
        }
    }

    override fun onDestroyView() {
        // back to the home teal app bar: white icons (dark icons in dark theme)
        activity?.let { EdgeToEdge.setStatusBarIconsDark(it, EdgeToEdge.isSystemNight(it)) }
        super.onDestroyView()
    }
}
