package com.kmeoung.getnetwork.bean




//- [ ] •5G (P (Primary), 수집된 모든 N (Neighbor) 구성, 측정 시간 동안 하나의 Cell ID 에 다수 데이터)
//- [ ] 5G >
//- [ ] Cell_ID
//- [ ] (gNB_ID (P) /
//- [ ] gNB_ID (N)) >
//- [ ] NR-ARFCN,
//- [ ] PCI,
//- [ ] (SS-)RSRP,
//- [ ] (SS-)RSRQ,
//- [ ] (SS-)SINR,
//- [ ] CQI,
//- [ ] MCS


/**
 * Ci = CELL ID
 */
data class Bean5GData(
    var currentNetworkType : String,
    var gNBID_N : Int,
    var gNBID_P : Int,
    var dbm:Int,
    var nci: Long,
    var nr_earfcn: Int,
    var pci: Int,
    var ss_rsrp: Int,
    var ss_rsrq: Int,
    var ss_sinr: Int,
    var cqi: Int,
    var mcs: Int?,
)