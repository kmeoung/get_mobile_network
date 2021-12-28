package com.kmeoung.getnetwork.ui.activity

import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import com.kmeoung.getnetwork.base.BaseActivity
import com.kmeoung.getnetwork.databinding.ActivityMainBinding
import com.kmeoung.getnetwork.ui.fragment.FragmentNetwork


class ActivityMain : BaseActivity() {

    companion object{
        enum class NETWORKTYPE {
            WIFI,
            CELLULAR
        }
    }

    private lateinit var binding: ActivityMainBinding

    private var currentType: NETWORKTYPE

    init {
        currentType = NETWORKTYPE.CELLULAR
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(binding.container, FragmentNetwork(currentType), false)
        val radioGroup = binding.radioGroup
        binding.rbtnWifi.isChecked = currentType == NETWORKTYPE.WIFI
        binding.rbtnCellular.isChecked = currentType == NETWORKTYPE.CELLULAR
        radioGroup.setOnCheckedChangeListener { _, resourceId ->
            var newType: NETWORKTYPE = currentType
            when (resourceId) {
                binding.rbtnWifi.id ->
                    if (newType != NETWORKTYPE.WIFI) newType = NETWORKTYPE.WIFI


                binding.rbtnCellular.id ->
                    if (newType != NETWORKTYPE.CELLULAR) newType = NETWORKTYPE.CELLULAR


            }
            if (newType != currentType) {
                currentType = newType
                replaceFragment(binding.container, FragmentNetwork(currentType), false)
                Toast.makeText(baseContext,"Network Type Changed",Toast.LENGTH_SHORT).show()
            }
        }
    }
}

