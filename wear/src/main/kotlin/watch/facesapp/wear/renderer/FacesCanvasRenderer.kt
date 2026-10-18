package watch.facesapp.wear.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.render.WatchRenderer
import java.time.ZonedDateTime

class FacesCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
    private val complicationSlotsManager: ComplicationSlotsManager,
    canvasType: Int = CanvasType.HARDWARE
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = canvasType,
    interactiveDrawModeUpdateDelayMillis = 1000L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = false
) {
    private val coreRenderer = WatchRenderer(context)
    private val reusableCalendar: java.util.Calendar = java.util.Calendar.getInstance()
    var faceConfig: FaceConfig = FaceConfig()
    var faceMode: FaceMode = FaceMode.TIME
    var complicationState: watch.facesapp.core.model.ComplicationState = watch.facesapp.core.model.ComplicationState()

    override suspend fun createSharedAssets(): SharedAssets {
        return object : SharedAssets {
            override fun onDestroy() = Unit
        }
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        val shape = if (context.resources.configuration.isScreenRound) WatchShape.ROUND else WatchShape.SQUARE

        reusableCalendar.timeInMillis = zonedDateTime.toInstant().toEpochMilli()
        reusableCalendar.timeZone = java.util.TimeZone.getTimeZone(zonedDateTime.zone)

        // 1. Draw watch face background, hands, numerals
        coreRenderer.render(
            canvas = canvas,
            bounds = bounds,
            config = faceConfig,
            calendar = reusableCalendar,
            isAmbient = isAmbient,
            shape = shape,
            faceMode = faceMode,
            complicationState = complicationState
        )

    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val highlight = renderParameters.highlightLayer ?: return
        canvas.drawColor(highlight.backgroundTint)
    }
}
