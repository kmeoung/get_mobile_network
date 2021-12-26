package com.kmeoung.getnetwork.base

import android.content.Context
import android.util.Log
import java.io.IOException

import java.io.OutputStreamWriter




object BaseWriteText {

    private const val TAG = "KMEOUNG_LOG_TEST"

    fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput("kmeoung_test.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "File write failed: $e")
        }
    }
}