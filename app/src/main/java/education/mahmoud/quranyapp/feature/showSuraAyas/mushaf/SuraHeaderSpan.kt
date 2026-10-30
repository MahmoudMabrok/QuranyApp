package education.mahmoud.quranyapp.feature.showSuraAyas.mushaf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.ReplacementSpan
import kotlin.math.min

/**
 * Draws a sura-name line as a Mushaf-style header (style A classic gold cartouche or
 * style B modern teal pill). Sizes follow the current text size, so the page text
 * auto-sizing keeps working: when the text shrinks to fit, the header shrinks with it.
 *
 * @param availableWidth width the header may take (text area width), in px.
 */
class SuraHeaderSpan(
    private val info: SuraHeaderInfo,
    private val style: SuraHeaderStyle,
    private val palette: MushafPalette,
    private val fonts: MushafFonts,
    private val availableWidth: () -> Int,
) : ReplacementSpan() {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val path = Path()
    private val rect = RectF()
    private val diamondPath = Path()

    private fun widthFor(textSize: Float): Float {
        val w = availableWidth()
        // before the first layout the width is unknown; any sane value works, it is measured again
        return if (w > 0) (w - 2).toFloat() else textSize * 16
    }

    /** Header height for a text size, kept proportional to the width on narrow screens. */
    private fun heightFor(textSize: Float, width: Float): Float = when (style) {
        SuraHeaderStyle.MODERN -> min(textSize * 2.3f, width * 0.15f)
        else -> min(textSize * 2.9f, width * 0.19f)
    }

    private fun gap(textSize: Float) = textSize * 0.18f

    override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val w = widthFor(paint.textSize)
        if (fm != null) {
            val h = heightFor(paint.textSize, w)
            val g = gap(paint.textSize)
            fm.ascent = -(h * 0.72f + g).toInt()
            fm.top = fm.ascent
            fm.descent = (h * 0.28f + g).toInt()
            fm.bottom = fm.descent
        }
        return w.toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint,
    ) {
        val w = widthFor(paint.textSize)
        val h = heightFor(paint.textSize, w)
        canvas.save()
        canvas.translate(x, y - h * 0.72f)
        when (style) {
            SuraHeaderStyle.MODERN -> drawModern(canvas, w, h)
            else -> drawClassic(canvas, w, h)
        }
        canvas.restore()
    }

    // ---------------------------------------------------------------- style A
    private fun drawClassic(c: Canvas, w: Float, h: Float) {
        val s = h / 84f // mockup units
        val cx = w / 2
        val cy = h / 2

        // outer ornamental band
        rect.set(s, s, w - s, h - s)
        fill(palette.band)
        c.drawRoundRect(rect, 5 * s, 5 * s, p)
        p.color = palette.line
        p.alpha = 110
        var dx = 12 * s
        while (dx < w - 8 * s) {
            diamond(c, dx, 4.5f * s, 1.6f * s)
            diamond(c, dx, h - 4.5f * s, 1.6f * s)
            dx += 10 * s
        }
        p.alpha = 255
        stroke(palette.line, 1.6f * s)
        c.drawRoundRect(rect, 5 * s, 5 * s, p)

        // inner field
        rect.set(8 * s, 8 * s, w - 8 * s, h - 8 * s)
        fill(palette.cream)
        c.drawRoundRect(rect, 3 * s, 3 * s, p)
        stroke(palette.line, 1.2f * s)
        c.drawRoundRect(rect, 3 * s, 3 * s, p)

        // cartouche
        val cw = min(118 * s * (w / (472 * s)).coerceAtMost(1.15f), w * 0.3f)
        val ch = 30 * s
        cartouche(cx, cy, cw, ch, s)
        fill(palette.cartouche)
        c.drawPath(path, p)
        stroke(palette.line, 2.2f * s)
        c.drawPath(path, p)
        c.save()
        c.scale(0.92f, 0.8f, cx, cy)
        stroke(palette.line, 0.8f * s / 0.86f)
        c.drawPath(path, p)
        c.restore()
        fill(palette.line)
        c.drawCircle(cx - cw, cy, 3 * s, p)
        c.drawCircle(cx + cw, cy, 3 * s, p)

        // name + order
        val nameMax = 2 * cw - 56 * s
        drawFitted(c, info.displayName, fonts.name, 27 * s, palette.cartoucheText, cx, cy + 4 * s, nameMax)
        drawFitted(c, info.orderLabel, fonts.caption, 9.5f * s, palette.cartoucheText, cx, cy + 21 * s, nameMax)

        // side panels: right = revelation (Makki/Madani), left = ayah count (RTL reading order)
        val pw = (w - 2 * cw - 48 * s) / 2
        if (pw > 18 * s) {
            panel(c, w - 14 * s - pw, pw, h, s, "نزولها", info.revelationLabel)
            panel(c, 14 * s, pw, h, s, "آياتها", info.ayahCountArabic)
        }
    }

    private fun panel(c: Canvas, x: Float, pw: Float, h: Float, s: Float, caption: String, value: String) {
        rect.set(x, 17 * s, x + pw, h - 17 * s)
        stroke(palette.line, 1f * s)
        c.drawRoundRect(rect, 6 * s, 6 * s, p)
        fill(palette.line)
        diamond(c, x + pw / 2, 17 * s, 5 * s)
        diamond(c, x + pw / 2, h - 17 * s, 5 * s)
        val cx = x + pw / 2
        drawFitted(c, caption, fonts.caption, 10.5f * s, palette.muted, cx, 36 * s, pw - 6 * s)
        drawFitted(c, value, fonts.value, 20 * s, palette.value, cx, 58 * s, pw - 6 * s)
    }

    private fun cartouche(cx: Float, cy: Float, cw: Float, ch: Float, s: Float) {
        path.reset()
        path.moveTo(cx - cw, cy)
        path.cubicTo(cx - cw + 10 * s, cy - ch * 0.55f, cx - cw + 14 * s, cy - ch, cx - cw + 34 * s, cy - ch)
        path.lineTo(cx + cw - 34 * s, cy - ch)
        path.cubicTo(cx + cw - 14 * s, cy - ch, cx + cw - 10 * s, cy - ch * 0.55f, cx + cw, cy)
        path.cubicTo(cx + cw - 10 * s, cy + ch * 0.55f, cx + cw - 14 * s, cy + ch, cx + cw - 34 * s, cy + ch)
        path.lineTo(cx - cw + 34 * s, cy + ch)
        path.cubicTo(cx - cw + 14 * s, cy + ch, cx - cw + 10 * s, cy + ch * 0.55f, cx - cw, cy)
        path.close()
    }

    // ---------------------------------------------------------------- style B
    private fun drawModern(c: Canvas, w: Float, h: Float) {
        val s = h / 64f
        val cx = w / 2
        val cy = h / 2
        rect.set(s, s, w - s, h - s)
        fill(palette.tint)
        c.drawRoundRect(rect, 16 * s, 16 * s, p)
        stroke(palette.line, 1.2f * s)
        c.drawRoundRect(rect, 16 * s, 16 * s, p)

        val pw2 = min(92 * s * (w / (472 * s)).coerceAtMost(1.2f), w * 0.26f)
        // connecting lines and fading diamonds
        stroke(palette.line, 0.8f * s)
        p.alpha = 128
        c.drawLine(cx - pw2 - 6 * s, cy, cx - pw2 - 40 * s, cy, p)
        c.drawLine(cx + pw2 + 6 * s, cy, cx + pw2 + 40 * s, cy, p)
        fill(palette.line)
        for ((off, alpha) in listOf(14f to 255, 25f to 153, 36f to 90)) {
            p.alpha = alpha
            diamond(c, cx - pw2 - off * s, cy, 3 * s)
            diamond(c, cx + pw2 + off * s, cy, 3 * s)
        }
        p.alpha = 255

        // pill
        rect.set(cx - pw2, 11 * s, cx + pw2, h - 11 * s)
        fill(palette.cartouche)
        c.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, p)
        drawFitted(c, info.displayName, fonts.name, 22 * s, palette.cartoucheText, cx, cy + 7 * s, 2 * pw2 - 20 * s)

        val sideW = (w / 2 - pw2 - 44 * s).coerceAtLeast(0f)
        if (sideW > 20 * s) {
            val rx = w - 18 * s - sideW / 2
            val lx = 18 * s + sideW / 2
            drawFitted(c, info.revelationLabel, fonts.valueBold, 17 * s, palette.value, rx, cy + 1 * s, sideW)
            drawFitted(c, info.orderLabel, fonts.caption, 10.5f * s, palette.muted, rx, cy + 17 * s, sideW)
            drawFitted(c, info.ayahCountArabic, fonts.valueBold, 17 * s, palette.value, lx, cy + 1 * s, sideW)
            drawFitted(c, "آياتها", fonts.caption, 10.5f * s, palette.muted, lx, cy + 17 * s, sideW)
        }
    }

    // ---------------------------------------------------------------- helpers
    private fun fill(color: Int) {
        p.style = Paint.Style.FILL
        p.color = color
    }

    private fun stroke(color: Int, width: Float) {
        p.style = Paint.Style.STROKE
        p.color = color
        p.strokeWidth = width
    }

    private fun diamond(c: Canvas, x: Float, y: Float, r: Float) {
        val style = p.style
        p.style = Paint.Style.FILL
        val d = diamondPath
        d.reset()
        d.moveTo(x - r, y)
        d.lineTo(x, y - r)
        d.lineTo(x + r, y)
        d.lineTo(x, y + r)
        d.close()
        c.drawPath(d, p)
        p.style = style
    }

    /** Draws centred text on [baseline], shrinking it to fit [maxWidth]. */
    private fun drawFitted(c: Canvas, text: String, face: Typeface, size: Float, color: Int, cx: Float, baseline: Float, maxWidth: Float) {
        if (maxWidth <= 0) return
        tp.typeface = face
        tp.color = color
        tp.textSize = size
        val m = tp.measureText(text)
        if (m > maxWidth) tp.textSize = size * maxWidth / m
        c.drawText(text, cx, baseline, tp)
    }
}
