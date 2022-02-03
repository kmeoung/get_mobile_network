package com.kmeoung.getnetwork.bean

import com.google.gson.annotations.SerializedName

data class BeanMobileNetwork(
    @SerializedName("NetworkType") var currentNetworkType: String = "LTE",
    @SerializedName("CELL_ID") var CELL_ID: Int = -999,
    @SerializedName("NB_ID_TYPE") var NB_ID_TYPE: String = "N",
    @SerializedName("ARFCN") var ARFCN: Int = -999,
    @SerializedName("PCI") var PCI: Int = -999,
    @SerializedName("RSRP") var RSRP: Int = -999,
    @SerializedName("RSRQ") var RSRQ: Int = -999,
    @SerializedName("SINR") var SINR: Int = -999,
    @SerializedName("CQI") var CQI: Int = -999,
    @SerializedName("MCS") var MCS: Int = -999,
    @SerializedName("data_idx") var data_idx: Int = 0,
    @SerializedName("meas_idx") var meas_idx: Int = 0,
    @SerializedName("meas_time") var meas_time: String = "-999",
)