package com.kmeoung.utils.network.listener

interface IOMobileNetworkListener {
    // 저장소 권한이 없음
    fun denidedStoragePermission()
    // WIFI & Mobile Network 권한이 없음
    fun denidedNeworkPermission()
    // GPS 비활성화 상태
    fun disableGps()
    // Internet 비활성화 상태
    fun disableInternet()
    // WIFI 호출 횟수 초과함
    fun wifiSearchCountOver()
    // 정보 조회 성공 (dataList는 Test모드 시에만 데이터가 들어옴)
    fun successFindInfo(jsonString : String,dataList:ArrayList<Any>? = null)
    // 모바일 네트워크를 확인할 수 없음
    fun canNotCheckMobileNetwork()
}