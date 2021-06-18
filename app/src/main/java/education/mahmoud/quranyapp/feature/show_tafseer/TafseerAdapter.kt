package education.mahmoud.quranyapp.feature.show_tafseer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem

class TafseerAdapter : RecyclerView.Adapter<TafseerAdapter.Holder>() {
    private val list: MutableList<AyahItem> = mutableListOf()

    fun addTafseer(item: AyahItem) {
        list.add(item)
        notifyItemInserted(list.size - 1)
        notifyItemRangeChanged(list.size - 1, list.size)
    }

    fun setTafseerList(newList: List<AyahItem>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.tafseer_item, viewGroup, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, i: Int) {
        val item = list[i]
        holder.tvtafseerAyah.text = holder.itemView.context
                .getString(R.string.tafseer_ayah_text_header, i + 1, item.textClean)
        val tafsser = item.tafseer
        holder.tvtafseer.text = tafsser
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(val item: View) : RecyclerView.ViewHolder(item) {
        var tvtafseerAyah: TextView = item.findViewById(R.id.tvtafseerAyah)
        val tvtafseer: TextView = item.findViewById(R.id.tvtafseer)
    }

    companion object {
        private const val TAG = "TafseerAdapter"
    }
}