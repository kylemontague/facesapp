package watch.facesapp.wear.service

import android.graphics.RectF
import android.view.SurfaceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import androidx.wear.watchface.CanvasComplicationFactory
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
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

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var healthSensorManager: HealthSensorManager? = null
    private var batteryReceiver: android.content.BroadcastReceiver? = null

    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager {
        val stepSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = STEP_SLOT_ID,
            canvasComplicationFactory = CanvasComplicationFactory { watchState, invalidateCallback ->
                CanvasComplicationDrawable(
                    ComplicationDrawable(this@FacesWatchFaceService),
                    watchState,
                    invalidateCallback
                )
            },
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE,
                ComplicationType.GOAL_PROGRESS
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_STEP_COUNT,
                ComplicationType.SHORT_TEXT
            ),
            bounds = ComplicationSlotBounds(RectF(0f, 0f, 0.1f, 0.1f))
        ).build()

        val heartRateSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id = HEART_RATE_SLOT_ID,
            canvasComplicationFactory = CanvasComplicationFactory { watchState, invalidateCallback ->
                CanvasComplicationDrawable(
                    ComplicationDrawable(this@FacesWatchFaceService),
                    watchState,
                    invalidateCallback
                )
            },
            supportedTypes = listOf(
                ComplicationType.SHORT_TEXT,
                ComplicationType.RANGED_VALUE
            ),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(),
            bounds = ComplicationSlotBounds(RectF(0f, 0f, 0.1f, 0.1f))
        ).build()

        return ComplicationSlotsManager(
            listOf(stepSlot, heartRateSlot),
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
        syncStatusToDataClient(applicationContext, true)

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

        // Load Cached Weather only if previously received from companion phone
        val weatherPrefs = applicationContext.getSharedPreferences("faces_weather_prefs", android.content.Context.MODE_PRIVATE)
        if (weatherPrefs.contains("temp") && weatherPrefs.contains("condition")) {
            val savedTemp = weatherPrefs.getInt("temp", 0)
            val savedCond = weatherPrefs.getString("condition", null)
            val savedSunSummary = weatherPrefs.getString("sun_summary", null)
            val savedIsSunset = weatherPrefs.getBoolean("is_sunset", true)
            if (!savedCond.isNullOrBlank()) {
                renderer.complicationState = renderer.complicationState.copy(
                    weatherTempC = savedTemp,
                    weatherCondition = savedCond,
                    sunriseSunsetSummary = savedSunSummary ?: renderer.complicationState.sunriseSunsetSummary,
                    sunriseSunsetIsSunset = if (savedSunSummary != null) savedIsSunset else renderer.complicationState.sunriseSunsetIsSunset
                )
            }
        }

        // Load Cached Health (Steps, Calories, Distance, Active Minutes)
        val healthPrefs = applicationContext.getSharedPreferences("faces_health_prefs", android.content.Context.MODE_PRIVATE)
        val savedSteps = healthPrefs.getLong("today_steps", 0L)
        val savedCalories = healthPrefs.getInt("calories", (savedSteps * 0.04f).toInt())
        val savedDist = healthPrefs.getFloat("distance", savedSteps * 0.000762f)
        val savedMins = healthPrefs.getInt("active_mins", (savedSteps / 100).toInt())
        val savedBpm = healthPrefs.getInt("last_heart_rate", 0).takeIf { it > 0 }
        if (savedSteps > 0L || savedCalories > 0) {
            renderer.complicationState = renderer.complicationState.copy(
                stepCount = savedSteps,
                caloriesBurned = savedCalories,
                distanceKm = savedDist,
                activeMinutes = savedMins,
                heartRateBpm = savedBpm ?: renderer.complicationState.heartRateBpm
            )
        }

        // Live Health Sensors (Steps, Heart Rate, Estimated Calories)
        healthSensorManager = HealthSensorManager(applicationContext) { healthState ->
            val existingSunSummary = renderer.complicationState.sunriseSunsetSummary
            renderer.complicationState = renderer.complicationState.copy(
                stepCount = healthState.stepCount,
                heartRateBpm = healthState.heartRateBpm,
                caloriesBurned = healthState.caloriesBurned,
                distanceKm = healthState.distanceKm,
                activeMinutes = healthState.activeMinutes,
                sunriseSunsetSummary = existingSunSummary ?: healthState.sunriseSunsetSummary,
                sunriseSunsetIsSunset = if (existingSunSummary != null) renderer.complicationState.sunriseSunsetIsSunset else healthState.sunriseSunsetIsSunset
            )
            val s = healthState.stepCount ?: 0L
            syncHealthToPhone(
                applicationContext,
                s,
                healthState.heartRateBpm,
                healthState.caloriesBurned ?: (s * 0.04f).toInt(),
                healthState.distanceKm ?: (s * 0.000762f),
                healthState.activeMinutes ?: (s / 100).toInt()
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

        // Load Cached Calendar Event if previously received from companion phone
        val calPrefs = applicationContext.getSharedPreferences("faces_calendar_prefs", android.content.Context.MODE_PRIVATE)
        if (calPrefs.contains("title") && calPrefs.contains("minutes")) {
            val savedTitle = calPrefs.getString("title", null)
            val savedMins = calPrefs.getInt("minutes", 0)
            if (!savedTitle.isNullOrBlank()) {
                renderer.complicationState = renderer.complicationState.copy(
                    nextEventTitle = savedTitle,
                    nextEventMinutes = savedMins
                )
            }
        }

        // Register dynamic battery updates
        val bReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: android.content.Context?, intent: android.content.Intent?) {
                updateBattery()
                if (renderer.faceMode == FaceMode.BATTERY) {
                    renderer.invalidate()
                }
            }
        }
        batteryReceiver = bReceiver
        applicationContext.registerReceiver(bReceiver, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))

        // Listen to Wear OS system complication data sources (Steps & Heart Rate)
        serviceScope.launch {
            complicationSlotsManager[STEP_SLOT_ID]?.complicationData?.collect { data ->
                val steps = parseStepCount(data)
                if (steps != null) {
                    healthSensorManager?.updateExternalSteps(steps)
                    val calories = (steps * 0.04f).toInt()
                    val distance = steps * 0.000762f
                    val activeMins = (steps / 100).toInt()
                    renderer.complicationState = renderer.complicationState.copy(
                        stepCount = steps,
                        caloriesBurned = calories,
                        distanceKm = distance,
                        activeMinutes = activeMins
                    )
                    syncHealthToPhone(
                        applicationContext,
                        steps,
                        renderer.complicationState.heartRateBpm,
                        calories,
                        distance,
                        activeMins
                    )
                    if (renderer.faceMode in listOf(FaceMode.STEPS, FaceMode.CALORIES, FaceMode.DISTANCE, FaceMode.ACTIVE_MINUTES)) {
                        renderer.invalidate()
                    }
                }
            }
        }

        serviceScope.launch {
            complicationSlotsManager[HEART_RATE_SLOT_ID]?.complicationData?.collect { data ->
                val bpm = parseHeartRate(data)
                if (bpm != null && bpm > 0) {
                    healthSensorManager?.updateExternalHeartRate(bpm)
                    renderer.complicationState = renderer.complicationState.copy(
                        heartRateBpm = bpm
                    )
                    if (renderer.faceMode == FaceMode.HEART_RATE) {
                        renderer.invalidate()
                    }
                }
            }
        }

        // Check runtime sensor permissions
        watch.facesapp.wear.permission.WearPermissionActivity.checkAndRequest(applicationContext)

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
        serviceScope.cancel()
        batteryReceiver?.let {
            try {
                applicationContext.unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        batteryReceiver = null
        syncStatusToDataClient(applicationContext, false)
        complicationRevertRunnable?.let { tapHandler.removeCallbacks(it) }
        complicationRevertRunnable = null
        healthSensorManager?.stop()
        healthSensorManager = null
        activeHealthManager = null
        activeRenderer = null
    }

    private fun parseStepCount(data: androidx.wear.watchface.complications.data.ComplicationData): Long? {
        return when (data) {
            is androidx.wear.watchface.complications.data.ShortTextComplicationData -> {
                val text = data.text.getTextAt(applicationContext.resources, java.time.Instant.now()).toString()
                text.filter { it.isDigit() }.toLongOrNull()
            }
            is androidx.wear.watchface.complications.data.RangedValueComplicationData -> data.value.toLong()
            is androidx.wear.watchface.complications.data.GoalProgressComplicationData -> data.value.toLong()
            else -> null
        }
    }

    private fun parseHeartRate(data: androidx.wear.watchface.complications.data.ComplicationData): Int? {
        return when (data) {
            is androidx.wear.watchface.complications.data.ShortTextComplicationData -> {
                val text = data.text.getTextAt(applicationContext.resources, java.time.Instant.now()).toString()
                text.filter { it.isDigit() }.toIntOrNull()
            }
            is androidx.wear.watchface.complications.data.RangedValueComplicationData -> data.value.toInt()
            else -> null
        }
    }

    companion object {
        const val STEP_SLOT_ID = 101
        const val HEART_RATE_SLOT_ID = 102
        var activeRenderer: FacesCanvasRenderer? = null
        var activeHealthManager: HealthSensorManager? = null
        var isPreviewActive: Boolean = false
        val isFaceActive: Boolean
            get() = activeRenderer != null

        private var previewWakeLock: android.os.PowerManager.WakeLock? = null
        private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        private var autoRevertRunnable: Runnable? = null

        fun syncStatusToDataClient(context: android.content.Context, isActive: Boolean) {
            try {
                val putDataMapReq = com.google.android.gms.wearable.PutDataMapRequest.create(WearMessageListener.PATH_WATCH_STATUS).apply {
                    dataMap.putBoolean(WearMessageListener.KEY_IS_ACTIVE, isActive)
                    dataMap.putLong(WearMessageListener.KEY_TIMESTAMP, System.currentTimeMillis())
                }
                val req = putDataMapReq.asPutDataRequest().setUrgent()
                com.google.android.gms.wearable.Wearable.getDataClient(context).putDataItem(req)
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to sync status to DataClient", e)
            }
        }

        fun onPermissionsGranted(context: android.content.Context) {
            activeHealthManager?.restart()
        }

        fun refreshPreviewHeartbeat(context: android.content.Context) {
            if (isPreviewActive) {
                scheduleAutoRevert(context)
            }
        }

        fun scheduleAutoRevert(context: android.content.Context) {
            autoRevertRunnable?.let { mainHandler.removeCallbacks(it) }
            val runnable = Runnable {
                releasePreviewWakeLock(context)
            }
            autoRevertRunnable = runnable
            mainHandler.postDelayed(runnable, 6000L) // Revert after 6s of inactivity/disconnect
        }

        @Synchronized
        fun wakeUpForPreview(context: android.content.Context, timeoutMs: Long = 10_000L) {
            try {
                isPreviewActive = true
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

                // Schedule auto-revert if no new preview updates or heartbeats arrive
                scheduleAutoRevert(context)
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to acquire preview wake lock", e)
            }
        }

        @Synchronized
        fun releasePreviewWakeLock(context: android.content.Context? = null) {
            try {
                autoRevertRunnable?.let { mainHandler.removeCallbacks(it) }
                autoRevertRunnable = null
                isPreviewActive = false
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
                isPreviewActive = false
                val savedConfig = WearMessageListener.loadSavedConfig(context)
                updateConfig(savedConfig)
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Failed to revert to saved config", e)
            }
        }

        fun updateWeather(
            tempC: Int,
            condition: String,
            sunriseSunsetSummary: String? = null,
            isSunset: Boolean = true
        ) {
            activeRenderer?.let { renderer ->
                renderer.complicationState = renderer.complicationState.copy(
                    weatherTempC = tempC,
                    weatherCondition = condition,
                    sunriseSunsetSummary = sunriseSunsetSummary ?: renderer.complicationState.sunriseSunsetSummary,
                    sunriseSunsetIsSunset = if (sunriseSunsetSummary != null) isSunset else renderer.complicationState.sunriseSunsetIsSunset
                )
                if (renderer.faceMode in listOf(FaceMode.WEATHER, FaceMode.SUNRISE_SUNSET)) {
                    renderer.invalidate()
                }
            }
        }

        fun updateHealthFromPhone(steps: Long, bpm: Int?, calories: Int, distance: Float, activeMins: Int) {
            activeRenderer?.let { renderer ->
                val currentSteps = renderer.complicationState.stepCount ?: 0L
                val bestSteps = maxOf(steps, currentSteps)
                val bestCalories = if (steps >= currentSteps) calories else (renderer.complicationState.caloriesBurned ?: calories)
                val bestDistance = if (steps >= currentSteps) distance else (renderer.complicationState.distanceKm ?: distance)
                val bestActiveMins = if (steps >= currentSteps) activeMins else (renderer.complicationState.activeMinutes ?: activeMins)
                val bestBpm = bpm ?: renderer.complicationState.heartRateBpm
                renderer.complicationState = renderer.complicationState.copy(
                    stepCount = bestSteps,
                    caloriesBurned = bestCalories,
                    distanceKm = bestDistance,
                    activeMinutes = bestActiveMins,
                    heartRateBpm = bestBpm
                )
                if (renderer.faceMode in listOf(
                        FaceMode.STEPS,
                        FaceMode.CALORIES,
                        FaceMode.DISTANCE,
                        FaceMode.ACTIVE_MINUTES,
                        FaceMode.HEART_RATE
                    )
                ) {
                    renderer.invalidate()
                }
            }
            activeHealthManager?.updateExternalSteps(steps)
        }

        fun syncHealthToPhone(
            context: android.content.Context,
            steps: Long,
            bpm: Int?,
            calories: Int,
            distance: Float,
            activeMins: Int
        ) {
            try {
                val payload = "$steps|${bpm ?: 0}|$calories|$distance|$activeMins"
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
                val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
                nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, WearMessageListener.PATH_HEALTH, bytes)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FacesWatchFaceService", "Error syncing health to phone", e)
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
