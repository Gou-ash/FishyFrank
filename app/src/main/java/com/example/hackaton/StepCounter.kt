package com.example.hackaton

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounter(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var callback: ((Long) -> Unit)? = null
    private var listening = false

    fun startListening(onStepChanged: (Long) -> Unit) {
        if (listening) return // Already listening
        if (sensorManager == null) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        }
        callback = onStepChanged
        listening = true
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        if (sensorManager != null) {
            sensorManager?.unregisterListener(this, stepSensor)
        }
        listening = false
        callback = null
    }

    override fun onSensorChanged(event: android.hardware.SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        callback?.invoke(event.values[0].toLong())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}