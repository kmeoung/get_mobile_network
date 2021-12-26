package com.kmeoung.getnetwork.ui.activity

import android.os.Bundle
import com.kmeoung.getnetwork.base.BaseActivity
import com.kmeoung.getnetwork.databinding.ActivityMainBinding
import com.kmeoung.getnetwork.ui.fragment.FragmentWifi


class ActivityMain : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(binding.container,FragmentWifi(),false)
    }
}