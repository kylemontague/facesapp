package com.faces.mobile.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class ShakeDetector(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /**
     * Cold Flow emitting Unit whenever an accelerometer shake gesture is detected.
     */
    fun shakeEvents(
        shakeThresholdGravity: Float = 2.3f, // ~ 2.3 Gs
        shakeSlopTimeMs: Long = 500L
    ): Flow<Unit> = callbackFlow {
        if (sensorManager == null || accelerometer == null) {
            close()
            return@callbackFlow
        }

        var lastShakeTime = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val x = event.values[0] / SensorManager.GRAVITY_EARTH
                val y = event.values[1] / SensorManager.GRAVITY_EARTH
                val z = event.values[2] / SensorManager.GRAVITY_EARTH

                val gForce = sqrt(x * x + y * y + z * z)

                if (gForce > shakeThresholdGravity) {
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > shakeSlopTimeMs) {
                        lastShakeTime = now
                        trySend(Unit)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
