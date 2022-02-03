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
    @SerializedName("BSSID") var BSSID: String = "-999",
    @SerializedName("SSID") var SSID: String = "-999",
    @SerializedName("Frequency") var frequency: Int = -999,
    @SerializedName("Channel") var channel: Int = -999,
    @SerializedName("RSSI") var RSSI: Int = -999,
    @SerializedName("Standard") var standard: String = "-999",
    @SerializedName("BandWidth") var bandWidth: String = "-999",
    @SerializedName("CINR") var CINR: Int = -999,
    @SerializedName("MCS") var MCS: Int = -999,
    @SerializedName("meas_idx") var meas_idx: Int = 0,
    @SerializedName("data_idx") var data_idx: Int = 0,
    @SerializedName("meas_time") var meas_time: String = "-999",

)