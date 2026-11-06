package education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext

import androidx.collection.ArraySet
import education.mahmoud.quranyapp.datalayer.local.room.ReadLog
import education.mahmoud.quranyapp.feature.showSuraAyas.AyahsViewModel
import education.mahmoud.quranyapp.utils.DateOperation
import education.mahmoud.quranyapp.utils.log

/**
 * Today's reading log, recorded the same way as the previous reader:
 * pages shown are collected in a set, loaded from today's stored ReadLog on resume and
 * written back (insert, or update if today's row exists) on stop.
 */
class ReadLogRecorder(private val model: AyahsViewModel) {
    private val pages = mutableSetOf<Int>()
    private var readLog: ReadLog? = null

    fun load() {
        try {
            val date = DateOperation.getCurrentDateExact().time
            val dateStr = DateOperation.getCurrentDateAsString()
            val saved = model.getLReadLogByDate(dateStr)
            pages.retainAll { false }
            saved?.pages?.forEach { if (it in 1..MUSHAF_PAGE_COUNT) pages.add(it) }
            readLog = saved ?: ReadLog(date, dateStr, ArraySet())
        } catch (e: RuntimeException) {
            "read log skipped ${e.message}".log()
            readLog = ReadLog(System.currentTimeMillis(), "", ArraySet())
        }
    }

    fun onPageShown(page: Int) {
        if (page in 1..MUSHAF_PAGE_COUNT) pages.add(page)
    }

    fun save() {
        if (pages.isEmpty()) return
        val item = readLog ?: return
        try {
            item.pages = ArraySet<Int>().apply { addAll(pages) }
            try {
                model.addReadLog(item)
            } catch (e: Exception) {
                model.updateReadLog(item)
            }
        } catch (e: RuntimeException) {
            "read log not saved ${e.message}".log()
        }
    }
}
