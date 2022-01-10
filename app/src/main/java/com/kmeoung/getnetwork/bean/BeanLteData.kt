package com.kmeoung.getnetwork.bean


//- [ ] •LTE (P (Primary), 수집된 모든 N (Neighbor) 구성, 측정 시간 동안 하나의 Cell ID 에 다수 데이터)
//- [ ] LTE >
//- [ ] Cell_ID
//- [ ] (eNB_ID (P) /
//- [ ] eNB_ID (N)) >
//- [ ] EARFCN,
//- [ ] PCI,
//- [ ] RSRP,
//- [ ] RSRQ,
//- [ ] SINR,
//- [ ] CQI,
//- [ ] MCS

/**
 * Ci = CELL ID
 */
data class BeanLteData(
    var currentNetworkType : String,
    var eNBID_N : Int,
    var eNBID_P : Int,
    var dbm: Int,
    var ci: Long,
    var earfcn: Int,
    var pci: Int,
    var rsrp: Int,
    var rsrq: Int,
    var sinr: Int,
    var cqi: Int,
    var mcs: Int?,
)