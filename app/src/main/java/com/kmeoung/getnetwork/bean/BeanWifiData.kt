package com.kmeoung.getnetwork.bean

/**
 * BSSID
 * SSID
 * frequency
 * channelWidth -> BandWidth : Int
 * (ScanResult.CHANNEL_WIDTH_160MHZ,ScanResult.CHANNEL_WIDTH_20MHZ,
 *  ScanResult.CHANNEL_WIDTH_40MHZ,ScanResult.CHANNEL_WIDTH_80MHZ,ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ)
 *
 *  info Link : https://developer.android.com/reference/android/net/wifi/ScanResult
 *
 */
data class BeanWifiData(
    var BSSID: String,
    var SSID: String,
    var frequency: Int,
    var channelWidth: Int,
    var rssi: Int,
    var standard: Int?,
    var bandWidth: Int?,
    var CINR: Int?,
    var MCS: Int?
)