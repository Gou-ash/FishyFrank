package com.example.hackaton;

import android.content.Context;
import java.io.File;

class StepStorage(val context:Context) {

    private val fileName = "steps.txt"

    fun saveSteps(steps: Long) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
                output.write(steps.toString().toByteArray())
        }
    }

    fun readSteps(): Long {
        return try {
            context.openFileInput(fileName).bufferedReader().use { it.readText() }.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
