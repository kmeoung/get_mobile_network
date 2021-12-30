package com.kmeoung.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 현재 와이파이 체크
 */
class WifiManager(private var context: Context) {

    private val wifiManager : WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val intentFilter = IntentFilter()

    companion object {
        val REQUIRED_PERMISSION = arrayOf<String>(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.CHANGE_WIFI_STATE"
        )
    }

    /**
     * 필수 권한 확인
     * checkPermissions
     * @return Boolean
     */
    fun checkPermissions(): Boolean {
        var isGranted = true
        for (permission in REQUIRED_PERMISSION) {
            val per = ContextCompat.checkSelfPermission(context, permission)
            if (per != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        return isGranted
    }

    /**
     * 필수 권한 요청
     * @param activity, requestCode
     */
    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, REQUIRED_PERMISSION,
            requestCode
        )
    }

    fun scanStart(listener : IOWifiListener?) {
        // 안드로이드 배터리 수명을 늘리기 위해 안드로이드 와이파이 검색을 하는 주기를 제한함
        // Android 8 / 8.1
        // 백그라운드 앱은 30분 간격으로 1회 스캔 가능
        // Android 9이상
        // 각 포그라운드 앱은 2분 간격으로 4회 스캔할 수 있습니다. 이 경우, 단시간에 여러 번의 스캔이 가능하게 됩니다.
        // 백그라운드 앱은 모두 합쳐서 30분 간격으로 1회 스캔할 수 있습니다.
        // 현재 와이파이 여러개가 검색이 되는건지 된다면 어떤식으로 표시되는지 테스트 필

        val wifiScanReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    val results = wifiManager.scanResults
                    if (listener != null) listener!!.scanSuccess(results)
                } else {
                    val results = wifiManager.scanResults
                    if (listener != null) listener!!.scanFailure(results)
                }
            }
        }

        context.registerReceiver(wifiScanReceiver, intentFilter)
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

        val success = wifiManager.startScan()
        if (!success) {
            // scan failure handling
            if(listener != null) listener!!.scanFailure(null)
        }
    }
}