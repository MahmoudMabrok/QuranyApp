package education.mahmoud.quranyapp.feature.read_log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.base.BaseFragment
import education.mahmoud.quranyapp.datalayer.Repository
import education.mahmoud.quranyapp.utils.show
import kotlinx.android.synthetic.main.fragment_read_log.*
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
 */
class ReadLogFragment : BaseFragment(), BaseFragment.InitListener {

    private val repository: Repository by inject()
    private var logAdapter: ReadLogAdapter = ReadLogAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setOnInitListeners(this)
        return inflater.inflate(R.layout.fragment_read_log, container, false)
    }

    override fun initViews(view: View) {
        initRV()
        loadData()
    }

    override fun setClickListeners() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val readLog = logAdapter.getItem(pos)
                repository.deleteReadLog(readLog)
                logAdapter.deleteitem(pos)
                context?.show(getString(R.string.msg_deleted))
            }
        }).attachToRecyclerView(rvReadLog)
    }

    private fun initRV() {
        rvReadLog.adapter = logAdapter
        rvReadLog.itemAnimator = DefaultItemAnimator()
    }

    private fun loadData() {
        logAdapter.setReadLogList(repository.readLog)
    }
}