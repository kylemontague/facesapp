package watch.facesapp.core.render.faces

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import watch.facesapp.core.mesh.Trianglify
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.util.FontProvider
import watch.facesapp.core.util.TimeUtils
import java.util.Calendar
import androidx.core.graphics.withSave
import watch.facesapp.core.render.TemplateRenderer

class PrismRenderer(private val context: Context) : TemplateRenderer {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val clipPath = Path()
    private val rectF = RectF()

    // Mesh bitmap cache
    private var cachedMeshBitmap: Bitmap? = null
    private var cachedColors: List<Int>? = null
    private var cachedWidth: Int = 0
    private var cachedHeight: Int = 0

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

            if (!isAmbient) {
                val meshColors = listOf(
                    config.getBackgroundColor(isAmbient = false),
                    config.getAccentColor(isAmbient = false),
                    config.getMeridianColor(isAmbient = false)
                )

                val w = bounds.width()
                val h = bounds.height()

                // Check if cached mesh can be reused
                if (cachedMeshBitmap == null ||
                    cachedWidth != w ||
                    cachedHeight != h ||
                    cachedColors != meshColors
                ) {
                    cachedMeshBitmap?.recycle()
                    cachedMeshBitmap = Trianglify.renderToBitmap(w, h, meshColors)
                    cachedWidth = w
                    cachedHeight = h
                    cachedColors = meshColors
                }

                cachedMeshBitmap?.let {
                    drawBitmap(it, bounds.left.toFloat(), bounds.top.toFloat(), bgPaint)
                }
            } else {
                // Ambient black
                bgPaint.color = 0xFF000000.toInt()
                drawPaint(bgPaint)
            }

            // Draw Digital Time / Date / Complications
            textPaint.typeface = FontProvider.getProximaSemibold(context)

            val textColor = config.getHourColor(isAmbient)
            textPaint.color = textColor

            if (isAmbient) {
                textPaint.style = Paint.Style.STROKE
                textPaint.strokeWidth = (width * 0.005f).coerceAtLeast(2f)
            } else {
                textPaint.style = Paint.Style.FILL_AND_STROKE
                textPaint.strokeWidth = 1f
            }

            val effectiveSteps = if (stepCount > 0L) stepCount else complicationState.stepCount
            val displayText = when (faceMode) {
                FaceMode.TIME -> TimeUtils.formatDigitalTime(calendar)
                FaceMode.DATE -> TimeUtils.formatDate(calendar.time, TimeUtils.UK_DATE)
                FaceMode.BATTERY -> "${complicationState.batteryPercent}%"
                FaceMode.STEPS -> "$effectiveSteps"
                FaceMode.HEART_RATE -> "${complicationState.heartRateBpm} bpm"
                FaceMode.CALORIES -> "${complicationState.caloriesBurned} kcal"
            }

            textPaint.textSize = if (displayText.length > 5) width * 0.19f else width * 0.26f

            val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
            drawText(displayText, centerX, textY, textPaint)

        }
    }
}
