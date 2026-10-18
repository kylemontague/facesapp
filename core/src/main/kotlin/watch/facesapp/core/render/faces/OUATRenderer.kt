package watch.facesapp.core.render.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.util.FontProvider
import watch.facesapp.core.util.NumbersToWords
import watch.facesapp.core.util.TimeUtils
import java.util.Calendar

class OUATRenderer(private val context: Context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val secondArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

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

        // 1. Draw Background
        bgPaint.color = config.getBackgroundColor(isAmbient)
        if (shape == WatchShape.SQUARE) {
            canvas.drawRect(bounds, bgPaint)
        } else {
            canvas.drawCircle(centerX, centerY, width / 2f, bgPaint)
        }

        // 2. Setup typography & paint colors
        val hourColor = config.getHourColor(isAmbient)
        val secondColor = config.getSecondColor(isAmbient)

        val proxiSemi = FontProvider.getProximaSemibold(context)
        val proxiLight = FontProvider.getProximaLight(context)

        val fontSize = width * 0.11f

        hourPaint.color = hourColor
        hourPaint.textSize = fontSize
        hourPaint.typeface = if (isAmbient) proxiLight else proxiSemi

        minutePaint.color = hourColor
        minutePaint.textSize = fontSize
        minutePaint.typeface = proxiLight

        secondArcPaint.color = secondColor
        secondArcPaint.strokeWidth = (width * 0.008f).coerceAtLeast(3f)

        // 3. Draw second counter arc in active mode
        if (!isAmbient && faceMode == FaceMode.TIME) {
            val radius = (width / 2f) * 0.9f
            val seconds = calendar.get(Calendar.SECOND)
            val angle = seconds * 6f
            val arcRect = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawArc(arcRect, -90f, angle, false, secondArcPaint)
        }

        // 4. Resolve text values
        val (hourText, minuteLines) = when (faceMode) {
            FaceMode.TIME -> {
                val h = calendar.get(Calendar.HOUR)
                val m = calendar.get(Calendar.MINUTE)
                NumbersToWords.getHoursAndMinutesWords(h, m)
            }
            FaceMode.DATE -> {
                val month = TimeUtils.formatDate(calendar.time, TimeUtils.MONTH_ONLY).lowercase()
                val day = NumbersToWords.convert(calendar.get(Calendar.DAY_OF_MONTH))
                Pair(month, day.split(" ").filter { it.isNotBlank() })
            }
            FaceMode.STEPS -> {
                Pair(stepCount.toString(), listOf("steps"))
            }
        }

        // 5. Draw text layout with signature offset
        val xOffset = -width * 0.18f
        val yOffset = -height * 0.10f

        val startX = centerX + xOffset
        val startY = centerY + yOffset

        canvas.drawText(hourText, startX, startY, hourPaint)

        for (i in minuteLines.indices) {
            val lineY = centerY + (i * (height / 6.2f))
            canvas.drawText(minuteLines[i], startX, lineY, minutePaint)
        }
    }
}
