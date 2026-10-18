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
import watch.facesapp.wear.calendar.CalendarEventManager
import watch.facesapp.wear.sensor.HealthSensorManager

class FacesWatchFaceService : WatchFaceService() {

    private var healthSensorManager: HealthSensorManager? = null

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

        // Live Health Sensors (Steps, Heart Rate, Estimated Calories)
        healthSensorManager = HealthSensorManager(applicationContext) { healthState ->
            renderer.complicationState = renderer.complicationState.copy(
                stepCount = healthState.stepCount,
                heartRateBpm = healthState.heartRateBpm,
                caloriesBurned = healthState.caloriesBurned,
                distanceKm = healthState.distanceKm,
                activeMinutes = healthState.activeMinutes,
                sunriseSunsetSummary = healthState.sunriseSunsetSummary,
                sunriseSunsetIsSunset = healthState.sunriseSunsetIsSunset
            )
            if (renderer.faceMode in listOf(
                    FaceMode.STEPS,
                    FaceMode.HEART_RATE,
                    FaceMode.CALORIES,
                    FaceMode.DISTANCE,
                    FaceMode.ACTIVE_MINUTES,
                    FaceMode.SUNRISE_SUNSET
                )
            ) {
                renderer.invalidate()
            }
        }.apply {
            start()
        }
        activeHealthManager = healthSensorManager

        // Live Calendar Event Check
        val calendarEventManager = CalendarEventManager(applicationContext)
        fun refreshCalendar() {
            val event = calendarEventManager.getNextEvent()
            if (event != null) {
                renderer.complicationState = renderer.complicationState.copy(
                    nextEventTitle = event.title,
                    nextEventMinutes = event.minutesUntil
                )
            }
        }
        refreshCalendar()

        // Load Cached Weather only if previously received from companion phone
        val weatherPrefs = applicationContext.getSharedPreferences("faces_weather_prefs", android.content.Context.MODE_PRIVATE)
        if (weatherPrefs.contains("temp") && weatherPrefs.contains("condition")) {
            val savedTemp = weatherPrefs.getInt("temp", 0)
            val savedCond = weatherPrefs.getString("condition", null)
            if (!savedCond.isNullOrBlank()) {
                renderer.complicationState = renderer.complicationState.copy(
                    weatherTempC = savedTemp,
                    weatherCondition = savedCond
                )
            }
        }

        // Interactive taps: cycle through active complications in our custom rendered face
        watchFace.setTapListener(object : WatchFace.TapListener {
            override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
                if (tapType == TapType.UP) {
                    updateBattery()
                    refreshCalendar()

                    // Request runtime sensor / calendar permissions if needed
                    watch.facesapp.wear.permission.WearPermissionActivity.checkAndRequest(applicationContext)

                    val activeCycle = renderer.faceConfig.activeComplications
                    renderer.faceMode = renderer.faceMode.nextAvailable(activeCycle, renderer.complicationState)
                    renderer.invalidate()

                    // Reset auto-revert timer to return to TIME (start of cycle) after 5 seconds of inactivity
                    complicationRevertRunnable?.let { tapHandler.removeCallbacks(it) }
                    if (renderer.faceMode != FaceMode.TIME) {
                        val runnable = Runnable {
                            if (renderer.faceMode != FaceMode.TIME) {
                                renderer.faceMode = FaceMode.TIME
                                renderer.invalidate()
                            }
                        }
                        complicationRevertRunnable = runnable
                        tapHandler.postDelayed(runnable, 5000L)
                    }
                }
            }
        })

        return watchFace
    }

    private val tapHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var complicationRevertRunnable: Runnable? = null

    override fun onDestroy() {
        super.onDestroy()
        complicationRevertRunnable?.let { tapHandler.removeCallbacks(it) }
        complicationRevertRunnable = null
        healthSensorManager?.stop()
        healthSensorManager = null
        activeHealthManager = null
        activeRenderer = null
    }

    companion object {
        var activeRenderer: FacesCanvasRenderer? = null
        var activeHealthManager: HealthSensorManager? = null
        private var previewWakeLock: android.os.PowerManager.WakeLock? = null
        private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        private var autoRevertRunnable: Runnable? = null

        fun onPermissionsGranted(context: android.content.Context) {
            activeHealthManager?.restart()
        }

        @Synchronized
        fun wakeUpForPreview(context: android.content.Context, timeoutMs: Long = 30_000L) {
            try {
                val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager ?: return
                if (previewWakeLock == null) {
                    @Suppress("DEPRECATION")
                    previewWakeLock = powerManager.newWakeLock(
                        android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                        android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        android.os.PowerManager.ON_AFTER_RELEASE,
                        "faces:preview_wakelock"
                    ).apply {
                        setReferenceCounted(false)
                    }
                }
                previewWakeLock?.acquire(timeoutMs)

                // Schedule auto-revert if no new preview updates arrive
                autoRevertRunnable?.let { mainHandler.removeCallbacks(it) }
                val runnable = Runnable {
                    revertToSavedConfig(context)
                }
                autoRevertRunnable = runnable
                mainHandler.postDelayed(runnable, timeoutMs + 2000L)
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to acquire preview wake lock", e)
            }
        }

        @Synchronized
        fun releasePreviewWakeLock(context: android.content.Context? = null) {
            try {
                autoRevertRunnable?.let { mainHandler.removeCallbacks(it) }
                autoRevertRunnable = null
                if (previewWakeLock?.isHeld == true) {
                    previewWakeLock?.release()
                }
                context?.let { revertToSavedConfig(it) }
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to release preview wake lock", e)
            }
        }

        fun revertToSavedConfig(context: android.content.Context) {
            try {
                val savedConfig = WearMessageListener.loadSavedConfig(context)
                updateConfig(savedConfig)
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to revert to saved config", e)
            }
        }

        fun updateWeather(tempC: Int, condition: String) {
            activeRenderer?.let { renderer ->
                renderer.complicationState = renderer.complicationState.copy(
                    weatherTempC = tempC,
                    weatherCondition = condition
                )
                if (renderer.faceMode == FaceMode.WEATHER) {
                    renderer.invalidate()
                }
            }
        }

        fun updateCalendarEvent(title: String, minutesUntil: Int) {
            activeRenderer?.let { renderer ->
                renderer.complicationState = renderer.complicationState.copy(
                    nextEventTitle = title,
                    nextEventMinutes = minutesUntil
                )
                if (renderer.faceMode == FaceMode.NEXT_EVENT) {
                    renderer.invalidate()
                }
            }
        }

        fun updateConfig(newConfig: FaceConfig) {
            activeRenderer?.let { renderer ->
                renderer.faceConfig = newConfig
                renderer.invalidate()
            }
        }
    }
}
