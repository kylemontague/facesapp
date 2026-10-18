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
import watch.facesapp.core.render.TemplateRenderer
import java.util.Calendar

class OUATRenderer(private val context: Context) : TemplateRenderer {

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

    private val arcRect = RectF()

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
            arcRect.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawArc(arcRect, -90f, angle, false, secondArcPaint)
        }

        // 4. Resolve text values
        val effectiveSteps = if (stepCount > 0L) stepCount else complicationState.stepCount
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
            FaceMode.BATTERY -> {
                val words = NumbersToWords.convert(complicationState.batteryPercent)
                Pair(words, listOf("percent", "battery"))
            }
            FaceMode.STEPS -> {
                val words = NumbersToWords.convert(effectiveSteps.toInt().coerceAtMost(99999))
                Pair(words, listOf("steps"))
            }
            FaceMode.HEART_RATE -> {
                val words = NumbersToWords.convert(complicationState.heartRateBpm)
                Pair(words, listOf("beats per", "minute"))
            }
            FaceMode.CALORIES -> {
                val words = NumbersToWords.convert(complicationState.caloriesBurned)
                Pair(words, listOf("calories", "burned"))
            }
        }

        val allLines = listOf(hourText) + minuteLines

        // 5. Dynamic text sizing & bounds check
        // Move text more to the left (-0.28f from center)
        val xOffset = -width * 0.28f
        val startX = centerX + xOffset

        val maxFontSize = width * 0.145f
        val minFontSize = width * 0.065f

        var low = minFontSize
        var high = maxFontSize
        var bestFontSize = minFontSize

        if (checkFits(maxFontSize, startX, allLines, hourPaint, minutePaint, shape, centerX, centerY, width, height, bounds)) {
            bestFontSize = maxFontSize
        } else {
            repeat(8) {
                val mid = (low + high) / 2f
                if (checkFits(mid, startX, allLines, hourPaint, minutePaint, shape, centerX, centerY, width, height, bounds)) {
                    bestFontSize = mid
                    low = mid
                } else {
                    high = mid
                }
            }
        }

        // Apply final optimal font size
        hourPaint.textSize = bestFontSize
        minutePaint.textSize = bestFontSize

        val lineSpacing = bestFontSize * 1.05f
        val totalLines = allLines.size
        // Center the text block vertically
        val startY = centerY - 0.02f * height + 0.25f * bestFontSize - (totalLines - 1) * lineSpacing / 2f

        for (i in allLines.indices) {
            val lineY = startY + (i * lineSpacing)
            val paint = if (i == 0) hourPaint else minutePaint
            canvas.drawText(allLines[i], startX, lineY, paint)
        }
    }

    private fun checkFits(
        fontSize: Float,
        startX: Float,
        lines: List<String>,
        hPaint: Paint,
        mPaint: Paint,
        shape: WatchShape,
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        bounds: Rect
    ): Boolean {
        hPaint.textSize = fontSize
        mPaint.textSize = fontSize

        val lineSpacing = fontSize * 1.05f
        val totalLines = lines.size
        val startY = centerY - 0.02f * height + 0.25f * fontSize - (totalLines - 1) * lineSpacing / 2f

        val safeRadius = (width / 2f) * 0.86f
        val safeRadiusSq = safeRadius * safeRadius
        val safeMargin = width * 0.07f

        val rect = Rect()

        for (i in lines.indices) {
            val text = lines[i]
            val paint = if (i == 0) hPaint else mPaint
            val lineY = startY + (i * lineSpacing)

            paint.getTextBounds(text, 0, text.length, rect)
            val textWidth = maxOf(rect.width().toFloat(), paint.measureText(text))

            val left = startX + rect.left
            val right = startX + rect.left + textWidth
            val top = lineY + rect.top
            val bottom = lineY + rect.bottom

            if (shape == WatchShape.SQUARE) {
                if (left < bounds.left + safeMargin ||
                    right > bounds.right - safeMargin ||
                    top < bounds.top + safeMargin ||
                    bottom > bounds.bottom - safeMargin
                ) {
                    return false
                }
            } else {
                // Round watch face: verify all 4 corners are inside safe radius
                val corners = arrayOf(
                    left to top,
                    right to top,
                    left to bottom,
                    right to bottom
                )
                for ((cx, cy) in corners) {
                    val dx = cx - centerX
                    val dy = cy - centerY
                    if (dx * dx + dy * dy > safeRadiusSq) {
                        return false
                    }
                }
            }
        }
        return true
    }
}
