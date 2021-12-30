package com.kmeoung.getnetwork.ui.fragment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kmeoung.getnetwork.R
import com.kmeoung.getnetwork.databinding.FramgnetNetworkBinding

import android.net.wifi.ScanResult
import android.util.Log
import android.widget.Toast

import com.kmeoung.getnetwork.base.*
import com.kmeoung.getnetwork.bean.BeanData
import com.kmeoung.getnetwork.ui.activity.ActivityMain.Companion.NETWORKTYPE
import com.kmeoung.utils.WifiManager

import com.kmeoung.utils.CellularManager
import com.kmeoung.utils.IOWifiListener


class FragmentNetwork(private var networkType: NETWORKTYPE) : BaseFragment() {
    // TODO : 상용망 데이터 기준은 DBM 임

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

        val wifiManager = WifiManager(requireContext())
        if(wifiManager.checkPermissions()){
            try {
                wifiManager.scanStart(object:IOWifiListener{
                    override fun scanSuccess(results: List<ScanResult>) {
                        if (_dialog != null) _dialog!!.dismiss()
                        Toast.makeText(requireContext(), "Wifi Scan Success", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Wifi Scan Success")
                        mAdapter.clear()
                        for (result in results) {
                            mAdapter.add(
                                BeanData(
                                    "ssid) ${result.SSID}",
                                    "level) ${result.level}",
                                    "frequency) ${result.frequency}"
                                )
                            )
                        }
                    }

                    override fun scanFailure(results: List<ScanResult>?) {
                        if (_dialog != null) _dialog!!.dismiss()
                        Toast.makeText(requireContext(), "2분뒤에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Wifi Scan Failed")
                    }
                })
            }catch (e:Exception){
                e.printStackTrace()
            }
        }else{
            wifiManager.requestPermissions(requireActivity(), REQUEST_PERMISSION_GRANT)
        }
    }

    private fun getCellularInfo() {

        mAdapter.clear()
        var cellularInfo = CellularManager(requireContext())
        if (cellularInfo.checkPermissions()) {
            try {
                mAdapter.add(BeanData(cellularInfo.networkType, "${cellularInfo.getDbm()}", ""))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "상용망 정보를 확인 할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            cellularInfo.requestPermissions(requireActivity(), REQUEST_PERMISSION_GRANT)
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


    override fun onPause() {
        super.onPause()
        if (_dialog != null) _dialog!!.dismiss()
        _binding = null
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
            val bean = mAdapter.get(i) as BeanData

            val tvTitle = h.getItemView<TextView>(R.id.tv_title)
            val tvPower = h.getItemView<TextView>(R.id.tv_power)
            val tvStrength = h.getItemView<TextView>(R.id.tv_strength)

            tvTitle.text = bean.name
            tvPower.text = bean.power1

            if (bean.power2.isEmpty()) {
                tvStrength.visibility = View.GONE
            } else {
                tvStrength.visibility = View.VISIBLE
                tvStrength.text = bean.power2
            }


        }

        override fun getItemViewType(i: Int): Int = 0
    }
}