package com.kmeoung.getnetwork.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kmeoung.getnetwork.R
import com.kmeoung.getnetwork.databinding.FramgnetNetworkBinding

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

import com.kmeoung.getnetwork.base.*
import com.kmeoung.getnetwork.bean.BeanWifi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kmeoung.getnetwork.ui.activity.ActivityMain.Companion.NETWORKTYPE

import androidx.core.content.ContextCompat.getSystemService


class FragmentNetwork(private var networkType: NETWORKTYPE) : BaseFragment() {
    // TODO : ISSUE 현재 다른 Listener 를 달아놓고 NETWORKTYPE으로 전환하면 Listener가 컨텍이 안되서 앱이 터짐
    companion object {
        private const val REQUEST_PERMISSION_GRANT = 3000
        private const val TAG = "FRAGMENT_NETWORK"
    }

    // TODO : temp
    private var _dialog: ProgressDialog? = null

    private var _binding: FramgnetNetworkBinding? = null
    private val binding get() = _binding!!
    private var _recyclerAdapter: BaseRecyclerViewAdapter? = null
    private val mAdapter get() = _recyclerAdapter!!

    private var wifiManager: BaseWifiManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FramgnetNetworkBinding.inflate(inflater, container, false)
        val view = binding.root
        val rvList = binding.rvList

        rvList.layoutManager = LinearLayoutManager(context)
        _recyclerAdapter = BaseRecyclerViewAdapter(rvListener)
        rvList.adapter = mAdapter

        // TODO : temp
        _dialog = ProgressDialog(requireContext())

