package watch.facesapp.wear.service

import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.TapEvent
import androidx.wear.watchface.TapType
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import watch.facesapp.core.model.FaceConfig
import watch.facesapp.core.model.FaceMode
import watch.facesapp.wear.datalayer.WearMessageListener
import watch.facesapp.wear.renderer.FacesCanvasRenderer

class FacesWatchFaceService : WatchFaceService() {

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager {
        return ComplicationSlotsManager(
            emptyList(),
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

        fun updateBattery() {
            val batteryFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = applicationContext.registerReceiver(null, batteryFilter)
            val level = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                val pct = (level * 100) / scale
                renderer.complicationState = renderer.complicationState.copy(batteryPercent = pct)
            }
        }
        updateBattery()

        // Interactive taps: cycle through active complications in our custom rendered face
        watchFace.setTapListener(object : WatchFace.TapListener {
            override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
                if (tapType == TapType.UP) {
                    updateBattery()
                    val activeCycle = renderer.faceConfig.activeComplications
                    renderer.faceMode = renderer.faceMode.next(activeCycle)
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
        var activeRenderer: FacesCanvasRenderer? = null

        fun updateConfig(newConfig: FaceConfig) {
            activeRenderer?.let { renderer ->
                renderer.faceConfig = newConfig
                renderer.invalidate()
            }
        }
    }
}
