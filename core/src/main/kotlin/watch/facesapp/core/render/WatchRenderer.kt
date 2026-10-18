package watch.facesapp.core.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.template.TemplateRegistry
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance dispatcher and renderer caching layer for watch faces.
 * Dynamically resolves renderers from [TemplateRegistry] by template ID,
 * caching instances for smooth 60 FPS previews and 1 Hz battery-conscious ticking.
 */
class WatchRenderer(private val context: Context) : TemplateRenderer {

    private val rendererCache = ConcurrentHashMap<String, TemplateRenderer>()

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        config: FaceConfig,
        calendar: Calendar,
        isAmbient: Boolean,
        shape: WatchShape,
        faceMode: FaceMode,
        stepCount: Long
    ) {
        val renderer = rendererCache.getOrPut(config.style.id) {
            val template = TemplateRegistry.get(config.style.id)
                ?: TemplateRegistry.defaultTemplate
            template.createRenderer(context)
        }

        renderer.render(
            canvas = canvas,
            bounds = bounds,
            config = config,
            calendar = calendar,
            isAmbient = isAmbient,
            shape = shape,
            faceMode = faceMode,
            stepCount = stepCount
        )
    }
}
