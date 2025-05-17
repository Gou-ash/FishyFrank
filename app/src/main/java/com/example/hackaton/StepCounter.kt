package com.example.hackaton

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounter(val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var listener: SensorEventListener? = null

    fun startListening(onStepsChanged: (Long) -> Unit) {
        if (listener != null) return // Prevent multiple registrations
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val stepsSinceLastReboot = event.values[0].toLong()
                    onStepsChanged(stepsSinceLastReboot)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stopListening() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}