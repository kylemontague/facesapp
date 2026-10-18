package com.faces.core.render.faces

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.faces.core.mesh.Trianglify
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.core.model.WatchShape
import com.faces.core.util.FontProvider
import com.faces.core.util.TimeUtils
import java.util.Calendar

class PrismRenderer(private val context: Context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val clipPath = Path()

    // Mesh bitmap cache
    private var cachedMeshBitmap: Bitmap? = null
    private var cachedColors: List<Int>? = null
    private var cachedWidth: Int = 0
    private var cachedHeight: Int = 0

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
        val rectF = RectF(bounds)

        // Clip to watch shape
        canvas.save()
        clipPath.reset()
        if (shape == WatchShape.SQUARE) {
            clipPath.addRect(rectF, Path.Direction.CW)
        } else {
            clipPath.addCircle(centerX, centerY, width / 2f, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

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
                cachedMeshBitmap = Trianglify.renderToBitmap(w, h, meshColors)
                cachedWidth = w
                cachedHeight = h
                cachedColors = meshColors
            }

            cachedMeshBitmap?.let {
                canvas.drawBitmap(it, bounds.left.toFloat(), bounds.top.toFloat(), bgPaint)
            }
        } else {
            // Ambient black
            bgPaint.color = 0xFF000000.toInt()
            canvas.drawPaint(bgPaint)
        }

        // Draw Digital Time / Date / Steps
        textPaint.typeface = FontProvider.getProximaSemibold(context)
        textPaint.textSize = width * 0.26f

        val textColor = config.getHourColor(isAmbient)
        textPaint.color = textColor

        if (isAmbient) {
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = (width * 0.005f).coerceAtLeast(2f)
        } else {
            textPaint.style = Paint.Style.FILL_AND_STROKE
            textPaint.strokeWidth = 1f
        }

        val displayText = when (faceMode) {
            FaceMode.TIME -> TimeUtils.formatDigitalTime(calendar)
            FaceMode.DATE -> TimeUtils.formatDate(calendar.time, TimeUtils.UK_DATE)
            FaceMode.STEPS -> "$stepCount"
        }

        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(displayText, centerX, textY, textPaint)

        canvas.restore()
    }
}
