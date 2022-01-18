package com.kmeoung.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kmeoung.getnetwork.bean.BeanWifiData
import java.lang.Exception

/**
 * 현재 와이파이 체크
 */
class WifiManager(private var context: Context) {

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val locationManager = context.getSystemService(
        Context.LOCATION_SERVICE
    ) as LocationManager

    private val intentFilter = IntentFilter()
    private val wifiScanReceiver: BroadcastReceiver
    private var wifiListener: IOWifiListener? = null
    private var scanCount = 0

    companion object {
        val REQUIRED_PERMISSION = arrayOf(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE"
        )
        const val DEFAULT_DATA = -999
    }

    /**
     * 무선 네트워크 사용 여부 체크
     */
    fun checkInternet(): Boolean {
        // 시스템 > 설정 > 위치 및 보안 > 무선 네트워크 사용 여부 체크.
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    /**
     * Gps 사용 여부 체
     */
    fun checkGps(): Boolean {
        // 시스템 > 설정 > 위치 및 보안 > GPS 위성 사용 여부 체크.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    init {
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

        wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                // todo : connectionInfo 시 변
//                val request : NetworkRequest =
//                    NetworkRequest.Builder()
//                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                        .build()
//
//                val connectivityManager = context.getSystemService(
//                    ConnectivityManager::class.java
//                )
//
//                val networkCallback = object : ConnectivityManager.NetworkCallback() {
//                    override fun onAvailable(network: Network) {
//                        super.onAvailable(network)
//                    }
//
//                    override fun onCapabilitiesChanged(
//                        network: Network,
//                        networkCapabilities: NetworkCapabilities
//                    ) {
//                        super.onCapabilitiesChanged(network, networkCapabilities)
//                        val wifiInfo =  networkCapabilities.transportInfo
//                    }
//                }
//                    // etc.
//                };
//                connectivityManager.requestNetwork(request, networkCallback); // For request
//                connectivityManager.registerNetworkCallback(request, networkCallback); // For listen

                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    val results = wifiManager.scanResults
                    val connectWifi = wifiManager.connectionInfo

                    for (result in results) {
                        var isConnected = false
                        var bssid: String
                        var ssid: String
                        var frequency: Int
                        var bandWidth: String
                        var rssi: Int
                        var standard: Int = DEFAULT_DATA

                        var wifiData: BeanWifiData

                        if (result.BSSID == connectWifi.bssid) {
                            isConnected = true
                            for (count in 2..scanCount) {
                                val newWifiManager =
                                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                val currentWifiInfo = newWifiManager.connectionInfo

                                bssid = currentWifiInfo.bssid
                                ssid = currentWifiInfo.ssid
                                frequency = currentWifiInfo.frequency
                                bandWidth = when (result.channelWidth) {
                                    ScanResult.CHANNEL_WIDTH_160MHZ -> "160MHz"
                                    ScanResult.CHANNEL_WIDTH_20MHZ -> "20MHz"
                                    ScanResult.CHANNEL_WIDTH_40MHZ -> "40MHz"
                                    ScanResult.CHANNEL_WIDTH_80MHZ -> "80MHz"
                                    ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> "80_PLUS_MHz"
                                    else -> ""
                                }
                                rssi = currentWifiInfo.rssi

                                // CINR = carrier to interference and noise ratio
                                // MCS = Modulation & Conding Scheme
                                // 30 이상에서 가져오기 가능
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                    standard = currentWifiInfo.wifiStandard

                                wifiData = BeanWifiData(
                                    isConnected = isConnected,
                                    BSSID = bssid,
                                    SSID = ssid,
                                    frequency = frequency,
                                    bandWidth = bandWidth,
                                    RSSI = rssi,
                                    standard = standard,
                                    scan_no = count
                                )

                                wifiListener?.scanSuccess(wifiData)
                            }
                        } else {
                            bssid = result.BSSID
                            ssid = result.SSID
                            frequency = result.frequency
                            bandWidth = when (result.channelWidth) {
                                ScanResult.CHANNEL_WIDTH_160MHZ -> "160MHz"
                                ScanResult.CHANNEL_WIDTH_20MHZ -> "20MHz"
                                ScanResult.CHANNEL_WIDTH_40MHZ -> "40MHz"
                                ScanResult.CHANNEL_WIDTH_80MHZ -> "80MHz"
                                ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> "80_PLUS_MHz"
                                else -> ""
                            }
                            rssi = result.level

                            wifiData = BeanWifiData(
                                isConnected = isConnected,
                                BSSID = bssid,
                                SSID = ssid,
                                frequency = frequency,
                                bandWidth = bandWidth,
                                RSSI = rssi,
                                standard = standard,
                            )
                            if (wifiListener != null) wifiListener!!.scanSuccess(wifiData)
                        }
                    }
                } else {
                    val results = wifiManager.scanResults
                    if (wifiListener != null) wifiListener!!.scanFailure(results)
                }
                if (wifiListener != null) wifiListener!!.scanEnded()
            }
        }
        context.registerReceiver(wifiScanReceiver, intentFilter)
    }


    /**
     * 필수 권한 확인
     * checkPermissions
     * @return Boolean
     */
    fun checkPermissions(): Boolean {
        var isGranted = true
        for (permission in REQUIRED_PERMISSION) {
            val per = ContextCompat.checkSelfPermission(context, permission)
            if (per != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        return isGranted
    }

    /**
     * 필수 권한 요청
     * @param activity, requestCode
     */
    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, REQUIRED_PERMISSION,
            requestCode
        )
    }

    fun scanStart(scanCount: Int, listener: IOWifiListener?) {
        // 안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
        // Android 8 / 8.1
        // 백그라운드 앱은 30분 간격으로 1회 스캔 가능
        // Android 9이상
        // 각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
        // 백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
        // 현재 와이파이 여러개가 검색이 되는건지 된다면 어떤식으로 표시되는지 테스트 필
        wifiListener = listener
        this.scanCount = scanCount
        try {
            val newWifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val currentWifiInfo = newWifiManager.connectionInfo

            val bssid = currentWifiInfo.bssid
            val ssid = currentWifiInfo.ssid
            val frequency = currentWifiInfo.frequency
            val bandWidth = "-999"
            val rssi = currentWifiInfo.rssi
            var standard = DEFAULT_DATA
            // CINR = carrier to interference and noise ratio
            // MCS = Modulation & Conding Scheme
            // 30 이상에서 가져오기 가능
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                standard = currentWifiInfo.wifiStandard

            val wifiData = BeanWifiData(
                isConnected = true,
                BSSID = bssid,
                SSID = ssid,
                frequency = frequency,
                bandWidth = bandWidth,
                RSSI = rssi,
                standard = standard,
                scan_no = 1
            )
            wifiListener?.scanSuccess(wifiData)
        }catch (e: Exception){
            e.printStackTrace()
        }

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            listener?.scanFailure(null)

        }
    }

    /**
     * Wifi 검색기 연결 해제
     */
    fun dispose() {
        context.unregisterReceiver(wifiScanReceiver)
    }
}