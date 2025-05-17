package com.example.hackaton

import android.content.Context

class StepStorage(val context: Context) {
    private val fileName = "steps_storage.txt"

    // Save both lastSensorValue and totalSteps
    fun saveSession(lastSensorValue: Long, totalSteps: Long) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write("$lastSensorValue\n$totalSteps")
        }
    }

    // Returns Pair<lastSensorValue, totalSteps>
    fun readSession(): Pair<Long, Long> {
        return try {
            context.openFileInput(fileName).bufferedReader().useLines { lines ->
                val list = lines.take(2).toList()
                val lastSensorValue = list.getOrNull(0)?.toLongOrNull() ?: 0L
                val totalSteps = list.getOrNull(1)?.toLongOrNull() ?: 0L
                lastSensorValue to totalSteps
            }
        } catch (e: Exception) {
            0L to 0L
        }
    }
}