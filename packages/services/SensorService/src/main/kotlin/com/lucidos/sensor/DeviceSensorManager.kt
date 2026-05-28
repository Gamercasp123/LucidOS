package com.lucidos.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * Sensor manager for accelerometer, gyroscope, proximity, etc.
 */
class DeviceSensorManager(private val context: Context) : SensorEventListener {
    private val TAG = "DeviceSensorManager"

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // Sensor data
    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f
    private var proximity = 0f
    private var light = 0f

    fun startListening() {
        try {
            Log.d(TAG, "Starting sensor listening")

            // Accelerometer
            sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            // Proximity
            sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }

            // Light
            sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting sensor listening", e)
        }
    }

    fun stopListening() {
        try {
            Log.d(TAG, "Stopping sensor listening")
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping sensor listening", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelX = it.values[0]
                    accelY = it.values[1]
                    accelZ = it.values[2]
                }
                Sensor.TYPE_PROXIMITY -> proximity = it.values[0]
                Sensor.TYPE_LIGHT -> light = it.values[0]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes
    }

    fun getAccelerometerData(): Triple<Float, Float, Float> = Triple(accelX, accelY, accelZ)

    fun getProximity(): Float = proximity

    fun getLight(): Float = light

    fun isProximityNear(): Boolean = proximity < 5f

    fun logSensorData() {
        Log.d(TAG, """
            Sensor Data:
            Accelerometer: X=${"%.2f".format(accelX)}, Y=${"%.2f".format(accelY)}, Z=${"%.2f".format(accelZ)}
            Proximity: ${"%.2f".format(proximity)}
            Light: ${"%.2f".format(light)}
        """.trimIndent())
    }
}
