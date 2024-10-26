package education.mahmoud.quranyapp.feature.showSuraAyas

import android.graphics.text.LineBreaker
import android.os.Build
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.databinding.PageItemBinding
import education.mahmoud.quranyapp.utils.Util

class PageViewHolder(private val binding: PageItemBinding, iBookmark: PageAdapter.IBookmark?) : RecyclerView.ViewHolder(binding.root) {

    // defined here to not be created with each bind call.
    init {
        binding.imBookmark.setOnClickListener {
            binding.page?.let {
                iBookmark?.onBookmarkClicked(it)
            }
        }
    }

    fun bind(page: Page, ayahsColor: Int, scrollColor: Int) {
        binding.page = page

        // set Colors
        binding.tvAyahs.setTextColor(ayahsColor)
        binding.tvJuz.setTextColor(ayahsColor)
        binding.tvPageNumShowAyahs.setTextColor(ayahsColor)
        binding.tvSurahName.setTextColor(ayahsColor)
        // as no setting screen enabled now // todo checked later
        // binding.ayahsLayout.setBackgroundColor(scrollColor)

        binding.tvAyahs.setText(Util.getSpannable(page.getText()), TextView.BufferType.SPANNABLE)

        // text justification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.tvAyahs.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }
    }
}
