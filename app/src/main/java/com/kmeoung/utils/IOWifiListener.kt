package com.kmeoung.utils

import android.net.wifi.ScanResult

interface IOWifiListener {
    fun scanSuccess(results : List<ScanResult>)

    fun scanFailure(results : List<ScanResult>?)
}