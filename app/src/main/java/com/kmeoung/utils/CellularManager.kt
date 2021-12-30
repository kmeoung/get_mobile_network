package com.kmeoung.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kmeoung.getnetwork.bean.BeanCellular
import com.kmeoung.getnetwork.ui.fragment.FragmentNetwork
import java.lang.Exception
import java.util.*

class CellularManager(private val context: Context) {

    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private var cellularType: CELLULAR_TYPE = CELLULAR_TYPE.NONE


    companion object {
        val REQUIRED_PERMISSION = arrayOf<String>(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_PHONE_STATE"
        )

        enum class CELLULAR_TYPE {
            TYPE_3G, TYPE_LTE, TYPE_5G, NONE
        }
    }

    /**
     * 필수 권한 확인
     * checkPermissions
     * @return Boolean
     * @required
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
        val cellInfo = telephonyManager.allCellInfo[0]

        cellularType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (cellInfo) {
                is CellInfoGsm -> CELLULAR_TYPE.TYPE_3G
                is CellInfoLte -> CELLULAR_TYPE.TYPE_LTE
                is CellInfoNr -> CELLULAR_TYPE.TYPE_5G
                else -> CELLULAR_TYPE.NONE
            }
        } else {
            when (cellInfo) {
                is CellInfoGsm -> CELLULAR_TYPE.TYPE_3G
                is CellInfoLte -> CELLULAR_TYPE.TYPE_LTE
                else -> CELLULAR_TYPE.NONE
            }
        }

        return isGranted
    }

    /**
     * 필수 권한 요청
     */
    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, REQUIRED_PERMISSION,
            requestCode
        )
    }


    /**
     * GET LTE RSRP
     * checkPermission
     * @return rsrp
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun getLteRsrp(): Int {
        if (checkPermissions()) {
            val cellInfo = telephonyManager.allCellInfo[0]
            val cellSignalStrengthLte = (cellInfo as CellInfoLte).cellSignalStrength
            return cellSignalStrengthLte.rsrp
        } else throw Exception("please check permissions")
    }

    /**
     * GET 5G RSRP
     * checkPermission
     * @return csi rsrp
     */

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun get5GCsiRsrp(): Int {
        if (checkPermissions()) {
            val cellInfo = telephonyManager.allCellInfo[0]
            val cellSignalStrengthNr: CellSignalStrengthNr =
                ((cellInfo as CellInfoNr).cellSignalStrength as CellSignalStrengthNr)
            return cellSignalStrengthNr.csiRsrp
        } else throw Exception("please check permissions")
    }

    /**
     * GET 5G RSRP
     * checkPermission
     * @return ss rsrp
     */

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun get5GSsRsrp(): Int {
        if (checkPermissions()) {
            val cellInfo = telephonyManager.allCellInfo[0]
            val cellSignalStrengthNr: CellSignalStrengthNr =
                ((cellInfo as CellInfoNr).cellSignalStrength as CellSignalStrengthNr)
            return cellSignalStrengthNr.ssRsrp
        } else throw Exception("please check permissions")
    }

    /**
     * 보통 사용하는 상용망 속도 기준
     * checkPermission
     * @return dbm
     */

    @SuppressLint("MissingPermission")
    @Throws(Exception::class)
    fun getDbm(): Int {
        if (checkPermissions()) {
            val cellInfo = telephonyManager.allCellInfo[0]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // CellInfo
                return cellInfo.cellSignalStrength.dbm
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo is CellInfoNr) { // CellInfoNr
                return cellInfo.cellSignalStrength.dbm
            } else if (cellInfo is CellInfoLte) { // CellInfoLte
                return cellInfo.cellSignalStrength.dbm
            }
            return (cellInfo as CellInfoGsm).cellSignalStrength.dbm
        } else throw Exception("please check permissions")
    }

    /**
     * 상용망 네트워크 타입
     * @return Network Type
     */
    var networkType = ""
    get() {
        return when (cellularType) {
            CELLULAR_TYPE.TYPE_3G -> "3G"
            CELLULAR_TYPE.TYPE_LTE -> "LTE"
            CELLULAR_TYPE.TYPE_5G -> "5G"
            else -> "확인불가"
        }
    }

}