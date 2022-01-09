package com.kmeoung.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception
import android.telephony.TelephonyManager

import android.net.ConnectivityManager

import android.net.NetworkInfo
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import android.util.Log
import androidx.core.content.PermissionChecker
import com.kmeoung.getnetwork.bean.CellularData
import com.kmeoung.getnetwork.bean.DATA_TYPE

import java.lang.reflect.Method


class CellularManager(private val context: Context) {

    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val locationManager = context.getSystemService(
        Context.LOCATION_SERVICE
    ) as LocationManager

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
     * 무선 네트워크 사용 여부 체크
     */
    fun checkInternet(): Boolean {
        // 시스템 > 설정 > 위치 및 보안 > 무선 네트워크 사용 여부 체크.
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    }

    /**
     * Gps 사용 여부 체
     */
    fun checkGps(): Boolean {
        // 시스템 > 설정 > 위치 및 보안 > GPS 위성 사용 여부 체크.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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


    @RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
    fun getType(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (telephonyManager.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_IDEN,
                TelephonyManager.NETWORK_TYPE_1xRTT ->
                    return "2G"
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B ->
                    return "3G"
                TelephonyManager.NETWORK_TYPE_LTE -> {
                    return if (isNRConnected(
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager,
                            context
                        )
                    ) "5G" else "LTE"
                }

                TelephonyManager.NETWORK_TYPE_NR ->
                    return "5G"
                else -> return "Unknown"
            }
        }
        return "Unknown"
    }


    /**
     * GET LTE RSRP
     * checkPermission
     * @return rsrp
     */

    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    @Throws(Exception::class)
    fun getLteRsrp(): Int {
        val cellInfo = telephonyManager.allCellInfo[0]
        val cellSignalStrengthLte = (cellInfo as CellInfoLte).cellSignalStrength
        return cellSignalStrengthLte.rsrp
    }

    /**
     * GET 5G RSRP
     * checkPermission
     * @return csi rsrp
     */

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun get5GCsiRsrp(): Int {
        val cellInfo = telephonyManager.allCellInfo[0]
        val cellSignalStrengthNr: CellSignalStrengthNr =
            ((cellInfo as CellInfoNr).cellSignalStrength as CellSignalStrengthNr)
        return cellSignalStrengthNr.csiRsrp
    }


    /**
     * GET 5G RSRP
     * checkPermission
     * @return ss rsrp
     */

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun get5GSsRsrp(): Int {
        val cellInfo = telephonyManager.allCellInfo[0]
        val cellSignalStrengthNr: CellSignalStrengthNr =
            ((cellInfo as CellInfoNr).cellSignalStrength as CellSignalStrengthNr)
        return cellSignalStrengthNr.ssRsrp
    }

    /**
     * 보통 사용하는 상용망 속도 기준
     * checkPermission
     * @return List<network_type,dbm>
     */

    @SuppressLint("MissingPermission")
    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    fun getDbms(): ArrayList<CellularData> {
        val cellInfos = telephonyManager.allCellInfo
        Log.d("TEST_11", getType())

        var array = ArrayList<CellularData>()
        for (cellInfo in cellInfos) {
            // TODO : Cell 즉 기지국 정보마다 Network Type이 정확한 데이터 인지 확인 필요
            var type = getType()
            var dbm: Int? = null
            var cid: Long? = null
            var pcid: Int? = null
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    when (cellInfo) {
                        is CellInfoNr -> {// CellInfoNr
                            dbm = cellInfo.cellSignalStrength.dbm
                            if (Build.VERSION.SDK_INT >= 30) {
                                cid = (cellInfo.cellIdentity as CellIdentityNr).nci
                                pcid = (cellInfo.cellIdentity as CellIdentityNr).pci
                            } else {
                                cid =
                                    (telephonyManager.cellLocation as GsmCellLocation).cid.toLong()
                            }
                        }
                        is CellInfoLte -> { // CellInfoLte
                            dbm = cellInfo.cellSignalStrength.dbm
                            if (Build.VERSION.SDK_INT >= 30) {
                                cid = cellInfo.cellIdentity.ci.toLong()
                                pcid = cellInfo.cellIdentity.pci
                            } else {
                                cid =
                                    (telephonyManager.cellLocation as GsmCellLocation).cid.toLong()
                            }
                        }
                        is CellInfoGsm -> {
                            dbm = cellInfo.cellSignalStrength.dbm
                            if (Build.VERSION.SDK_INT >= 30) {
                                cid = cellInfo.cellIdentity.cid.toLong()
                                pcid = cellInfo.cellIdentity.psc
                            } else {
                                cid =
                                    (telephonyManager.cellLocation as GsmCellLocation).cid.toLong()
                            }
                        }
                    }
                }
                cellInfo is CellInfoLte -> { // CellInfoLte
                    dbm = cellInfo.cellSignalStrength.dbm
                    if (Build.VERSION.SDK_INT >= 30) {
                        cid = cellInfo.cellIdentity.ci.toLong()
                        pcid = cellInfo.cellIdentity.pci
                    } else {
                        cid = (telephonyManager.cellLocation as GsmCellLocation).cid.toLong()
                    }
                }
                cellInfo is CellInfoGsm -> {
                    dbm = cellInfo.cellSignalStrength.dbm
                    if (Build.VERSION.SDK_INT >= 30) {
                        cid = cellInfo.cellIdentity.cid.toLong()
                        pcid = cellInfo.cellIdentity.psc
                    } else {
                        cid = (telephonyManager.cellLocation as GsmCellLocation).cid.toLong()
                    }
                }
            }
            array.add(CellularData(cid, type, dbm, pcid))// CellInfo
        }
        return array
    }

    /**
     * 5G 체크
     */
    fun isNRConnected(telephonyManager: TelephonyManager, context: Context): Boolean {

        try {
            val obj = Class.forName(telephonyManager.javaClass.name)
                .getDeclaredMethod("getServiceState", *arrayOfNulls(0))
                .invoke(telephonyManager, *arrayOfNulls(0))
            // try extracting from string
            val serviceState = obj.toString()
            val is5gActive = (serviceState.contains("nrState=CONNECTED") ||
                    serviceState.contains("nsaState=5") ||
                    serviceState.contains("EnDc=true") &&
                    serviceState.contains("5G Allocated=true"))
            if (is5gActive) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}