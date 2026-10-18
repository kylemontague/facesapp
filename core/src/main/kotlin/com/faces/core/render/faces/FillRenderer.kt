package com.faces.core.render.faces

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.core.model.WatchShape
import com.faces.core.util.FontProvider
import com.faces.core.util.TimeUtils
import java.util.Calendar

class FillRenderer(private val context: Context) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        isDither = true
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isDither = true
    }

    private val clipPath = Path()

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

        val bgColor = config.getBackgroundColor(isAmbient)
        val accentColor = config.getAccentColor(isAmbient)

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
            // Seconds progression split (0.0 .. 1.0)
            val seconds = calendar.get(Calendar.SECOND)
            val split = ((seconds + 1f) / 61f).coerceIn(0f, 1f)

            val isEvenMinute = calendar.get(Calendar.MINUTE) % 2 == 0
            val (c1, c2) = if (isEvenMinute) {
                Pair(accentColor, bgColor)
            } else {
                Pair(bgColor, accentColor)
            }

            // Dual tone step gradient shader for background
            val bgColors = intArrayOf(c1, c1, c2, c2)
            val bgPositions = floatArrayOf(0f, split, split, 1f)
            bgPaint.shader = LinearGradient(
                rectF.left, rectF.bottom, rectF.right, rectF.top,
                bgColors, bgPositions, Shader.TileMode.CLAMP
            )
            canvas.drawPaint(bgPaint)
            bgPaint.shader = null

            // Inverted dual tone shader for text
            val txtColors = intArrayOf(c2, c2, c1, c1)
            val txtPositions = floatArrayOf(0f, split, split, 1f)
            textPaint.shader = LinearGradient(
                rectF.left, rectF.bottom, rectF.right, rectF.top,
                txtColors, txtPositions, Shader.TileMode.CLAMP
            )
            textPaint.style = Paint.Style.FILL
        } else {
            // Ambient mode: deep black background + hollow stroke text
            bgPaint.color = 0xFF000000.toInt()
            canvas.drawPaint(bgPaint)

            textPaint.shader = null
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.style = Paint.Style.STROKE
            textPaint.strokeWidth = (width * 0.005f).coerceAtLeast(2f)
        }

        // Draw Time / Date / Steps text
        textPaint.typeface = FontProvider.getProximaSemibold(context)
        textPaint.textSize = width * 0.26f

        val displayText = when (faceMode) {
            FaceMode.TIME -> TimeUtils.formatDigitalTime(calendar)
            FaceMode.DATE -> TimeUtils.formatDate(calendar.time, TimeUtils.UK_DATE)
            FaceMode.STEPS -> "$stepCount"
        }

        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(displayText, centerX, textY, textPaint)

        textPaint.shader = null
        canvas.restore()
    }
}
