package com.kmeoung.getnetwork.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager

/**
 * 느낌이 현재 사용중인 와이파이 체크 하는 것 같
 */
class BaseWifiManager(context: Context,listener: IOWifiListener) {

    private val wifiManager : WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    init{
        // Wifi Manager
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)


        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    val results = wifiManager.scanResults
                    listener.scanSuccess(results)
                } else {
                    val results = wifiManager.scanResults
                    listener.scanFailure(results)
                }
            }
        }

        context.registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun scanStart() {
        // 안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
        // Android 8 / 8.1
        // 백그라운드 앱은 30분 간격으로 1회 스캔 가능
        // Android 9이상
        // 각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
        // 백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
        // 현재 와이파이 여러개가 검색이 되는건지 된다면 어떤식으로 표시되는지 테스트 필
        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            scanFailure()
        }
    }



    private fun scanSuccess() {

    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!

    }
}