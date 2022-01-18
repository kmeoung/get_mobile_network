package com.kmeoung.getnetwork.bean

import com.google.gson.annotations.SerializedName

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
    @SerializedName("isConnected") var isConnected : Boolean,
    @SerializedName("BSSID") var BSSID: String = "",
    @SerializedName("SSID") var SSID: String = "",
    @SerializedName("Frequency") var frequency: Int = -999,
    @SerializedName("Channel") var channel: Int = -999,
    @SerializedName("RSSI") var RSSI: Int = -999,
    @SerializedName("Standard") var standard: Int = -999,
    @SerializedName("BandWidth") var bandWidth: String = "",
    @SerializedName("CINR") var CINR: Int = -999,
    @SerializedName("MCS") var MCS: Int = -999,
    @SerializedName("Scan_No") var scan_no: Int = 0,
)