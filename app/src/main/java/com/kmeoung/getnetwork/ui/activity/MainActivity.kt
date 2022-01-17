package com.kmeoung.getnetwork.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.kmeoung.getnetwork.R
import com.kmeoung.getnetwork.base.BaseActivity
import com.kmeoung.getnetwork.base.BaseRecyclerViewAdapter
import com.kmeoung.getnetwork.base.BaseViewHolder
import com.kmeoung.getnetwork.base.IORecyclerViewListener
import com.kmeoung.getnetwork.bean.*
import com.kmeoung.getnetwork.databinding.MainActivityBinding
import com.kmeoung.utils.CellularManager
import com.kmeoung.utils.IOWifiListener
import com.kmeoung.utils.WifiManager
import com.kmeoung.utils.WriteTextManager
import java.text.SimpleDateFormat
import java.util.*
import android.telephony.TelephonyManager

import android.telephony.SubscriptionInfo

import android.telephony.SubscriptionManager

import android.R.attr.name


class MainActivity : BaseActivity() {

    companion object {
        private const val REQUEST_PERMISSION_GRANT = 3000
        private const val TAG = "MAIN_ACTIVITY_TAG"

        private const val TYPE_CELLULAR = 0
        private const val TYPE_WIFI = 1

        private const val REQUEST_PERMISSIONS = "권한을 허용해주셔야 정상적으로 앱 이용이 가능합니다."
        private const val CONFIRM = "확인"
        private const val CAN_NOT_CHECK_NETWORK = "상용망 정보를 확인할 수 없습니다."
        private const val ACTIVE_MOBILE_NETWORK = "모바일 네트워크를 활성화한 후 다시 시도해주세요."
        private const val ACTIVE_LOCATION_INFORMATION = "위치 정보 설정을 활성화한 후 다시 시도해주세요."
        private const val PLEASE_WAIT_2MINUTE = "2분뒤에 다시 시도해주세요"
    }

    private lateinit var binding: MainActivityBinding
    private var _recyclerAdapter: BaseRecyclerViewAdapter? = null
    private val mAdapter get() = _recyclerAdapter!!
    private var _wifiManager: WifiManager? = null
    private val mWifiManager get() = _wifiManager!!
    var cal: Calendar? = null
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var startDate: String = ""
    var endDate: String = ""


