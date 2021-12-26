package com.kmeoung.getnetwork.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kmeoung.getnetwork.R
import com.kmeoung.getnetwork.base.BaseFragment
import com.kmeoung.getnetwork.base.BaseRecyclerViewAdapter
import com.kmeoung.getnetwork.base.BaseViewHolder
import com.kmeoung.getnetwork.base.IORecyclerViewListener
import com.kmeoung.getnetwork.databinding.FramgnetNetworkBinding
import com.kmeoung.getnetwork.ui.activity.NETWORKTYPE
import android.Manifest.permission

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat




class FragmentNetwork(private var networkType: NETWORKTYPE) : BaseFragment() {

    private var _binding: FramgnetNetworkBinding? = null
    private val binding get() = _binding!!

    private var mAdapter : BaseRecyclerViewAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FramgnetNetworkBinding.inflate(inflater, container, false)
        val view = binding.root


        val rvList = binding.rvList

        rvList.layoutManager = LinearLayoutManager(context)
        mAdapter = BaseRecyclerViewAdapter(rvListener)
        rvList.adapter = mAdapter

        binding.btnGetNetwork.setOnClickListener {
            when (networkType) {
                NETWORKTYPE.WIFI ->
                    getWifiInfo()
                NETWORKTYPE.CELLULAR ->
                    getCellularInfo()
            }
        }

        return view
    }

    private fun getWifiInfo() {
//        WIFI 제한사항
//        안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
//        Android 8 / 8.1
//        백그라운드 앱은 30분 간격으로 1회 스캔 가능
//        Android 9이상
//        각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
//        백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
//        ContextCompat.checkSelfPermission(context,PERMISSION)
    }

    private fun getCellularInfo() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val rvListener: IORecyclerViewListener = object : IORecyclerViewListener {
        override val itemCount: Int
            get() = if(mAdapter != null) mAdapter!!.size() else 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            return BaseViewHolder.newInstance(R.layout.listitem_temp, parent, false)
        }

        override fun onBindViewHolder(h: BaseViewHolder, i: Int) {
            val tvTitle = h.getItemView<TextView>(R.id.tv_title)
            val tvPower = h.getItemView<TextView>(R.id.tv_power)
            val tvStrength = h.getItemView<TextView>(R.id.tv_strength)

        }

        override fun getItemViewType(i: Int): Int = 0
    }
}