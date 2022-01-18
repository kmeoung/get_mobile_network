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
    @SerializedName("Scan_No") var scan_no: Int = 0
)