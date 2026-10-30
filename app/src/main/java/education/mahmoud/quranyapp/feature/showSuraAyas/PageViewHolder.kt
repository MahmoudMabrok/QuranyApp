package education.mahmoud.quranyapp.feature.showSuraAyas

import android.graphics.Rect
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.recyclerview.widget.RecyclerView
import education.mahmoud.quranyapp.databinding.PageItemBinding
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.JuzNames
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafFonts
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafFrameDrawable
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafOptions
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.MushafPalette
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderSpan
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.SuraHeaderStyle
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaf.isRightPage
import education.mahmoud.quranyapp.utils.Util

class PageViewHolder(private val binding: PageItemBinding, iBookmark: PageAdapter.IBookmark?) : RecyclerView.ViewHolder(binding.root) {

    private val padding = Rect()
    private var lastTextWidth = 0

    // defined here to not be created with each bind call.
    init {
        // header spans take the text width; if it changes (first layout, rotation, split screen)
        // set the text again so the headers are measured with the real width
        binding.tvAyahs.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            val tv = v as TextView
            val w = tv.width - tv.totalPaddingLeft - tv.totalPaddingRight
            if (w > 0 && w != lastTextWidth) {
                lastTextWidth = w
                tv.post { tv.setText(tv.text, TextView.BufferType.SPANNABLE) }
            }
        }
        binding.imBookmark.setOnClickListener {
            binding.page?.let {
                iBookmark?.onBookmarkClicked(it)
            }
        }
    }

    fun bind(page: Page, ayahsColor: Int, scrollColor: Int, options: MushafOptions = MushafOptions()) {
        binding.page = page
        // apply data binding now, so it does not overwrite the texts set below on the next frame
        binding.executePendingBindings()

        val context = binding.root.context
        val tv = binding.tvAyahs
        val palette = MushafPalette.of(options.headerStyle, options.nightMode, scrollColor, ayahsColor)
        val fonts = MushafFonts.get(context)

        // set Colors
        tv.setTextColor(ayahsColor)
        binding.tvJuz.setTextColor(ayahsColor)
        binding.tvPageNumShowAyahs.setTextColor(ayahsColor)
        binding.tvSurahName.setTextColor(ayahsColor)
        binding.ayahsLayout.setBackgroundColor(scrollColor)

        // text with Mushaf-style sura headers
        val content = page.buildContent()
        val spannable = Util.getSpannable(content.text)
        if (options.headerStyle != SuraHeaderStyle.PLAIN) {
            for (header in content.headers) {
                val info = page.suraInfos[header.surahIndex] ?: continue
                // the header draws its own colours; drop the red "Allah" colouring inside names
                spannable.getSpans(header.start, header.end, ForegroundColorSpan::class.java)
                    .forEach { spannable.removeSpan(it) }
                spannable.setSpan(
                    SuraHeaderSpan(info, options.headerStyle, palette, fonts) { textWidth(tv) },
                    header.start, header.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
        tv.setText(spannable, TextView.BufferType.SPANNABLE)

        // text justification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tv.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }

        if (options.pageFrame) applyFrame(page, palette, fonts, content) else removeFrame(page)
    }

    private fun applyFrame(page: Page, palette: MushafPalette, fonts: MushafFonts, content: Page.Content) {
        val tv = binding.tvAyahs
        val metrics = tv.resources.displayMetrics
        val widthDp = metrics.widthPixels / metrics.density
        val unit = metrics.density * (widthDp / 411f).coerceIn(0.8f, 1.25f)
        val hizbOffset = page.hizbMark?.let { content.ayahStarts.getOrNull(it.ayahPosition) }
        val frame = MushafFrameDrawable(palette, fonts, unit, page.pageNum, page.hizbMark) {
            val layout = tv.layout
            if (hizbOffset == null || layout == null) {
                null
            } else {
                val line = layout.getLineForOffset(hizbOffset.coerceAtMost(tv.text.length))
                tv.extendedPaddingTop + (layout.getLineTop(line) + layout.getLineBottom(line)) / 2f
            }
        }
        tv.background = frame
        frame.contentPadding(padding)
        tv.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        setMargin(tv, 0)
        // page number is drawn by the frame medallion
        binding.tvPageNumShowAyahs.visibility = View.GONE

        // top bar like the printed Mushaf: juz on the outer side, sura name on the inner side
        binding.tvJuz.text = JuzNames.of(page.juz)
        val right = isRightPage(page.pageNum)
        val bar = binding.topLinear
        bar.removeAllViews()
        if (right) {
            bar.addView(binding.imBookmark)
            bar.addView(binding.tvSurahName)
            bar.addView(binding.tvJuz)
        } else {
            bar.addView(binding.tvJuz)
            bar.addView(binding.tvSurahName)
            bar.addView(binding.imBookmark)
        }
        binding.tvSurahName.gravity = if (right) Gravity.START else Gravity.END
        binding.tvSurahName.textAlignment = if (right) View.TEXT_ALIGNMENT_VIEW_START else View.TEXT_ALIGNMENT_VIEW_END
        binding.tvJuz.textAlignment = if (right) View.TEXT_ALIGNMENT_VIEW_END else View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun removeFrame(page: Page) {
        val tv = binding.tvAyahs
        val px = (10 * tv.resources.displayMetrics.density).toInt()
        tv.background = null
        tv.setPadding(px, px, px, px)
        setMargin(tv, px)
        binding.tvPageNumShowAyahs.visibility = View.VISIBLE
        binding.tvJuz.text = page.juz.toString()
        val bar = binding.topLinear
        bar.removeAllViews()
        bar.addView(binding.tvSurahName)
        bar.addView(binding.tvJuz)
        bar.addView(binding.imBookmark)
        binding.tvSurahName.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        binding.tvJuz.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
    }

    /** Width of the text area; before the first layout it is estimated from the screen width. */
    private fun textWidth(tv: TextView): Int {
        if (tv.width > 0) return tv.width - tv.totalPaddingLeft - tv.totalPaddingRight
        val lp = tv.layoutParams as? ViewGroup.MarginLayoutParams
        val margins = (lp?.leftMargin ?: 0) + (lp?.rightMargin ?: 0)
        return tv.resources.displayMetrics.widthPixels - margins - tv.paddingLeft - tv.paddingRight
    }

    /** Keeps the page bar, text and frame out of the status / navigation bars and cutouts. */
    fun applyInsets(insets: Insets) {
        binding.ayahsLayout.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }

    private fun setMargin(view: View, px: Int) {
        val lp = view.layoutParams as? ViewGroup.MarginLayoutParams ?: return
        if (lp.leftMargin == px && lp.topMargin == px) return
        lp.setMargins(px, px, px, px)
        view.layoutParams = lp
    }
}
