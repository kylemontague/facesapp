package watch.facesapp.core.render

import android.graphics.Canvas
import android.graphics.Rect
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import java.util.Calendar

/**
 * Standard rendering contract for watch face templates.
 * Implemented by template renderers to draw onto Android Canvas on both mobile preview and Wear OS.
 */
interface TemplateRenderer {
    fun render(
        canvas: Canvas,
        bounds: Rect,
        config: FaceConfig,
        calendar: Calendar,
        isAmbient: Boolean,
        shape: WatchShape,
        faceMode: FaceMode = FaceMode.TIME,
        stepCount: Long = 0L
    )
}
