package watch.facesapp.wear.service

import android.graphics.RectF
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.TapType
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.wear.datalayer.WearMessageListener
import watch.facesapp.wear.renderer.FacesCanvasRenderer

class FacesWatchFaceService : WatchFaceService() {

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager {
        val defaultCanvasComplicationFactory = CanvasComplicationFactory { watchState, listener ->
            val complicationDrawable = ComplicationDrawable(this@FacesWatchFaceService)
            CanvasComplicationDrawable(complicationDrawable, watchState, listener)
        }

        // Left Complication: Steps / Health / Short Text
        val leftComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = LEFT_COMPLICATION_ID,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_STEP_COUNT,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(RectF(0.10f, 0.39f, 0.32f, 0.61f))
        ).build()

        // Right Complication: Watch Battery / Short Text
        val rightComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = RIGHT_COMPLICATION_ID,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE,
                ComplicationType.MONOCHROMATIC_IMAGE,
                ComplicationType.SMALL_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_WATCH_BATTERY,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(RectF(0.68f, 0.39f, 0.90f, 0.61f))
        ).build()

        // Bottom Complication: Date / Next Event / Long Text
        val bottomComplication = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = BOTTOM_COMPLICATION_ID,
            canvasComplicationFactory = defaultCanvasComplicationFactory,
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.LONG_TEXT,
                ComplicationType.MONOCHROMATIC_IMAGE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_DATE,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(RectF(0.25f, 0.72f, 0.75f, 0.88f))
        ).build()

        return ComplicationSlotsManager(
            listOf(leftComplication, rightComplication, bottomComplication),
            currentUserStyleRepository
        )
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = FacesCanvasRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository,
            complicationSlotsManager = complicationSlotsManager
        )

        // Load saved face or defaults
        val initialConfig = WearMessageListener.loadSavedConfig(applicationContext)
        renderer.faceConfig = initialConfig

        activeRenderer = renderer

        val watchFace = WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )

        // Interactive taps: Complications are automatically handled by the framework; background taps toggle Time/Date mode
        watchFace.setTapListener(object : WatchFace.TapListener {
            override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
                if (tapType == TapType.UP && complicationSlot == null) {
                    renderer.faceMode = when (renderer.faceMode) {
                        FaceMode.TIME -> FaceMode.DATE
                        FaceMode.DATE -> FaceMode.TIME
                        FaceMode.STEPS -> FaceMode.TIME
                    }
                    renderer.invalidate()
                }
            }
        })

        return watchFace
    }

    override fun onDestroy() {
        super.onDestroy()
        activeRenderer = null
    }

    companion object {
        const val LEFT_COMPLICATION_ID = 100
        const val RIGHT_COMPLICATION_ID = 101
        const val BOTTOM_COMPLICATION_ID = 102

        var activeRenderer: FacesCanvasRenderer? = null

        fun updateConfig(newConfig: FaceConfig) {
            activeRenderer?.let { renderer ->
                renderer.faceConfig = newConfig
                renderer.invalidate()
            }
        }
    }
}
