package watch.facesapp.wear.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.RenderParameters
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.core.model.FaceStyle
import watch.facesapp.core.model.WatchShape
import watch.facesapp.core.render.WatchRenderer
import java.time.ZonedDateTime

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
    private val activeWatchState: WatchState = watchState
    private val coreRenderer = WatchRenderer(context)
    private val reusableCalendar: java.util.Calendar = java.util.Calendar.getInstance()

    var faceConfig: FaceConfig = FaceConfig()
        set(value) {
            field = value
            updateRefreshRate()
        }

    var faceMode: FaceMode = FaceMode.TIME
        set(value) {
            field = value
            updateRefreshRate()
        }

    var complicationState: watch.facesapp.core.model.ComplicationState = watch.facesapp.core.model.ComplicationState()

    init {
        updateRefreshRate()
    }

    override fun onRenderParametersChanged(renderParameters: RenderParameters) {
        super.onRenderParametersChanged(renderParameters)
        updateRefreshRate()
    }

    /**
     * Optimizes battery by adjusting the interactive redraw frequency.
     * Faces with seconds representations (OUAT second arc, Business second hand, Fill progressive gradient)
     * tick at 1 Hz (1000ms) in active TIME mode.
     * Faces without seconds (Two Time, Space-Time Prism), all complication display modes, and ambient mode
     * tick at 1/60 Hz (60,000ms), reducing CPU wakeups and canvas draws by up to 60x.
     */
    fun updateRefreshRate() {
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT
        val targetDelay = calculateUpdateDelayMillis(faceConfig.style, faceMode, isAmbient)
        if (interactiveDrawModeUpdateDelayMillis != targetDelay) {
            interactiveDrawModeUpdateDelayMillis = targetDelay
        }
    }

    companion object {
        /**
         * Determines the optimal refresh delay in milliseconds.
         * Returns 1000L (1 Hz) for faces displaying active seconds (OUAT ring, Business second hand, Fill progression),
         * and 60,000L (1/60 Hz) for all others, complications, and ambient mode.
         */
        fun calculateUpdateDelayMillis(
            style: FaceStyle,
            mode: FaceMode,
            isAmbient: Boolean
        ): Long {
            val hasSeconds = when {
                isAmbient -> false
                mode != FaceMode.TIME -> false
                style == FaceStyle.BUSINESS_TIME -> true
                style == FaceStyle.OUA_TIME -> true
                style == FaceStyle.FILL_TIME -> true
                else -> false
            }
            return if (hasSeconds) 1000L else 60_000L
        }
    }

    override suspend fun createSharedAssets(): SharedAssets {
        return object : SharedAssets {
            override fun onDestroy() = Unit
        }
    }

    private val lowBitAmbientDrawFilter = android.graphics.PaintFlagsDrawFilter(android.graphics.Paint.ANTI_ALIAS_FLAG, 0)

    private fun updateBattery() {
        val batteryFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, batteryFilter)
        val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) {
            val pct = (level * 100) / scale
            complicationState = complicationState.copy(batteryPercent = pct)
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

        if (!isAmbient) {
            updateBattery()
        }

        if (isAmbient) {
            if (faceMode != FaceMode.TIME) {
                faceMode = FaceMode.TIME
            }
            if (watch.facesapp.wear.service.FacesWatchFaceService.isPreviewActive) {
                watch.facesapp.wear.service.FacesWatchFaceService.releasePreviewWakeLock(context)
            }
        }
        updateRefreshRate()

        // Low-bit ambient optimization: disable anti-aliasing on 1-bit / low-color ambient hardware displays
        val prevFilter = canvas.drawFilter
        if (isAmbient && activeWatchState.hasLowBitAmbient) {
            canvas.drawFilter = lowBitAmbientDrawFilter
        }

        reusableCalendar.timeInMillis = zonedDateTime.toInstant().toEpochMilli()
        reusableCalendar.timeZone = java.util.TimeZone.getTimeZone(zonedDateTime.zone)

        try {
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
        } finally {
            canvas.drawFilter = prevFilter
        }
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
