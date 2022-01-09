package com.kmeoung.getnetwork.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
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
import com.kmeoung.getnetwork.bean.BeanData
import com.kmeoung.getnetwork.bean.BeanWifiData
import com.kmeoung.getnetwork.bean.CellularData
import com.kmeoung.getnetwork.bean.DATA_TYPE
import com.kmeoung.getnetwork.databinding.MainActivityBinding
import com.kmeoung.utils.CellularManager
import com.kmeoung.utils.IOWifiListener
import com.kmeoung.utils.WifiManager
import com.kmeoung.utils.WriteTextManager

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
//                        getWifiInfo()
                        for (data in cellularInfo.getDbms()) {
                            mAdapter.add(
                                BeanData(
                                    "${data.type} ci:${data.cid} / ${if (data.type == "3G") "psc" else "pci"}:${data.pci}",
                                    data.dbm ?: -9999,
                                    DATA_TYPE.CELLULAR
                                )
                            )
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
                        override fun scanSuccess(results: List<ScanResult>) {
                            if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
                            Toast.makeText(
                                this@MainActivity,
                                "Wifi Scan Success",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.d(TAG, "Wifi Scan Success")
                            for (result in results) {

                                var wifiData = BeanWifiData(
                                    result.BSSID,
                                    result.SSID,
                                    result.frequency,
                                    result.channelWidth,
                                    result.level // rssi
                                )


                                mAdapter.add(
                                    BeanData(
                                        "[ssid] ${result.SSID}",
                                        result.level,
                                        DATA_TYPE.WIFI
                                    )
                                )
                            }

                            // TODO : 데이터 로그 저장
                            var logData = ""
                            for (data in mAdapter.getList()) {
                                val bean = data as BeanData
                                logData += when (data.dataType) {
                                    DATA_TYPE.WIFI ->
                                        "[WIFI]\n"
                                    DATA_TYPE.CELLULAR ->
                                        "[CELLULAR]\n"
                                }
                                logData += "${bean.name}\n"
                                logData += "dbm:${bean.strength}\n"

                                logData += "\n"
                            }
                            if (mAdapter.size() > 0) {
//                                WriteTextManager.setSaveText(this@MainActivity, logData)
                            }
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

        override fun onBindViewHolder(h: BaseViewHolder, i: Int) {
            val bean = mAdapter.get(i) as BeanData
            val llBg = h.getItemView<LinearLayout>(R.id.ll_bg)
            val tvTitle = h.getItemView<TextView>(R.id.tv_title)
            val tvStrength = h.getItemView<TextView>(R.id.tv_strength)
            when (getItemViewType(i)) {
                TYPE_CELLULAR -> {
                    llBg.setBackgroundColor(getColor(R.color.teal_200))
                }
                TYPE_WIFI -> {
                    llBg.setBackgroundColor(getColor(R.color.yellow))
                }
            }
            tvTitle.text = bean.name
            tvStrength.text = "${bean.strength}"
        }

        override fun getItemViewType(i: Int): Int {
            return if ((mAdapter.get(i) as BeanData).dataType == DATA_TYPE.CELLULAR) TYPE_CELLULAR
            else TYPE_WIFI
        }
    }
}