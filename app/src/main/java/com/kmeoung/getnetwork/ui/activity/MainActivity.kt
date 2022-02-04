package com.kmeoung.getnetwork.ui.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kmeoung.getnetwork.R
import com.kmeoung.getnetwork.base.BaseActivity
import com.kmeoung.getnetwork.base.BaseRecyclerViewAdapter
import com.kmeoung.getnetwork.base.BaseViewHolder
import com.kmeoung.getnetwork.base.IORecyclerViewListener
import com.kmeoung.getnetwork.bean.*
import com.kmeoung.getnetwork.databinding.MainActivityBinding
import java.text.SimpleDateFormat
import java.util.*
import com.kmeoung.utils.*
import com.kmeoung.utils.network.listener.IOMobileNetworkListener
import com.kmeoung.utils.network.MobileNetworkManager
import kotlin.collections.ArrayList


class MainActivity : BaseActivity() {

    companion object {
        private const val REQUEST_PERMISSION_GRANT = 3000
        private const val REQUEST_STORAGE_PERMISSION_GRANT = 3001
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

    @SuppressLint("SimpleDateFormat")
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    var startDate: String = ""
    var endDate: String = ""


    // TODO : temp
    private var _dialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        _dialog = ProgressDialog(this@MainActivity)
        _dialog!!.setCancelable(false)

        setContentView(binding.root)

        initAdapter()

        binding.tvAddress.text =
            "Sim Operator : ${Utils.getSimOperator(baseContext)}\nAndroid API : ${Build.VERSION.SDK_INT}\nMAC Address : ${Utils.getMacAddress()}"
        binding.tvDate.text = "Start Scan : $startDate\nEnd Scan : $endDate"
        // 모바일 네트워크 먼저 로딩
        binding.btn.setOnClickListener { _ ->
            mAdapter.clear()
            binding.btn.isEnabled = false
            if (_dialog != null) _dialog!!.show()

            startDate = sdf.format(Calendar.getInstance().time)
            getNetworkInfo()

        }
    }

    val CREATE_FILE = 1

    private fun createFile(pickerInitialUri: Uri) {
        startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "invoice.json")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
        }, CREATE_FILE)
    }

    private fun getNetworkInfo() {
        val networkManager = MobileNetworkManager(this@MainActivity)
        networkManager.getTestNetworkInfo(object : IOMobileNetworkListener {
            override fun denidedStoragePermission() {
                WriteTextManager.requestPermissions(
                    this@MainActivity,
                    REQUEST_STORAGE_PERMISSION_GRANT
                )

                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }

            override fun denidedNeworkPermission() {
                ActivityCompat.requestPermissions(
                    this@MainActivity, MobileNetworkManager.REQUIRED_PERMISSION_TEST,
                    REQUEST_PERMISSION_GRANT
                )

                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }

            override fun disableGps() {
                Toast.makeText(this@MainActivity, ACTIVE_LOCATION_INFORMATION, Toast.LENGTH_SHORT)
                    .show()
                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }

            override fun disableInternet() {
                Toast.makeText(
                    this@MainActivity,
                    ACTIVE_MOBILE_NETWORK,
                    Toast.LENGTH_SHORT
                ).show()
                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }

            override fun canNotCheckMobileNetwork() {
                Toast.makeText(this@MainActivity, CAN_NOT_CHECK_NETWORK, Toast.LENGTH_SHORT)
                    .show()
            }

            override fun wifiSearchCountOver() {
                Toast.makeText(
                    this@MainActivity,
                    PLEASE_WAIT_2MINUTE,
                    Toast.LENGTH_SHORT
                )
                    .show()
                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }

            override fun successFindInfo(jsonString: String, dataList: ArrayList<Any>?) {
                if (dataList != null) {
                    mAdapter.setList(dataList)
                }

                endDate = sdf.format(Calendar.getInstance().time)
                binding.tvDate.text = "Start Scan : $startDate\nEnd Scan : $endDate"

                binding.btn.isEnabled = true
                if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
            }
        })
    }

    /**
     * RecyclerView Adapter 초기
     */
    private fun initAdapter() {
        _recyclerAdapter = BaseRecyclerViewAdapter(rvListener)
        binding.rvList.adapter = mAdapter
        binding.rvList.layoutManager = LinearLayoutManager(this@MainActivity)
    }

    override fun onStop() {
        super.onStop()
        if (_dialog != null && _dialog!!.isShowing) _dialog!!.dismiss()
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

        } else if (requestCode == REQUEST_STORAGE_PERMISSION_GRANT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setMessage(REQUEST_PERMISSIONS)
                    builder.setPositiveButton(
                        CONFIRM
                    ) { dialog, _ -> dialog.dismiss() }
                    builder.show()
                }
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
                    val data = mAdapter.get(i) as BeanMobileNetwork
                    llBg.setBackgroundColor(getColor(if (data.currentNetworkType == "LTE") R.color.teal_200 else R.color.teal_700))
                    tvTitle.text = """${data.currentNetworkType}
                        |meas_idx : ${data.meas_idx}
                        |data_idx : ${data.data_idx}
                        |CELL_ID : ${data.CELL_ID}
                        |ARFCN : ${data.ARFCN}
                        |PCI : ${data.PCI}
                        |RSRP : ${data.RSRP}
                        |RSRQ : ${data.RSRQ}
                        |SINR : ${data.SINR}
                        |CQI : ${data.CQI}
                        |MCS : ${data.MCS}
                    """.trimMargin()
                }
                TYPE_WIFI -> {
                    val data = mAdapter.get(i) as BeanWifiData
                    llBg.setBackgroundColor(getColor(if (data.isConnected) R.color.yellow_2 else R.color.yellow))
                    tvTitle.text = """${if (data.isConnected) "WIFI_Conn" else "WIFI_Scan"}
                        |meas_idx : ${data.meas_idx}
                        |data_idx : ${data.data_idx}
                        |BSSID : ${data.BSSID}
                        |SSID : ${data.SSID}
                        |Frequency : ${data.frequency}
                        |BandWidth : ${data.bandWidth}
                        |Channel : ${data.channel}
                        |RSSI : ${data.RSSI}
                        |CINR : ${data.CINR}
                        |MCS : ${data.MCS}
                        |Standard : ${data.standard}
                    """.trimMargin()
                }
            }

        }

        override fun getItemViewType(i: Int): Int {
            return if (mAdapter.get(i) is BeanWifiData) TYPE_WIFI
            else TYPE_CELLULAR
        }
    }


}