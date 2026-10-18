package watch.facesapp.core.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.render.faces.BusinessRenderer
import watch.facesapp.core.render.faces.FillRenderer
import watch.facesapp.core.render.faces.OUATRenderer
import watch.facesapp.core.render.faces.PrismRenderer
import watch.facesapp.core.render.faces.TwoTimeRenderer
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
