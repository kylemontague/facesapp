package watch.facesapp.mobile.sensor

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
 * Manages phone-side step counting, calorie estimation, and distance metrics
 * to keep mobile complication previews live and synced with user activity.
 */
class PhoneHealthManager(
    private val context: Context,
    private val onStateUpdated: (ComplicationState) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val todayStr = LocalDate.now().toString()
    private val savedDate = prefs.getString(KEY_SAVED_DATE, null)

    // Default to today's saved steps or sensible baseline if 0
    private var currentSteps: Long = if (savedDate == todayStr) {
        prefs.getLong(KEY_TODAY_STEPS, 1420L)
    } else {
        1420L
    }

    private var currentHeartRate: Int? = prefs.getInt(KEY_LAST_HEART_RATE, 0).takeIf { it > 0 }
    private var isListening = false

    fun start() {
        if (isListening) return
        isListening = true
        registerStepSensor()
        notifyUpdate()
    }

    fun stop() {
        if (!isListening) return
        isListening = false
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering phone sensor listener", e)
        }
    }

    fun updateFromWatch(steps: Long, bpm: Int?, calories: Int?, distance: Float?, activeMins: Int?) {
        if (steps > 0) {
            currentSteps = steps
            prefs.edit {
                putString(KEY_SAVED_DATE, todayStr)
                putLong(KEY_TODAY_STEPS, steps)
            }
        }
        if (bpm != null && bpm > 0) {
            currentHeartRate = bpm
            prefs.edit { putInt(KEY_LAST_HEART_RATE, bpm) }
        }
        notifyUpdate(caloriesOverride = calories, distanceOverride = distance, activeMinsOverride = activeMins)
    }

    private fun registerStepSensor() {
        val sm = sensorManager ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "ACTIVITY_RECOGNITION permission not yet granted; using cached step state")
                return
            }
        }

        try {
            val stepCounter = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepCounter != null) {
                sm.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d(TAG, "Registered phone TYPE_STEP_COUNTER")
            } else {
                val stepDetector = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                if (stepDetector != null) {
                    sm.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d(TAG, "Registered phone TYPE_STEP_DETECTOR fallback")
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException registering phone step sensor", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values.firstOrNull()?.toLong() ?: return
                calculateDailySteps(totalSteps)
                notifyUpdate()
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                currentSteps += 1
                prefs.edit {
                    putString(KEY_SAVED_DATE, todayStr)
                    putLong(KEY_TODAY_STEPS, currentSteps)
                }
                notifyUpdate()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun calculateDailySteps(totalBootSteps: Long) {
        val today = LocalDate.now().toString()
        val saved = prefs.getString(KEY_SAVED_DATE, null)
        var baseline = prefs.getLong(KEY_BOOT_BASELINE, -1L)

        if (saved != today || baseline <= 0L || baseline > totalBootSteps) {
            baseline = totalBootSteps
            prefs.edit {
                putString(KEY_SAVED_DATE, today)
                putLong(KEY_BOOT_BASELINE, baseline)
            }
        }

        val deltaSteps = (totalBootSteps - baseline).coerceAtLeast(0L)
        val savedSteps = prefs.getLong(KEY_TODAY_STEPS, 1420L)
        currentSteps = maxOf(savedSteps, deltaSteps).coerceAtLeast(1420L)
        prefs.edit { putLong(KEY_TODAY_STEPS, currentSteps) }
    }

    private fun notifyUpdate(
        caloriesOverride: Int? = null,
        distanceOverride: Float? = null,
        activeMinsOverride: Int? = null
    ) {
        val calories = caloriesOverride ?: (currentSteps * 0.04f).toInt()
        val distance = distanceOverride ?: (currentSteps * 0.000762f)
        val activeMins = activeMinsOverride ?: (currentSteps / 100).toInt().coerceAtLeast(0)

        val state = ComplicationState(
            stepCount = currentSteps,
            heartRateBpm = currentHeartRate,
            caloriesBurned = calories,
            distanceKm = distance,
            activeMinutes = activeMins
        )
        onStateUpdated(state)
    }

    companion object {
        private const val TAG = "PhoneHealthManager"
        private const val PREFS_NAME = "faces_phone_health_prefs"
        private const val KEY_SAVED_DATE = "saved_date"
        private const val KEY_BOOT_BASELINE = "boot_baseline"
        private const val KEY_TODAY_STEPS = "today_steps"
        private const val KEY_LAST_HEART_RATE = "last_heart_rate"
    }
}
