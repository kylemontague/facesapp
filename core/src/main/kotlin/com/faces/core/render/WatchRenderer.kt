package com.faces.core.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.core.model.FaceStyle
import com.faces.core.model.WatchShape
import com.faces.core.render.faces.BusinessRenderer
import com.faces.core.render.faces.FillRenderer
import com.faces.core.render.faces.OUATRenderer
import com.faces.core.render.faces.PrismRenderer
import com.faces.core.render.faces.TwoTimeRenderer
import java.util.Calendar

class WatchRenderer(context: Context) {

    private val ouatRenderer = OUATRenderer(context)
    private val businessRenderer = BusinessRenderer(context)
    private val fillRenderer = FillRenderer(context)
    private val twoTimeRenderer = TwoTimeRenderer(context)
    private val prismRenderer = PrismRenderer(context)

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
        when (config.style) {
            FaceStyle.OUA_TIME -> ouatRenderer.render(
                canvas, bounds, config, calendar, isAmbient, shape, faceMode, stepCount
            )
            FaceStyle.BUSINESS_TIME -> businessRenderer.render(
                canvas, bounds, config, calendar, isAmbient, shape, faceMode, stepCount
            )
            FaceStyle.FILL_TIME -> fillRenderer.render(
                canvas, bounds, config, calendar, isAmbient, shape, faceMode, stepCount
            )
            FaceStyle.TWO_TIME -> twoTimeRenderer.render(
                canvas, bounds, config, calendar, isAmbient, shape, faceMode, stepCount
            )
            FaceStyle.PRISM_TIME -> prismRenderer.render(
                canvas, bounds, config, calendar, isAmbient, shape, faceMode, stepCount
            )
        }
    }
}
