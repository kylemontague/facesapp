package watch.facesapp.wear.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import watch.facesapp.core.model.ComplicationState
import java.time.LocalDate

/**
 * Manages Wear OS hardware sensors for live health metrics (Steps, Heart Rate, Calories)
 * and safely updates the watch face complication state.
 */
class HealthSensorManager(
    private val context: Context,
    private val onStateUpdated: (ComplicationState) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val todayStr = LocalDate.now().toString()
    private val savedDate = prefs.getString(KEY_SAVED_DATE, null)

    private var currentSteps: Long = if (savedDate == todayStr) prefs.getLong(KEY_TODAY_STEPS, 0L) else 0L
    private var hasRealSteps: Boolean = true

    private var currentHeartRate: Int = prefs.getInt(KEY_LAST_HEART_RATE, 0)
    private var hasRealHeartRate: Boolean = currentHeartRate > 0

    private var isListening = false
    private var isSamplingHeartRate = false
    private var lastHeartRateSampleTimeMs = 0L

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var lastSavedSteps: Long = currentSteps
    private var lastDiskSaveTimeMs: Long = 0L

    private val heartRateTimeoutRunnable = Runnable {
        stopHeartRateSampling()
        scheduleNextHeartRateSample(HEART_RATE_INTERVAL_MS)
    }

    private val periodicHeartRateRunnable = Runnable {
        if (isListening) {
            startHeartRateSampling()
        }
    }

    fun updateExternalSteps(steps: Long) {
        currentSteps = steps
        hasRealSteps = true
        persistHealthStateToDisk(force = false)
        notifyUpdate()
    }

    fun updateExternalHeartRate(bpm: Int) {
        if (bpm > 0) {
            currentHeartRate = bpm
            hasRealHeartRate = true
            prefs.edit { putInt(KEY_LAST_HEART_RATE, bpm) }
            notifyUpdate()
        }
    }

    fun start() {
        if (isListening || sensorManager == null) return
        isListening = true

        // 1. Register step counter
        registerStepSensor()

        // 2. Schedule or trigger initial periodic heart rate sample
        val now = System.currentTimeMillis()
        if (now - lastHeartRateSampleTimeMs >= HEART_RATE_INTERVAL_MS) {
            startHeartRateSampling()
        } else {
            val remainingDelay = (HEART_RATE_INTERVAL_MS - (now - lastHeartRateSampleTimeMs)).coerceAtLeast(1000L)
            scheduleNextHeartRateSample(remainingDelay)
        }

        // Deliver initial state
        notifyUpdate()
    }

    fun restart() {
        stop()
        start()
    }

    fun stop() {
        if (!isListening && !isSamplingHeartRate) return
        isListening = false

        // Cancel periodic polling & timeouts
        handler.removeCallbacks(periodicHeartRateRunnable)
        handler.removeCallbacks(heartRateTimeoutRunnable)

        // Stop optical PPG sensor immediately
        stopHeartRateSampling()

        // Unregister all listeners
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering sensor listener", e)
        }

        // Flush any uncommitted steps or health stats to disk
        persistHealthStateToDisk(force = true)
    }

    /**
     * Triggered when the user explicitly navigates to the HEART_RATE complication,
     * ensuring a fresh reading without waiting for the next periodic cycle.
     */
    fun triggerImmediateHeartRateSample() {
        val now = System.currentTimeMillis()
        if (!isSamplingHeartRate && (now - lastHeartRateSampleTimeMs > 30_000L)) {
            handler.removeCallbacks(periodicHeartRateRunnable)
            startHeartRateSampling()
        }
    }

    private fun registerStepSensor() {
        val sm = sensorManager ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "ACTIVITY_RECOGNITION permission not yet granted; step listener deferred")
            return
        }

        try {
            val stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepCounter != null) {
                sm.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d(TAG, "Registered TYPE_STEP_COUNTER listener")
            } else {
                val stepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                if (stepDetector != null) {
                    sm.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d(TAG, "Registered TYPE_STEP_DETECTOR listener fallback")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException registering step sensor", e)
        }
    }

    private fun startHeartRateSampling() {
        val sm = sensorManager ?: return
        if (isSamplingHeartRate) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "BODY_SENSORS permission not yet granted; heart rate sampling deferred")
            scheduleNextHeartRateSample(HEART_RATE_INTERVAL_MS)
            return
        }

        try {
            val heartRateSensor = sm.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            if (heartRateSensor != null) {
                isSamplingHeartRate = true
                sm.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d(TAG, "Started periodic TYPE_HEART_RATE sample (PPG active)")

                // Automatically timeout sampling after 15s if no reading is obtained (e.g. watch off wrist)
                handler.removeCallbacks(heartRateTimeoutRunnable)
                handler.postDelayed(heartRateTimeoutRunnable, HEART_RATE_SAMPLE_TIMEOUT_MS)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException registering heart rate sensor", e)
            scheduleNextHeartRateSample(HEART_RATE_INTERVAL_MS)
        }
    }

    private fun stopHeartRateSampling() {
        if (!isSamplingHeartRate) return
        isSamplingHeartRate = false
        handler.removeCallbacks(heartRateTimeoutRunnable)
        val sm = sensorManager ?: return
        try {
            val heartRateSensor = sm.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            if (heartRateSensor != null) {
                sm.unregisterListener(this, heartRateSensor)
                Log.d(TAG, "Stopped TYPE_HEART_RATE sample (PPG powered off)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering heart rate sensor", e)
        }
    }

    private fun scheduleNextHeartRateSample(delayMs: Long) {
        handler.removeCallbacks(periodicHeartRateRunnable)
        handler.postDelayed(periodicHeartRateRunnable, delayMs)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values.firstOrNull()?.toLong() ?: return
                hasRealSteps = true
                calculateDailySteps(totalSteps)
                notifyUpdate()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                hasRealSteps = true
                currentSteps += 1
                persistHealthStateToDisk(force = false)
                notifyUpdate()
            }
            Sensor.TYPE_HEART_RATE -> {
                val bpm = event.values.firstOrNull()?.toInt() ?: return
                if (bpm > 0) {
                    hasRealHeartRate = true
                    currentHeartRate = bpm
                    lastHeartRateSampleTimeMs = System.currentTimeMillis()
                    prefs.edit { putInt(KEY_LAST_HEART_RATE, bpm) }
                    notifyUpdate()

                    // Successful sample achieved: immediately shut down PPG LED to save battery!
                    stopHeartRateSampling()
                    scheduleNextHeartRateSample(HEART_RATE_INTERVAL_MS)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun calculateDailySteps(totalBootSteps: Long) {
        val today = LocalDate.now().toString()
        val saved = prefs.getString(KEY_SAVED_DATE, null)
        var baseline = prefs.getLong(KEY_BOOT_BASELINE, -1L)

        if (saved != today || baseline <= 0L || baseline > totalBootSteps) {
            // New day or reboot reset: store new baseline
            baseline = totalBootSteps
            prefs.edit {
                putString(KEY_SAVED_DATE, today)
                    .putLong(KEY_BOOT_BASELINE, baseline)
            }
        }

        currentSteps = (totalBootSteps - baseline).coerceAtLeast(0L)
        persistHealthStateToDisk(force = false)
    }

    /**
     * Debounces writes to SharedPreferences to prevent constant flash I/O wakes.
     * Persists if steps changed by >= 50, 60s has elapsed, or forced on lifecycle stop.
     */
    private fun persistHealthStateToDisk(force: Boolean) {
        val now = System.currentTimeMillis()
        if (force || kotlin.math.abs(currentSteps - lastSavedSteps) >= 50 || (now - lastDiskSaveTimeMs >= 60_000L)) {
            lastDiskSaveTimeMs = now
            lastSavedSteps = currentSteps
            val calories = (currentSteps * 0.04f).toInt()
            val distance = currentSteps * 0.000762f
            val activeMins = (currentSteps / 100).toInt().coerceAtLeast(0)

            prefs.edit {
                putString(KEY_SAVED_DATE, todayStr)
                putLong(KEY_TODAY_STEPS, currentSteps)
                putInt("calories", calories)
                putFloat("distance", distance)
                putInt("active_mins", activeMins)
            }
        }
    }

    private fun notifyUpdate() {
        val calories = (currentSteps * 0.04f).toInt()
        val distance = currentSteps * 0.000762f
        val activeMins = (currentSteps / 100).toInt().coerceAtLeast(0)

        // Sunrise / Sunset calculation (based on current hour and day of year)
        val now = java.time.LocalTime.now()
        val currentHour = now.hour
        val isSunset = currentHour in 6..18
        val summary = if (isSunset) {
            val sunsetHour = (18 + (Math.sin(LocalDate.now().dayOfYear * Math.PI / 180) * 1.5)).toInt()
            String.format(java.util.Locale.US, "Sunset %02d:30", sunsetHour)
        } else {
            "Sunrise 06:45"
        }

        val newState = ComplicationState(
            stepCount = currentSteps,
            heartRateBpm = if (hasRealHeartRate) currentHeartRate else null,
            caloriesBurned = calories,
            distanceKm = distance,
            activeMinutes = activeMins,
            sunriseSunsetSummary = summary,
            sunriseSunsetIsSunset = isSunset
        )
        onStateUpdated(newState)
    }

    companion object {
        private const val TAG = "HealthSensorManager"
        private const val PREFS_NAME = "faces_health_prefs"
        private const val KEY_SAVED_DATE = "saved_date"
        private const val KEY_BOOT_BASELINE = "boot_baseline"
        private const val KEY_TODAY_STEPS = "today_steps"
        private const val KEY_LAST_HEART_RATE = "last_heart_rate"

        // Periodic sample every 3 minutes (180,000ms) with a 15s timeout
        private const val HEART_RATE_INTERVAL_MS = 180_000L
        private const val HEART_RATE_SAMPLE_TIMEOUT_MS = 15_000L
    }
}
