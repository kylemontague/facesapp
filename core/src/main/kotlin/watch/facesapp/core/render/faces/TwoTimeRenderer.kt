package watch.facesapp.core.render.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.util.FontProvider
import watch.facesapp.core.util.NumbersToWords
import watch.facesapp.core.util.TimeUtils
import java.util.Calendar
import androidx.core.graphics.withSave
import java.util.Locale

class TwoTimeRenderer(private val context: Context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }

    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val clipPath = Path()
    private val rectF = RectF()

    fun render(
        canvas: Canvas,
        bounds: Rect,
        config: FaceConfig,
        calendar: Calendar,
        isAmbient: Boolean,
        shape: WatchShape,
        faceMode: FaceMode = FaceMode.TIME,
        stepCount: Long = 0L
    ) {
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        rectF.set(bounds)

        // Clip to watch shape
        canvas.withSave {
            clipPath.reset()
            if (shape == WatchShape.SQUARE) {
                clipPath.addRect(rectF, Path.Direction.CW)
            } else {
                clipPath.addCircle(centerX, centerY, width / 2f, Path.Direction.CW)
            }
            clipPath(clipPath)

            // 1. Draw Diagonal Linear Gradient Background
            if (!isAmbient) {
                val startColor = config.getBackgroundColor(isAmbient = false)
                val endColor = config.getAccentColor(isAmbient = false)
                bgPaint.shader = LinearGradient(
                    rectF.left, rectF.bottom, rectF.right, rectF.top,
                    startColor, endColor, Shader.TileMode.CLAMP
                )
                drawPaint(bgPaint)
                bgPaint.shader = null
            } else {
                bgPaint.shader = null
                bgPaint.color = 0xFF000000.toInt()
                drawPaint(bgPaint)
            }

            // 2. Setup typography & text
            val proxiSemi = FontProvider.getProximaSemibold(context)
            val quickBold = FontProvider.getQuicksandBold(context)

            val hourTextSize = width * 0.22f
            val minuteTextSize = width * 0.08f

            val textColor = config.getHourColor(isAmbient)

            hourPaint.color = textColor
            hourPaint.textSize = hourTextSize
            hourPaint.typeface = proxiSemi
            if (isAmbient) {
                hourPaint.style = Paint.Style.STROKE
                hourPaint.strokeWidth = (width * 0.005f).coerceAtLeast(2f)
            } else {
                hourPaint.style = Paint.Style.FILL_AND_STROKE
                hourPaint.strokeWidth = 1f
            }

            minutePaint.color = textColor
            minutePaint.textSize = minuteTextSize
            minutePaint.typeface = quickBold
            minutePaint.style = Paint.Style.FILL

            linePaint.color = textColor
            linePaint.strokeWidth = (width * 0.006f).coerceAtLeast(2f)

            // 3. Resolve display strings
            val (topText, bottomText) = when (faceMode) {
                FaceMode.TIME -> {
                    val hourStr = TimeUtils.formatTwoDigitHour(calendar)
                    val minStr = NumbersToWords.getMinutesCompact(calendar.get(Calendar.MINUTE))
                    Pair(hourStr, minStr)
                }

                FaceMode.DATE -> {
                    val day = TimeUtils.formatDate(calendar.time, TimeUtils.DAY_ONLY)
                    val month =
                        TimeUtils.formatDate(calendar.time, TimeUtils.MONTH_ONLY).lowercase()
                    Pair(day, month)
                }

                FaceMode.STEPS -> {
                    val stepStr = if (stepCount >= 10_000) {
                        String.format(Locale.getDefault(),"%.1fK", stepCount / 1000f)
                    } else {
                        stepCount.toString()
                    }
                    Pair(stepStr, "steps")
                }
            }

            // 4. Measure layout
            val hourWidth = hourPaint.measureText(topText)
            val minWidth = minutePaint.measureText(bottomText)
            val maxLineWidth = maxOf(hourWidth, minWidth) * 1.15f

            val halfLineWidth = maxLineWidth / 2f

            // Draw top digits
            val topY = centerY - height * 0.04f
            drawText(topText, centerX, topY, hourPaint)

            // Draw divider line 1
            val line1Y = topY + (height * 0.03f)
            drawLine(centerX - halfLineWidth, line1Y, centerX + halfLineWidth, line1Y, linePaint)

            // Draw bottom text
            val bottomY = line1Y + (height * 0.10f)
            drawText(bottomText, centerX, bottomY, minutePaint)

            // Draw divider line 2
            val line2Y = bottomY + (height * 0.03f)
            drawLine(centerX - halfLineWidth, line2Y, centerX + halfLineWidth, line2Y, linePaint)

        }
    }
}
