package com.kmeoung.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kmeoung.getnetwork.bean.BeanWifiData
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

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
    var useNumber = 0

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
     * Frequency에 따라 채널이 변경되는것을 확인하였습니다.
     *          2.4 GHZ 시
    CHA LOWER   CENTER  UPPER
    NUM FREQ    FREQ    FREQ
    MHZ     MHZ     MHZ
    1 2401    2412    2423
    2 2406    2417    2428
    3 2411    2422    2433
    4 2416    2427    2438
    5 2421    2432    2443
    6 2426    2437    2448
    7 2431    2442    2453
    8 2436    2447    2458
    9 2441    2452    2463
    10 2446    2457    2468
    11 2451    2462    2473
    12 2456    2467    2478
    13 2461    2472    2483
    14 2473    2484    2495

    5GHz 시
    CHANNEL NUMBER	FREQUENCY MHZ	EUROPE
    (ETSI)	NORTH AMERICA
    (FCC)	JAPAN
    36	5180	Indoors	✔	✔
    40	5200	Indoors	✔	✔
    44	5220	Indoors	✔	✔
    48	5240	Indoors	✔	✔
    52	5260	Indoors / DFS / TPC	DFS	DFS / TPC
    56	5280	Indoors / DFS / TPC	DFS	DFS / TPC
    60	5300	Indoors / DFS / TPC	DFS	DFS / TPC
    64	5320	Indoors / DFS / TPC	DFS	DFS / TPC
    100	5500	DFS / TPC	DFS	DFS / TPC
    104	5520	DFS / TPC	DFS	DFS / TPC
    108	5540	DFS / TPC	DFS	DFS / TPC
    112	5560	DFS / TPC	DFS	DFS / TPC
    116	5580	DFS / TPC	DFS	DFS / TPC
    120	5600	DFS / TPC	No Access	DFS / TPC
    124	5620	DFS / TPC	No Access	DFS / TPC
    128	5640	DFS / TPC	No Access	DFS / TPC
    132	5660	DFS / TPC	DFS	DFS / TPC
    136	5680	DFS / TPC	DFS	DFS / TPC
    140	5700	DFS / TPC	DFS	DFS / TPC
    149	5745	SRD	✔	No Access
    153	5765	SRD	✔	No Access
    157	5785	SRD	✔	No Access
    161	5805	SRD	✔	No Access
    165	5825	SRD	✔	No Access
     */
    private fun getChannelinFrequency(frequency: Int): Int {

        return when (frequency) {
            2412 -> 1
            2417 -> 2
            2422 -> 3
            2427 -> 4
            2432 -> 5
            2437 -> 6
            2442 -> 7
            2447 -> 8
            2452 -> 9
            2457 -> 10
            2462 -> 11
            2467 -> 12
            2472 -> 13
            2484 -> 14
            5180 -> 36
            5200 -> 40
            5220 -> 44
            5240 -> 48
            5260 -> 52
            5280 -> 56
            5300 -> 60
            5320 -> 64
            5500 -> 100
            5520 -> 104
            5540 -> 108
            5560 -> 112
            5580 -> 116
            5600 -> 120
            5620 -> 124
            5640 -> 128
            5660 -> 132
            5680 -> 136
            5700 -> 140
            5745 -> 149
            5765 -> 153
            5785 -> 157
            5805 -> 161
            5825 -> 165
            else -> -999
        }
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
                    var scanCount = 0
                    val cal = Calendar.getInstance()
                    val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    for (result in results) {
                        var isConnected = false
                        var bssid: String
                        var ssid: String
                        var frequency: Int
                        var bandWidth: String
                        var rssi: Int
                        var standard = "$DEFAULT_DATA"


                        var wifiData: BeanWifiData

                        var channel = getChannelinFrequency(result.frequency)

                        if (result.BSSID == connectWifi.bssid) {
                            isConnected = true
                            for (count in 1 until scanCount) {
                                val newWifiManager =
                                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                val currentWifiInfo = newWifiManager.connectionInfo

                                bssid = currentWifiInfo.bssid
                                ssid = currentWifiInfo.ssid
                                frequency = currentWifiInfo.frequency
                                channel = getChannelinFrequency(frequency)
                                bandWidth = when (result.channelWidth) {
                                    ScanResult.CHANNEL_WIDTH_160MHZ -> "160MHz"
                                    ScanResult.CHANNEL_WIDTH_20MHZ -> "20MHz"
                                    ScanResult.CHANNEL_WIDTH_40MHZ -> "40MHz"
                                    ScanResult.CHANNEL_WIDTH_80MHZ -> "80MHz"
                                    ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> "80_PLUS_MHz"
                                    else -> "-999"
                                }
                                rssi = currentWifiInfo.rssi

                                // CINR = carrier to interference and noise ratio
                                // MCS = Modulation & Conding Scheme
                                // 30 이상에서 가져오기 가능
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                    standard = when (currentWifiInfo.wifiStandard) {
                                        ScanResult.WIFI_STANDARD_11AC -> "11AC"
                                        ScanResult.WIFI_STANDARD_11AD -> "11AD"
                                        ScanResult.WIFI_STANDARD_11AX -> "11AX"
                                        ScanResult.WIFI_STANDARD_11N -> "11N"
                                        ScanResult.WIFI_STANDARD_LEGACY -> "LEGACY"
                                        ScanResult.WIFI_STANDARD_UNKNOWN -> "UNKNOWN"
                                        else -> "-999"
                                    }

                                wifiData = BeanWifiData(
                                    isConnected = isConnected,
                                    BSSID = bssid,
                                    SSID = ssid,
                                    frequency = frequency,
                                    bandWidth = bandWidth,
                                    RSSI = rssi,
                                    standard = standard,
                                    meas_idx = count,
                                    channel = channel,
                                    meas_time = fmt.format(cal.time)
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
                                data_idx = scanCount,
                                channel = channel,
                                meas_time = fmt.format(cal.time)
                            )
                            scanCount++
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

    /**
     * 와이파이 스
    안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
    Android 8 / 8.1캔
    백그라운드 앱은 30분 간격으로 1회 스캔 가능
    Android 9이상
    각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
    백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
    현재 와이파이 여러개가 검색이 되는건지 된다면 어떤식으로 표시되는지 테스트 필요
     */
    fun scanStart(scanCount: Int, listener: IOWifiListener?) {

        useNumber++
        wifiListener = listener
        this.scanCount = scanCount
        try {
            val cal = Calendar.getInstance()
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            val newWifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val currentWifiInfo = newWifiManager.connectionInfo


            Log.d("TAEWOONGKWON", newWifiManager.scanResults.toString())
            val bssid = currentWifiInfo.bssid
            val ssid = currentWifiInfo.ssid
            val frequency = currentWifiInfo.frequency
            val bandWidth = "-999"
            val rssi = currentWifiInfo.rssi

            var standard = "$DEFAULT_DATA"
            // CINR = carrier to interference and noise ratio
            // MCS = Modulation & Conding Scheme
            var channel = getChannelinFrequency(frequency)
            // 30 이상에서 가져오기 가능
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                standard = when (currentWifiInfo.wifiStandard) {
                    ScanResult.WIFI_STANDARD_11AC -> "11AC"
                    ScanResult.WIFI_STANDARD_11AD -> "11AD"
                    ScanResult.WIFI_STANDARD_11AX -> "11AX"
                    ScanResult.WIFI_STANDARD_11N -> "11N"
                    ScanResult.WIFI_STANDARD_LEGACY -> "LEGACY"
                    ScanResult.WIFI_STANDARD_UNKNOWN -> "UNKNOWN"
                    else -> "-999"
                }

            val wifiData = BeanWifiData(
                isConnected = true,
                BSSID = bssid,
                SSID = ssid,
                frequency = frequency,
                bandWidth = bandWidth,
                RSSI = rssi,
                standard = standard,
                meas_idx = 0,
                channel = channel,
                meas_time = fmt.format(cal.time)
            )
            wifiListener?.scanSuccess(wifiData)
        } catch (e: Exception) {
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