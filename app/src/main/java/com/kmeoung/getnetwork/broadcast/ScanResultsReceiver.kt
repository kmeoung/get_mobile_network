package com.kmeoung.getnetwork.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast



class ScanResultsReceiver : BroadcastReceiver() {
    private val TAG = "WIFI_SCAN_RESULTS"

    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent != null) {
//            StringBuilder().apply {
//                append("Action: ${intent.action}\n")
//                append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
//                toString().also { log ->
//                    Log.d(TAG, log)
//                    Toast.makeText(context, log, Toast.LENGTH_LONG).show()
//                }
//            }
//        }

    }
}