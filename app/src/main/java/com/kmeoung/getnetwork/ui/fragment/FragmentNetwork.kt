package com.kmeoung.getnetwork.ui.fragment

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
    })
}