package com.faces.wear.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.core.model.WatchShape
import com.faces.core.render.WatchRenderer
import java.time.ZonedDateTime
import java.util.GregorianCalendar

class FacesCanvasRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository,
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
    var faceConfig: FaceConfig = FaceConfig()
    var faceMode: FaceMode = FaceMode.TIME

    override suspend fun createSharedAssets(): Renderer.SharedAssets {
        return object : Renderer.SharedAssets {
            override fun onDestroy() = Unit
        }
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: Renderer.SharedAssets
    ) {
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        val shape = if (context.resources.configuration.isScreenRound) WatchShape.ROUND else WatchShape.SQUARE

        val calendar = GregorianCalendar.from(zonedDateTime)

        coreRenderer.render(
            canvas = canvas,
            bounds = bounds,
            config = faceConfig,
            calendar = calendar,
            isAmbient = isAmbient,
            shape = shape,
            faceMode = faceMode
        )
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: Renderer.SharedAssets
    ) {
        // Unused for Canvas watchface without complication slots
    }
}