    // TODO : temp
    private var _dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)

        _dialog = ProgressDialog(this@MainActivity)

        setContentView(binding.root)

        initAdapter()

        WriteTextManager.requestPermissions(this@MainActivity, REQUEST_PERMISSION_GRANT)

        // 모바일 네트워크 먼저 로딩
        binding.btn.setOnClickListener { _ ->
            if (_dialog != null) _dialog!!.show()
            cal = Calendar.getInstance()
            startDate = sdf.format(cal!!.time)
//            getWifiInfo()
            getCellularInfo()
        }
    }

    /**
     * RecyclerView Adapter 초기
     */
    private fun initAdapter() {
        _recyclerAdapter = BaseRecyclerViewAdapter(rvListener)
        binding.rvList.adapter = mAdapter
        binding.rvList.layoutManager = LinearLayoutManager(this@MainActivity)
    }

    @SuppressLint("MissingPermission")
    private fun getSimOperator(): String {
        //for dual sim mobile
        val localSubscriptionManager = SubscriptionManager.from(this)
        return if (localSubscriptionManager.activeSubscriptionInfoCount > 1) {
            //if there are two sims in dual sim mobile
            val localList: List<*> = localSubscriptionManager.activeSubscriptionInfoList
            val simInfo = localList[0] as SubscriptionInfo
            val simInfo1 = localList[1] as SubscriptionInfo
            simInfo.displayName.toString()
            //                val sim2 = simInfo1.displayName.toString()
        } else {
            //if there is 1 sim in dual sim mobile
            val tManager = baseContext
                .getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            tManager.networkOperatorName
        }
    }

    /**
     * Get Cellular Information
     */
    @SuppressLint("MissingPermission")
    private fun getCellularInfo() {
        var cellularInfo = CellularManager(this@MainActivity)
        if (cellularInfo.checkPermissions()) {
            if (cellularInfo.checkGps()) {
                if (cellularInfo.checkInternet()) {
                    mAdapter.clear()
                    try {
                        getWifiInfo()
                        for (data in cellularInfo.getData()) {
                            if (data != null) mAdapter.add(data)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@MainActivity, CAN_NOT_CHECK_NETWORK, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        ACTIVE_MOBILE_NETWORK,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                Toast.makeText(this@MainActivity, ACTIVE_LOCATION_INFORMATION, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            cellularInfo.requestPermissions(
                this@MainActivity, REQUEST_PERMISSION_GRANT
            )
        }
    }

    /**
     * Get WIFI Information
     */
    private fun getWifiInfo() {
//        WIFI 제한사항
//        안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
//        Android 8 / 8.1
//        백그라운드 앱은 30분 간격으로 1회 스캔 가능
//        Android 9이상
//        각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
//        백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
//        ContextCompat.checkSelfPermission(context,PERMISSION)
        // TODO : 현재 연결된 데이터 확인
        val connectivityManager =
            getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(currentNetwork)
        val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(currentNetwork)
        Log.d(TAG, linkProperties.toString())
        Log.d(TAG, caps.toString())

        _wifiManager = WifiManager(this@MainActivity)

        if (mWifiManager.checkPermissions()) {
            if (mWifiManager.checkGps()) {
                if (_dialog != null) _dialog!!.show()
                try {
                    mWifiManager.scanStart(object : IOWifiListener {
                        override fun scanSuccess(wifiData: BeanWifiData) {
                            if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
                            Log.d(TAG, "Wifi Scan Success")
                            mAdapter.add(wifiData)
                        }

                        override fun scanEnded(results: List<ScanResult>) {
                            saveLog()
                        }

                        override fun scanFailure(results: List<ScanResult>?) {
                            if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
                            Toast.makeText(
                                this@MainActivity,
                                PLEASE_WAIT_2MINUTE,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.d(TAG, "Wifi Scan Failed")
                        }
                    })
                } catch (e: Exception) {
                    if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
                    e.printStackTrace()
                }
            } else {
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
                Toast.makeText(this@MainActivity, ACTIVE_LOCATION_INFORMATION, Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            mWifiManager.requestPermissions(
                this@MainActivity,
                REQUEST_PERMISSION_GRANT
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
        mWifiManager.dispose()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_GRANT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허가
                // 해당 권한을 사용해서 작업을 진행할 수 있습니다
            } else run {
                // 권한 거부
                // 사용자가 해당권한을 거부했을때 해주어야 할 동작을 수행합니다
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(REQUEST_PERMISSIONS)
                builder.setPositiveButton(
                    CONFIRM
                ) { dialog, _ -> dialog.dismiss() }
                builder.show()
            }

        }
    }

    private val rvListener = object : IORecyclerViewListener {
        override val itemCount: Int
            get() = mAdapter.size()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return BaseViewHolder.newInstance(R.layout.listitem_temp, parent, false)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(h: BaseViewHolder, i: Int) {

            val llBg = h.getItemView<LinearLayout>(R.id.ll_bg)
            val tvTitle = h.getItemView<TextView>(R.id.tv_title)
            when (getItemViewType(i)) {
                TYPE_CELLULAR -> {
                    when (mAdapter.get(i)) {
                        is BeanLteData -> {
                            llBg.setBackgroundColor(getColor(R.color.teal_200))
                            val bean = mAdapter.get(i) as BeanLteData
                            var data = ""
                            data += "${bean.currentNetworkType}\n"
                            if (bean.eNBID_N != 0 && bean.eNBID_N != Int.MAX_VALUE) data += "eNB_ID(N) : ${bean.eNBID_N}\n"
                            if (bean.eNBID_P != 0 && bean.eNBID_P != Int.MAX_VALUE) data += "eNB_ID(P) : ${bean.eNBID_P}\n"
                            if (bean.earfcn != 0 && bean.earfcn != Int.MAX_VALUE) data += "EARFCN : ${bean.earfcn}\n"
                            if (bean.ci != 0L && bean.ci != Long.MAX_VALUE) data += "CI : ${bean.ci}\n"
                            if (bean.pci != 0 && bean.pci != Int.MAX_VALUE) data += "PCI : ${bean.pci}\n"
                            if (bean.rsrp != 0 && bean.rsrp != Int.MAX_VALUE) data += "RSRP : ${bean.rsrp}\n"
                            if (bean.rsrq != 0 && bean.rsrq != Int.MAX_VALUE) data += "RSRQ : ${bean.rsrq}\n"
                            if (bean.sinr != 0 && bean.sinr != Int.MAX_VALUE) data += "SINR : ${bean.sinr}\n"
                            if (bean.cqi != 0 && bean.cqi != Int.MAX_VALUE) data += "CQI : ${bean.cqi}\n"
                            if (bean.mcs != null && bean.mcs != 0 && bean.mcs != Int.MAX_VALUE) data += "MCS : ${bean.mcs}\n"
                            tvTitle.text = data
                        }
                        is Bean5GData -> {
                            llBg.setBackgroundColor(getColor(R.color.teal_700))
                            val bean = mAdapter.get(i) as Bean5GData
                            var data = ""
                            data += "${bean.currentNetworkType}\n"
                            if (bean.gNBID_N != 0 && bean.gNBID_N != Int.MAX_VALUE) data += "gNB_ID(N) : ${bean.gNBID_N}\n"
                            if (bean.gNBID_P != 0 && bean.gNBID_P != Int.MAX_VALUE) data += "gNB_ID(P) : ${bean.gNBID_P}\n"
                            if (bean.nr_earfcn != 0 && bean.nr_earfcn != Int.MAX_VALUE) data += "NR-ARFCN : ${bean.nr_earfcn}\n"
                            if (bean.nci != 0L && bean.nci != Long.MAX_VALUE) data += "NCI : ${bean.nci}\n"
                            if (bean.pci != 0 && bean.pci != Int.MAX_VALUE) data += "PCI : ${bean.pci}\n"
                            if (bean.ss_rsrp != 0 && bean.ss_rsrp != Int.MAX_VALUE) data += "SS-RSRP : ${bean.ss_rsrp}\n"
                            if (bean.ss_rsrq != 0 && bean.ss_rsrq != Int.MAX_VALUE) data += "SS-RSRQ : ${bean.ss_rsrq}\n"
                            if (bean.ss_sinr != 0 && bean.ss_sinr != Int.MAX_VALUE) data += "SS-SINR : ${bean.ss_sinr}\n"
                            if (bean.cqi != 0 && bean.cqi != Int.MAX_VALUE) data += "CQI : ${bean.cqi}\n"
                            if (bean.mcs != null && bean.mcs != 0 && bean.mcs != Int.MAX_VALUE) data += "MCS : ${bean.mcs}\n"
                            tvTitle.text = data
                        }
                    }


                }
                TYPE_WIFI -> {
                    val bean = mAdapter.get(i) as BeanWifiData
                    llBg.setBackgroundColor(getColor(R.color.yellow))
                    var data = ""
                    data += "WIFI\n"
                    if (bean.BSSID.isNotEmpty()) data += "BSSID : ${bean.BSSID}\n"
                    if (bean.SSID.isNotEmpty()) data += "SSID : ${bean.SSID}\n"
                    if (bean.frequency != 0 && bean.frequency != Int.MAX_VALUE) data += "Frequency : ${bean.frequency}\n"
                    if (bean.bandWidth != null && bean.bandWidth != 0 && bean.bandWidth != Int.MAX_VALUE) data += "BandWidth : ${bean.bandWidth}\n"
                    if (bean.channelWidth.isNotEmpty()) data += "Channel : ${bean.channelWidth}\n"
                    if (bean.rssi != null && bean.rssi != 0 && bean.rssi != Int.MAX_VALUE) data += "RSSI : ${bean.rssi}\n"
                    if (bean.CINR != null && bean.CINR != 0 && bean.CINR != Int.MAX_VALUE) data += "CINR : ${bean.CINR}\n"
                    if (bean.MCS != null && bean.MCS != 0 && bean.MCS != Int.MAX_VALUE) data += "MCS : ${bean.MCS}\n"
                    if (bean.standard != null && bean.standard != 0 && bean.standard != Int.MAX_VALUE) data += "Standard : ${bean.standard}\n"
                    tvTitle.text = data
                }
            }

        }

        override fun getItemViewType(i: Int): Int {
            return if (mAdapter.get(i) is BeanWifiData) TYPE_WIFI
            else TYPE_CELLULAR
        }
    }

    @SuppressLint("MissingPermission")
    fun saveLog() {
        cal = Calendar.getInstance()
        endDate = sdf.format(cal!!.time)
        // TODO : 데이터 로그 저장

        val conInfo = mWifiManager.getConnectionInfo()


        var logData = ""
        logData += "Mac Address : ${conInfo.macAddress} \n"
        logData += "Sim Operator : ${getSimOperator()}\n"
        logData += "Start Time : $startDate \n"
        logData += "End Time : $endDate \n"
        for (data in mAdapter.getList()) {


            when (data) {
                is BeanLteData -> {
                    val bean = data as BeanLteData
//                    logData += """${bean.currentNetworkType}
//                                |eNB_ID(N) : ${bean.eNBID_N}
//                                |eNB_ID(P) : ${bean.eNBID_P}
//                                |EARFCN : ${bean.earfcn}
//                                |CI : ${bean.ci}
//                                |PCI : ${bean.pci}
//                                |RSRP : ${bean.rsrp}
//                                |RSRQ : ${bean.rsrq}
//                                |SINR : ${bean.sinr}
//                                |CQI : ${bean.cqi}
//                                |MCS : ${bean.mcs}
//                            """.trimMargin()

                    var data = ""
                    data += "${bean.currentNetworkType}\n"
                    if (bean.eNBID_N != 0 && bean.eNBID_N != Int.MAX_VALUE) data += "eNB_ID(N) : ${bean.eNBID_N}\n"
                    if (bean.eNBID_P != 0 && bean.eNBID_P != Int.MAX_VALUE) data += "eNB_ID(P) : ${bean.eNBID_P}\n"
                    if (bean.earfcn != 0 && bean.earfcn != Int.MAX_VALUE) data += "EARFCN : ${bean.earfcn}\n"
                    if (bean.ci != 0L && bean.ci != Long.MAX_VALUE) data += "CI : ${bean.ci}\n"
                    if (bean.pci != 0 && bean.pci != Int.MAX_VALUE) data += "PCI : ${bean.pci}\n"
                    if (bean.rsrp != 0 && bean.rsrp != Int.MAX_VALUE) data += "RSRP : ${bean.rsrp}\n"
                    if (bean.rsrq != 0 && bean.rsrq != Int.MAX_VALUE) data += "RSRQ : ${bean.rsrq}\n"
                    if (bean.sinr != 0 && bean.sinr != Int.MAX_VALUE) data += "SINR : ${bean.sinr}\n"
                    if (bean.cqi != 0 && bean.cqi != Int.MAX_VALUE) data += "CQI : ${bean.cqi}\n"
                    if (bean.mcs != null && bean.mcs != 0 && bean.mcs != Int.MAX_VALUE) data += "MCS : ${bean.mcs}\n"
                    logData += data
                }
                is Bean5GData -> {
                    val bean = data as Bean5GData
//                    logData += """${bean.currentNetworkType}
//                                |gNB_ID(N) : ${bean.gNBID_N}
//                                |gNB_ID(P) : ${bean.gNBID_P}
//                                |NR-ARFCN : ${bean.nr_earfcn}
//                                |NCI : ${bean.nci}
//                                |PCI : ${bean.pci}
//                                |SS-RSRP : ${bean.ss_rsrp}
//                                |SS-RSRQ : ${bean.ss_rsrq}
//                                |SS-SINR : ${bean.ss_sinr}
//                                |CQI : ${bean.cqi}
//                                |MCS : ${bean.mcs}
//                            """.trimMargin()

                    var data = ""
                    data += "${bean.currentNetworkType}\n"
                    if (bean.gNBID_N != 0 && bean.gNBID_N != Int.MAX_VALUE) data += "gNB_ID(N) : ${bean.gNBID_N}\n"
                    if (bean.gNBID_P != 0 && bean.gNBID_P != Int.MAX_VALUE) data += "gNB_ID(P) : ${bean.gNBID_P}\n"
                    if (bean.nr_earfcn != 0 && bean.nr_earfcn != Int.MAX_VALUE) data += "NR-ARFCN : ${bean.nr_earfcn}\n"
                    if (bean.nci != 0L && bean.nci != Long.MAX_VALUE) data += "NCI : ${bean.nci}\n"
                    if (bean.pci != 0 && bean.pci != Int.MAX_VALUE) data += "PCI : ${bean.pci}\n"
                    if (bean.ss_rsrp != 0 && bean.ss_rsrp != Int.MAX_VALUE) data += "SS-RSRP : ${bean.ss_rsrp}\n"
                    if (bean.ss_rsrq != 0 && bean.ss_rsrq != Int.MAX_VALUE) data += "SS-RSRQ : ${bean.ss_rsrq}\n"
                    if (bean.ss_sinr != 0 && bean.ss_sinr != Int.MAX_VALUE) data += "SS-SINR : ${bean.ss_sinr}\n"
                    if (bean.cqi != 0 && bean.cqi != Int.MAX_VALUE) data += "CQI : ${bean.cqi}\n"
                    if (bean.mcs != null && bean.mcs != 0 && bean.mcs != Int.MAX_VALUE) data += "MCS : ${bean.mcs}\n"
                    logData += data
                }
                is BeanWifiData -> {
                    val bean = data as BeanWifiData
//                    logData += """WIFI
//                        |BSSID : ${bean.BSSID}
//                        |SSID : ${bean.SSID}
//                        |Frequency : ${bean.frequency}
//                        |BandWidth : ${bean.bandWidth}
//                        |Channel : ${bean.channelWidth}
//                        |RSSI : ${bean.rssi}
//                        |CINR : ${bean.CINR}
//                        |MCS : ${bean.MCS}
//                        |Standard : ${bean.standard}
//                    """.trimMargin()

                    var data = ""
                    data += "WIFI\n"
                    if (bean.BSSID.isNotEmpty()) data += "BSSID : ${bean.BSSID}\n"
                    if (bean.SSID.isNotEmpty()) data += "SSID : ${bean.SSID}\n"
                    if (bean.frequency != 0 && bean.frequency != Int.MAX_VALUE) data += "Frequency : ${bean.frequency}\n"
                    if (bean.bandWidth != null && bean.bandWidth != 0 && bean.bandWidth != Int.MAX_VALUE) data += "BandWidth : ${bean.bandWidth}\n"
                    if (bean.channelWidth.isNotEmpty()) data += "Channel : ${bean.channelWidth}\n"
                    if (bean.rssi != null && bean.rssi != 0 && bean.rssi != Int.MAX_VALUE) data += "RSSI : ${bean.rssi}\n"
                    if (bean.CINR != null && bean.CINR != 0 && bean.CINR != Int.MAX_VALUE) data += "CINR : ${bean.CINR}\n"
                    if (bean.MCS != null && bean.MCS != 0 && bean.MCS != Int.MAX_VALUE) data += "MCS : ${bean.MCS}\n"
                    if (bean.standard != null && bean.standard != 0 && bean.standard != Int.MAX_VALUE) data += "Standard : ${bean.standard}\n"

                    logData += data
                }
            }
            logData += "\n----\n"
        }
        if (mAdapter.size() > 0) {
            WriteTextManager.setSaveText(this@MainActivity, logData)
        }
    }
}