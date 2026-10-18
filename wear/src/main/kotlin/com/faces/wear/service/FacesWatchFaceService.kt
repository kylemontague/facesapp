package com.faces.wear.service

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
import com.faces.core.model.FaceConfig
import com.faces.core.model.FaceMode
import com.faces.wear.datalayer.WearMessageListener
import com.faces.wear.renderer.FacesCanvasRenderer

class FacesWatchFaceService : WatchFaceService() {

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
            currentUserStyleRepository = currentUserStyleRepository
        )

        // Load saved face or defaults
        val initialConfig = WearMessageListener.loadSavedConfig(applicationContext)
        renderer.faceConfig = initialConfig

        activeRenderer = renderer

        val watchFace = WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )

        // Interactive taps toggle between Time and Date mode
        watchFace.setTapListener(object : WatchFace.TapListener {
            override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
                if (tapType == TapType.UP) {
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
        if (activeRenderer?.let { it == activeRenderer } == true) {
            activeRenderer = null
        }
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
