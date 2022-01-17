package com.kmeoung.utils

import android.net.wifi.ScanResult
import com.kmeoung.getnetwork.bean.BeanWifiData

interface IOWifiListener {
    fun scanSuccess(wifiData : BeanWifiData)

    fun scanFailure(results : List<ScanResult>?)

    fun scanEnded(results : List<ScanResult>)
}