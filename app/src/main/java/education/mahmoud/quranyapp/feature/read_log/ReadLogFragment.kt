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
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
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
        val view = inflater.inflate(R.layout.fragment_read_log, container, false)
        return view
    }

    override fun initViews(view: View) {
        super.initViews(view)
        initRV()
        loadData()
    }

    private fun initRV() {
      with(binding) {
        rvReadLog.setAdapter(logAdapter)
        rvReadLog.setItemAnimator(DefaultItemAnimator())

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val readLog = logAdapter.getItem(pos)
                quranRepository.deleteReadLog(readLog)
                logAdapter.deleteitem(pos)
                showMessage(getString(R.string.msg_deleted))
            }
        }).attachToRecyclerView(rvReadLog)
      } }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        logAdapter.setReadLogList(quranRepository.readLog)
    }
}
