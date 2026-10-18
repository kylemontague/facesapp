package watch.facesapp.core.render.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.util.FontProvider
import watch.facesapp.core.util.TimeUtils
import java.util.Calendar
import watch.facesapp.core.render.TemplateRenderer
import kotlin.math.cos
import kotlin.math.sin

class BusinessRenderer(private val context: Context) : TemplateRenderer {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        config: FaceConfig,
        calendar: Calendar,
        isAmbient: Boolean,
        shape: WatchShape,
        faceMode: FaceMode,
        stepCount: Long,
        complicationState: watch.facesapp.core.model.ComplicationState
    ) {
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val width = bounds.width().toFloat()
        val radius = width / 2f

        // 1. Draw Background
        bgPaint.color = config.getBackgroundColor(isAmbient)
        if (shape == WatchShape.SQUARE) {
            canvas.drawRect(bounds, bgPaint)
        } else {
            canvas.drawCircle(centerX, centerY, radius, bgPaint)
        }

        if (faceMode == FaceMode.TIME) {
            val (hrRot, minRot, secRot) = TimeUtils.computeHandAngles(calendar)

            val secLength = radius * 0.90f
            val minLength = radius * 0.70f
            val hrLength = radius * 0.50f

            val hourStroke = (width * 0.018f).coerceAtLeast(6f)
            val minStroke = (width * 0.012f).coerceAtLeast(4f)
            val secStroke = (width * 0.006f).coerceAtLeast(2f)

            hourPaint.color = config.getHourColor(isAmbient)
            hourPaint.strokeWidth = hourStroke

            minutePaint.color = config.getMinuteColor(isAmbient)
            minutePaint.strokeWidth = minStroke

            secondPaint.color = config.getSecondColor(isAmbient)
            secondPaint.strokeWidth = secStroke

            centerDotPaint.color = config.getHourColor(isAmbient)

            // Second Hand (Interactive only)
            if (!isAmbient) {
                val secX = sin(secRot) * secLength
                val secY = -cos(secRot) * secLength
                canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, secondPaint)
            }

            // Minute Hand
            val minX = sin(minRot) * minLength
            val minY = -cos(minRot) * minLength
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, minutePaint)

            // Hour Hand
            val hrX = sin(hrRot) * hrLength
            val hrY = -cos(hrRot) * hrLength
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, hourPaint)

            // Center Pin
            canvas.drawCircle(centerX, centerY, hourStroke * 0.8f, centerDotPaint)

            // Ambient mode accent ring
            if (isAmbient) {
                centerDotPaint.style = Paint.Style.STROKE
                centerDotPaint.strokeWidth = 2f
                canvas.drawCircle(centerX, centerY, hourStroke * 1.4f, centerDotPaint)
                centerDotPaint.style = Paint.Style.FILL
            }
        } else {
            // Complications Display (Date / Battery / Steps / Heart Rate / Calories)
            textPaint.typeface = FontProvider.getProximaLight(context)
            textPaint.textSize = width * 0.09f
            textPaint.color = config.getMinuteColor(isAmbient)

            val effectiveSteps = if (stepCount > 0L) stepCount else complicationState.stepCount
            val text = when (faceMode) {
                FaceMode.DATE -> TimeUtils.formatDate(calendar.time, TimeUtils.SHORT_DATE)
                FaceMode.BATTERY -> "${complicationState.batteryPercent}% Battery"
                FaceMode.STEPS -> "$effectiveSteps Steps"
                FaceMode.HEART_RATE -> "${complicationState.heartRateBpm} BPM"
                FaceMode.CALORIES -> "${complicationState.caloriesBurned} kcal"
                FaceMode.TIME -> ""
            }
            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, centerX, textY, textPaint)
        }
    }
}
