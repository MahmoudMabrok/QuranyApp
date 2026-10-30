package education.mahmoud.quranyapp.feature.showSuraAyas.mushaf

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import education.mahmoud.quranyapp.utils.NumberHelper
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ornamental Mushaf page frame drawn behind the page text.
 *
 * The outer side (right on odd / right-hand pages, left on even / left-hand pages) gets a
 * thicker roped band, the page-number medallion and the hizb medallion, like a printed
 * Mushaf spread. Only the frame is mirrored; the text is never touched.
 *
 * @param unit size of one design unit in px (about 1dp, smaller on narrow screens).
 * @param hizbCenterY returns the y (in drawable coordinates) of the line where a hizb quarter
 * starts, or null when unknown (then the medallion is centred vertically).
 */
class MushafFrameDrawable(
    private val palette: MushafPalette,
    private val fonts: MushafFonts,
    private val unit: Float,
    private val pageNum: Int,
    private val hizb: HizbMark?,
    private val hizbCenterY: () -> Float? = { null },
) : Drawable() {

    private val rightPage = isRightPage(pageNum)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val path = Path()
    private val frame = RectF()
    private val inner = RectF()

    // geometry in design units
    private val outerMargin get() = 9f * unit
    private val innerMargin get() = 3f * unit
    private val topMargin get() = 2f * unit
    private val bottomMargin get() = 19f * unit
    private val bandOuter get() = 12f * unit
    private val bandThin get() = 6f * unit
    private val medallionR get() = 16f * unit
    private val textGap get() = 5f * unit

    /** Padding the text view needs so the text stays inside the frame. */
    fun contentPadding(out: Rect) {
        val outerPad = (outerMargin + bandOuter + textGap).toInt()
        val innerPad = (innerMargin + bandThin + textGap).toInt()
        out.left = if (rightPage) innerPad else outerPad
        out.right = if (rightPage) outerPad else innerPad
        out.top = (topMargin + bandThin + textGap).toInt()
        out.bottom = (bottomMargin + bandThin + textGap).toInt()
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.width() <= 0 || b.height() <= 0) return
        val leftM = if (rightPage) innerMargin else outerMargin
        val rightM = if (rightPage) outerMargin else innerMargin
        frame.set(b.left + leftM, b.top + topMargin, b.right - rightM, b.bottom - bottomMargin)
        val bl = if (rightPage) bandThin else bandOuter
        val br = if (rightPage) bandOuter else bandThin
        inner.set(frame.left + bl, frame.top + bandThin, frame.right - br, frame.bottom - bandThin)

        // band fill (frame minus inner area)
        path.reset()
        path.fillType = Path.FillType.EVEN_ODD
        path.addRoundRect(frame, 4 * unit, 4 * unit, Path.Direction.CW)
        path.addRect(inner, Path.Direction.CW)
        fill(palette.band)
        canvas.drawPath(path, p)

        // thin bands: little diamonds
        fill(palette.line)
        p.alpha = 120
        var x = inner.left + 6 * unit
        while (x < inner.right - 3 * unit) {
            diamond(canvas, x, frame.top + bandThin / 2, 1.5f * unit)
            diamond(canvas, x, frame.bottom - bandThin / 2, 1.5f * unit)
            x += 9 * unit
        }
        val thinX = if (rightPage) frame.left + bandThin / 2 else frame.right - bandThin / 2
        var y = inner.top + 6 * unit
        while (y < inner.bottom - 3 * unit) {
            diamond(canvas, thinX, y, 1.5f * unit)
            y += 9 * unit
        }
        p.alpha = 255

        // thick outer band: rope of beads with a centre line
        val ropeX = if (rightPage) inner.right + bandOuter / 2 else inner.left - bandOuter / 2
        stroke(palette.line, 0.8f * unit)
        p.alpha = 150
        canvas.drawLine(ropeX, inner.top, ropeX, inner.bottom, p)
        p.alpha = 255
        y = inner.top + 7 * unit
        while (y < inner.bottom - 4 * unit) {
            fill(palette.cream)
            canvas.drawCircle(ropeX, y, 2.8f * unit, p)
            stroke(palette.line, 0.9f * unit)
            canvas.drawCircle(ropeX, y, 2.8f * unit, p)
            fill(palette.line)
            canvas.drawCircle(ropeX, y, 1f * unit, p)
            y += 11 * unit
        }

        // borders: thick outside line, double rule inside
        stroke(palette.line, 2f * unit)
        canvas.drawRoundRect(frame, 4 * unit, 4 * unit, p)
        stroke(palette.line, 1.2f * unit)
        canvas.drawRect(inner, p)
        stroke(palette.line, 0.6f * unit)
        p.alpha = 160
        canvas.drawRect(
            inner.left + 2.5f * unit, inner.top + 2.5f * unit,
            inner.right - 2.5f * unit, inner.bottom - 2.5f * unit, p,
        )
        p.alpha = 255

        // corner ornaments
        fill(palette.line)
        for (cx in floatArrayOf(inner.left, inner.right)) {
            for (cy in floatArrayOf(inner.top, inner.bottom)) diamond(canvas, cx, cy, 3.2f * unit)
        }

        // page-number medallion at the bottom outer corner
        val pnX = if (rightPage) frame.right - 30 * unit else frame.left + 30 * unit
        rosette(canvas, pnX, frame.bottom, medallionR)
        text(canvas, NumberHelper.getArabicNumber(pageNum), fonts.valueBold, 11.5f * unit, palette.value, pnX, frame.bottom + 4.2f * unit, medallionR * 1.2f)

        // hizb medallion on the outer band
        hizb?.let { mark ->
            val r = medallionR * 0.95f
            val hy = (hizbCenterY() ?: frame.centerY()).coerceIn(inner.top + r + 4 * unit, inner.bottom - r - 4 * unit)
            rosette(canvas, ropeX, hy, r)
            text(canvas, mark.title, fonts.caption, 6.3f * unit, palette.muted, ropeX, hy - 1f * unit, r * 1.35f)
            text(canvas, mark.numberArabic, fonts.valueBold, 9f * unit, palette.value, ropeX, hy + 8f * unit, r * 1.3f)
        }
    }

    /** Eight-point star with a round centre, like the medallions in a printed Mushaf. */
    private fun rosette(c: Canvas, cx: Float, cy: Float, r: Float) {
        val s = r * 0.74f
        path.reset()
        path.fillType = Path.FillType.WINDING
        for (i in 0 until 16) {
            val a = Math.PI / 8 * i - Math.PI / 2
            val rr = if (i % 2 == 0) r else s * 1.02f
            val px = cx + (rr * cos(a)).toFloat()
            val py = cy + (rr * sin(a)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        fill(palette.cream)
        c.drawPath(path, p)
        stroke(palette.line, 1.3f * unit)
        c.drawPath(path, p)
        fill(palette.paper)
        c.drawCircle(cx, cy, s * 0.92f, p)
        fill(palette.cream)
        p.alpha = 140
        c.drawCircle(cx, cy, s * 0.92f, p)
        p.alpha = 255
        stroke(palette.line, 0.9f * unit)
        c.drawCircle(cx, cy, s * 0.92f, p)
    }

    private val diamondPath = Path()
    private fun diamond(c: Canvas, x: Float, y: Float, r: Float) {
        diamondPath.reset()
        diamondPath.moveTo(x - r, y)
        diamondPath.lineTo(x, y - r)
        diamondPath.lineTo(x + r, y)
        diamondPath.lineTo(x, y + r)
        diamondPath.close()
        val st = p.style
        p.style = Paint.Style.FILL
        c.drawPath(diamondPath, p)
        p.style = st
    }

    private fun text(c: Canvas, s: String, face: android.graphics.Typeface, size: Float, color: Int, cx: Float, baseline: Float, maxW: Float) {
        tp.typeface = face
        tp.color = color
        tp.textSize = size
        val m = tp.measureText(s)
        if (m > maxW) tp.textSize = size * maxW / m
        c.drawText(s, cx, baseline, tp)
    }

    private fun fill(color: Int) {
        p.style = Paint.Style.FILL
        p.color = color
    }

    private fun stroke(color: Int, w: Float) {
        p.style = Paint.Style.STROKE
        p.color = color
        p.strokeWidth = w
    }

    override fun setAlpha(alpha: Int) {
        p.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        p.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
