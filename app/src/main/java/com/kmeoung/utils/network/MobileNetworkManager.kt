package com.kmeoung.utils.network

import android.content.Context
import android.net.wifi.ScanResult
import android.os.Build
import android.provider.Settings
import com.kmeoung.getnetwork.bean.BeanMobileNetwork
import com.kmeoung.getnetwork.bean.BeanWifiData
import com.kmeoung.utils.Utils
import com.kmeoung.utils.WriteTextManager
import com.kmeoung.utils.network.listener.IOMobileNetworkListener
import com.kmeoung.utils.network.listener.IOWifiListener
import org.json.JSONArray
import org.json.JSONObject

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

        val REQUIRED_PERMISSION_STORAGE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )
        } else {
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"
            )
        }

        val REQUIRED_PERMISSION_TEST = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.CHANGE_WIFI_STATE",
                "android.permission.READ_PHONE_STATE",
            )
        } else {
            arrayOf(
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.CHANGE_WIFI_STATE",
                "android.permission.READ_PHONE_STATE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE"
            )
        }

        val REQUIRED_PERMISSION_RELEASE = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
        )
    }

    /**
     * 테스트용 네트워크 정보 가져오기
     * 내부 스토리지 접근 권환 선 확인 필요
     *
     */
    fun getTestNetworkInfo(mobileNetworkListener: IOMobileNetworkListener) {
        val wifiManager = WifiManager(context)
        val cellularManager = CellularManager(context)
        if (WriteTextManager.checkPermissions(context)) {
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
            }
        } else {
            mobileNetworkListener.denidedStoragePermission()
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

                    } catch (e: Exception) {
                        e.printStackTrace()
                        mobileNetworkListener.canNotCheckMobileNetwork()
                    }

                    // WIFI 호출
                    getWifiInfo(
                        wifiManager,
                        networkList,
                        mobileNetworkListener,
                        buildType = buildTypeRelease
                    )
                } else {
                    mobileNetworkListener.disableInternet()

                }
            } else {
                mobileNetworkListener.disableGps()

            }
        } else {
            mobileNetworkListener.denidedNeworkPermission()
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
                    val dataJson = dataFormatToJson(networkList, context)
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
                val dataJson = dataFormatToJson(networkList, context)
                WriteTextManager.setSaveText(context, dataJson.toString())
            }
        }
    }

    /**
     * 데이터 JSON형식으로 포맷
     */
    private fun dataFormatToJson(dataList: java.util.ArrayList<Any>, context: Context): JSONObject {
//        cal = Calendar.getInstance()
//        endDate = sdf.format(cal!!.time)
//        binding.tvDate.text = "Start Scan : $startDate\nEnd Scan : $endDate"

        val json = JSONObject()

        val jsonGenieverse = JSONObject()
        jsonGenieverse.put("home_id", "")
        jsonGenieverse.put("meas_mode", 0)
        jsonGenieverse.put("meas_proc", 0)
        jsonGenieverse.put("position_idx_A", JSONArray())

        json.put("Com_Genieverse", jsonGenieverse)

        val jsonMRFH = JSONObject()

        jsonMRFH.put("ue_mac", Utils.getMacAddress())
        jsonMRFH.put("sim_operator", Utils.getSimOperator(context))
        jsonMRFH.put("android_api", Build.VERSION.SDK_INT)

        json.put("Com_MRFH", jsonMRFH)

        val lteNetworks = JSONArray()
        val nrNetworks = JSONArray()
        val wifiConn = JSONArray()
        val wifiScan = JSONArray()
        for (data in dataList) {
            when (data) {
                is BeanMobileNetwork -> {
                    val networks = JSONObject()
                    networks.put("meas_idx", data.meas_idx)
                    networks.put("data_idx", data.data_idx)
                    networks.put("meas_time", data.meas_time)
                    networks.put("cell_id", data.CELL_ID)
                    networks.put("arfnc", data.ARFCN)
                    networks.put("pci", data.PCI)
                    networks.put("rsrp", data.RSRP)
                    networks.put("rsrq", data.RSRQ)
                    networks.put("cqi", data.CQI)
                    networks.put("mcs", data.MCS)

                    if (data.currentNetworkType == "LTE") {
                        lteNetworks.put(networks)
                    } else {
                        nrNetworks.put(networks)
                    }
                }
                is BeanWifiData -> {
                    val wifi = JSONObject()
                    wifi.put("meas_idx", data.meas_idx)
                    wifi.put("data_idx", data.data_idx)
                    wifi.put("meas_time", data.meas_time)
                    wifi.put("bssid", data.BSSID)
                    wifi.put("ssid", data.SSID)
                    wifi.put("freq", data.frequency)
                    wifi.put("bw", data.bandWidth)
                    wifi.put("ch", data.channel)
                    wifi.put("rssi", data.RSSI)
                    wifi.put("cinr", data.CINR)
                    wifi.put("mcs", data.MCS)
                    wifi.put("standard", data.standard)

                    if (data.isConnected) {
                        wifiConn.put(wifi)
                    } else {
                        wifiScan.put(wifi)
                    }

                }
            }
        }
        json.put("LTE", lteNetworks)
        json.put("5G", nrNetworks)
        json.put("WIFI_Conn", wifiConn)
        json.put("WIFI_Scan", wifiScan)
        return json
    }

}