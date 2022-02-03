package com.kmeoung.utils

import android.content.Context
import android.net.wifi.ScanResult
import com.kmeoung.getnetwork.bean.BeanWifiData

/**
 * WIFI 및 모바일 네트워크 정보 가져오기
 */
class MobileNetworkManager(private val context: Context) {

    private val buildTypeTest = 0
    private val buildTypeRelease = 1

    companion object {
        val REQUIRED_PERMISSION_CELLULAR = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_PHONE_STATE"
        )

        val REQUIRED_PERMISSION_WIFI = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE"
        )

        val REQUIRED_PERMISSION_STORAGE = arrayOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
        )

        val REQUIRED_PERMISSION = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
        )
    }

    /**
     * 테스트용 네트워크 정보 가져오기
     * 내부 스토리지 접근 권환 선 확인 필요
     *
     */
    fun getTestNetworkInfo(mobileNetworkListener: IOMobileNetworkListener) {
        val writeTextManager = WriteTextManager
        val wifiManager = WifiManager(context)
        val cellularManager = CellularManager(context)
        if (writeTextManager.checkPermissions(context) && cellularManager.checkPermissions() && wifiManager.checkPermissions()) {
            // 인터넷 및 GPS 권한 확인
            if (cellularManager.checkGps()) {
                if (cellularManager.checkInternet()) {
                    val networkList = ArrayList<Any>()
                    try {
                        // 모바일 네트워크 데이터 확인
                        for (data in cellularManager.getData(3)) {
                            networkList.add(data)
                        }
                        // WIFI 호출
                        getWifiInfo(
                            wifiManager,
                            networkList,
                            mobileNetworkListener,
                            buildType = buildTypeTest
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mobileNetworkListener.canNotCheckMobileNetwork()
                    }
                } else {
                    mobileNetworkListener.disableInternet()

                }
            } else {
                mobileNetworkListener.disableGps()

            }
        } else {
            mobileNetworkListener.denidedPermission()
        }
    }

    /**
     * 상용화용 네트워크 정보 가져오기
     */
    fun getReleaseNetworkInfo(mobileNetworkListener: IOMobileNetworkListener) {
        val wifiManager = WifiManager(context)
        val cellularManager = CellularManager(context)
        // 모바일 네트워크 및 와이파이 필수 권한 확인
        if (cellularManager.checkPermissions() && wifiManager.checkPermissions()) {
            // 인터넷 및 GPS 권한 확인
            if (cellularManager.checkGps()) {
                if (cellularManager.checkInternet()) {
                    val networkList = ArrayList<Any>()
                    try {
                        // 모바일 네트워크 데이터 확인
                        for (data in cellularManager.getData(3)) {
                            networkList.add(data)
                        }
                        // WIFI 호출
                        getWifiInfo(
                            wifiManager,
                            networkList,
                            mobileNetworkListener,
                            buildType = buildTypeRelease
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mobileNetworkListener.disableInternet()
                    }

                } else {
                    mobileNetworkListener.disableInternet()

                }
            } else {
                mobileNetworkListener.disableGps()

            }
        } else {
            mobileNetworkListener.denidedPermission()
        }
    }

    /**
     * WIFI 정보 가져오기
    WIFI 제한사항
    안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
    Android 8 / 8.1
    백그라운드 앱은 30분 간격으로 1회 스캔 가능
    Android 9이상
    각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
    백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
     */
    private fun getWifiInfo(
        wifiManager: WifiManager,
        networkList: ArrayList<Any>,
        mobileNetworkListener: IOMobileNetworkListener,
        buildType: Int
    ) {
        try {
            wifiManager.scanStart(3, object : IOWifiListener {
                override fun scanSuccess(wifiData: BeanWifiData) {
                    networkList.add(wifiData)
                }

                override fun scanFailure(results: List<ScanResult>?) {
                    mobileNetworkListener.wifiSearchCountOver()
                }

                override fun scanEnded() {
                    val dataJson = Utils.dataFormatToJson(networkList, context)
                    when (buildType) {
                        buildTypeTest -> {
                            mobileNetworkListener.successFindInfo(dataJson.toString(), networkList)
                            WriteTextManager.setSaveText(context, dataJson.toString())
                        }
                        buildTypeRelease -> mobileNetworkListener.successFindInfo(dataJson.toString())
                    }
                    // WIFI Receiver 사용 후 등록해제
                    wifiManager.dispose()
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
            if (buildType == buildTypeTest) {
                val dataJson = Utils.dataFormatToJson(networkList, context)
                WriteTextManager.setSaveText(context, dataJson.toString())
            }
        }
    }

}