        binding.btnGetNetwork.setOnClickListener {

            when (networkType) {
                NETWORKTYPE.WIFI -> {
                    getWifiInfo()
                    if (_dialog != null) _dialog!!.show()
                }
                NETWORKTYPE.CELLULAR ->
                    getCellularInfo()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        initCellularSet()
        when (networkType) {
            NETWORKTYPE.CELLULAR ->
                initCellularSet()
            NETWORKTYPE.WIFI ->
                initWifiSet()

        }
    }

    /**
     * Wifi 세팅 초기
     */
    private fun initWifiSet() {
        wifiManager = BaseWifiManager(requireContext(), mWifiListener)
    }

    /**
     * Cellular 세팅 초기화
     */
    private fun initCellularSet() {

    }

    /**
     * Network 사용중인지 확
     */
    fun isOnline(): Boolean {
        val connMgr =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    /**
     * Wifi 정보들 가져오기
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

        var permissions: ArrayList<String> = ArrayList()


        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissions.add(Manifest.permission.CHANGE_WIFI_STATE)
        // todo : 위치 서비스 활성화 코드 추가 바람

        var denidedPermission = ArrayList<String>()

        for (permission in permissions) {
            val per = ContextCompat.checkSelfPermission(requireContext(), permission)
            if (per != PackageManager.PERMISSION_GRANTED) {
                denidedPermission.add(permission)
            }
        }
        var array = arrayOfNulls<String>(denidedPermission.size)
        array = denidedPermission.toArray(array)
        if (denidedPermission.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(), array,
                REQUEST_PERMISSION_GRANT
            )
        } else {
            if (wifiManager != null) wifiManager!!.scanStart()
        }
    }

    private fun getSavedWifiInfo() {
        var wifi =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        var results = wifi.scanResults
        mAdapter.clear()
        for (result in results) {

            mAdapter.add(
                BeanWifi(
                    "ssid) ${result.SSID}",
                    "level) ${result.level}",
                    "frequency) ${result.frequency}"
                )
            )
        }
        Toast.makeText(context, "${results[0].SSID} / ${results[0].level}", Toast.LENGTH_SHORT)
            .show()
    }

    private fun getCellularInfo() {

        val connectivityManager =
            requireContext().getSystemService(ConnectivityManager::class.java)
        val currentNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
        val linkProperties = connectivityManager.getLinkProperties(currentNetwork)
        Log.d(TAG, linkProperties.toString())
        Log.d(TAG, caps.toString())


        val telephonyManager =
            requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        var permissions: ArrayList<String> = ArrayList()
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        var denidedPermission = ArrayList<String>()
        for (permission in permissions) {
            val per = ContextCompat.checkSelfPermission(requireContext(), permission)
            if (per != PackageManager.PERMISSION_GRANTED) {
                denidedPermission.add(permission)
            }
        }
        var array = arrayOfNulls<String>(denidedPermission.size)
        array = denidedPermission.toArray(array)
        if (denidedPermission.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(), array,
                REQUEST_PERMISSION_GRANT
            )
        } else {
            Log.d(TAG, telephonyManager.toString())

            val cellInfo = telephonyManager!!.allCellInfo[0]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(TAG, cellInfo.cellSignalStrength.toString())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (cellInfo is CellInfoNr) { // 5G
                    Log.d(
                        TAG,
                        (telephonyManager!!.allCellInfo[0] as CellInfoLte).cellSignalStrength.toString()
                    )
                }
            } else {
                when (cellInfo) {
                    is CellInfoLte -> Log.d(
                        TAG,
                        (telephonyManager!!.allCellInfo[0] as CellInfoLte).cellSignalStrength.toString()
                    )
                    is CellInfoGsm -> Log.d(
                        TAG,
                        (telephonyManager!!.allCellInfo[0] as CellInfoGsm).cellSignalStrength.toString()
                    )
                    else -> Toast.makeText(
                        requireContext(),
                        "데이터 정보를 확인할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

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
                //                CustomToast.makeText(this, "권한을 허용해주셔야 테스트가 가능합니다.", CustomToast.LENGTH_SHORT).show();
                val builder = AlertDialog.Builder(context)
                builder.setMessage("권한을 허용해주셔야 정상적으로 앱 이용이 가능합니다.")
                builder.setPositiveButton(
                    "확인"
                ) { dialog, _ -> dialog.dismiss() }
                builder.show()
            }

        }
    }

    var mWifiListener: IOWifiListener? = object : IOWifiListener {
        override fun scanSuccess(results: List<ScanResult>) {
            if (_dialog != null) _dialog!!.dismiss()
            Toast.makeText(requireContext(), "Wifi Scan Success", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Wifi Scan Success")
            mAdapter.clear()
            for (result in results) {
                mAdapter.add(
                    BeanWifi(
                        "ssid) ${result.SSID}",
                        "level) ${result.level}",
                        "frequency) ${result.frequency}"
                    )
                )
            }
        }

        override fun scanFailure(results: List<ScanResult>?) {
            if (_dialog != null) _dialog!!.dismiss()
            Toast.makeText(requireContext(), "2분뒤에 다시 시도해주세", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Wifi Scan Failed")
        }
    }


    override fun onPause() {
        super.onPause()
        if (_dialog != null) _dialog!!.dismiss()
        _binding = null
        // TODO : 다른앱에서 확인하려면 null 제거
        mWifiListener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (_dialog != null) _dialog!!.dismiss()
        _binding = null
    }

    private val rvListener: IORecyclerViewListener = object : IORecyclerViewListener {
        override val itemCount: Int
            get() = mAdapter.size()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return BaseViewHolder.newInstance(R.layout.listitem_temp, parent, false)
        }

        override fun onBindViewHolder(h: BaseViewHolder, i: Int) {
            val bean = mAdapter.get(i) as BeanWifi

            val tvTitle = h.getItemView<TextView>(R.id.tv_title)
            val tvPower = h.getItemView<TextView>(R.id.tv_power)
            val tvStrength = h.getItemView<TextView>(R.id.tv_strength)

            tvTitle.text = bean.name
            tvPower.text = bean.power1
            tvStrength.text = bean.power2

        }

        override fun getItemViewType(i: Int): Int = 0
    }
}