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

import android.telephony.gsm.GsmCellLocation
import android.util.Log
import com.kmeoung.getnetwork.bean.Bean5GData
import com.kmeoung.getnetwork.bean.BeanLteData
import com.kmeoung.getnetwork.bean.CellularData
import android.telephony.CellInfoLte
import android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
import android.telephony.CellInfo

import androidx.core.content.ContextCompat.getSystemService

import android.content.Context.TELEPHONY_SERVICE

import android.telephony.PhoneStateListener


class CellularManager(private val context: Context) {


    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val locationManager = context.getSystemService(
        Context.LOCATION_SERVICE
    ) as LocationManager

    private var mSignalStrength: SignalStrengthListener? = null

    companion object {
        val REQUIRED_PERMISSION = arrayOf<String>(
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_PHONE_STATE"
        )

        /**
         * Lte 4G
         * Nr 5G
         */
        enum class NETWORK_TYPE {
            LTE, NR
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
    private fun getType(): String {
        // todo : 버전이 낮은경우 데이터를 가져오기 위한 리스너
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mSignalStrength = SignalStrengthListener()
            telephonyManager.listen(
                mSignalStrength,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
            )
            return "LTE"
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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

    //10진수 -> 16진수
    private fun DecToHex(dec: Long): String {
        return String.format("%x", dec);
    }

    //16진수 -> 10진수
    private fun HexToDec(hex: String): Int {
        return Integer.parseInt(hex, 16);
    }

    @SuppressLint("MissingPermission")
    fun getNodeBId(cid: Long): Int {
        //16진수 cid
        var cellidHex = DecToHex(cid);

        //16진수 eNB
        var eNBHex = cellidHex.substring(0, cellidHex.length - 2);

        //10진수 eNB
        return HexToDec(eNBHex)
    }


    @SuppressLint("MissingPermission")
    fun getData(): Any? {
        val cellInfos = telephonyManager.allCellInfo
        for (cellInfo in cellInfos) {
            var dbm: Int

            when {
                getType() == "5G" -> {
                    // 우리나라 5G 상용화 19년 4월 3일
                    // Android Q 공개 날짜 19년 9월 3일
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        when (cellInfo) {
                            is CellInfoNr -> {// CellInfoNr
                                //- [ ] •5G (P (Primary), 수집된 모든 N (Neighbor) 구성, 측정 시간 동안 하나의 Cell ID 에 다수 데이터)
                                //- [ ] 5G >
                                //- [ ] (SS-)SINR,
                                //- [ ] CQI,
                                //- [ ] MCS

                                var cqi = 0
                                var mcs = 0

                                dbm = cellInfo.cellSignalStrength.dbm
                                var nci = (cellInfo.cellIdentity as CellIdentityNr).nci
                                var pci = (cellInfo.cellIdentity as CellIdentityNr).pci
                                var nrarfcn = (cellInfo.cellIdentity as CellIdentityNr).nrarfcn
                                var rsrp =
                                    (cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrp
                                var rsrq =
                                    (cellInfo.cellSignalStrength as CellSignalStrengthNr).ssRsrq
                                var sinr =
                                    (cellInfo.cellSignalStrength as CellSignalStrengthNr).ssSinr

                                return Bean5GData(
                                    getNodeBId(nci),
                                    getNodeBId(pci.toLong()),
                                    dbm,
                                    nci,
                                    nrarfcn,
                                    pci,
                                    rsrp,
                                    rsrq,
                                    sinr,
                                    cqi,
                                    mcs
                                )
                            }
                            is CellInfoLte -> { // CellInfoLte
                                //- [ ] MCS
                                var mcs: Int = 0

                                dbm = cellInfo.cellSignalStrength.dbm
                                var ci = cellInfo.cellIdentity.ci.toLong()
                                var earfcn = cellInfo.cellIdentity.earfcn
                                var pci = cellInfo.cellIdentity.pci
                                var rsrp = cellInfo.cellSignalStrength.rsrp
                                var rsrq = cellInfo.cellSignalStrength.rsrq
                                var cqi = cellInfo.cellSignalStrength.cqi
                                var sinr = cellInfo.cellSignalStrength.rssnr

                                return BeanLteData(
                                    getNodeBId(ci),
                                    getNodeBId(pci.toLong()),
                                    dbm,
                                    ci,
                                    earfcn,
                                    pci,
                                    rsrp,
                                    rsrq,
                                    sinr,
                                    cqi,
                                    mcs
                                )
                            }
                        }
                    }
                }
                getType() == "LTE" -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        var cellInfoList: List<CellInfo>
                        try {
                            cellInfoList = telephonyManager.allCellInfo
                        } catch (e: Exception) {
                            Log.d(
                                "SignalStrength",
                                "+++++++++++++++++++++++++++++++++++++++++ null array spot 1: $e"
                            )
                            return null
                        }


                        try {
                            for (cellInfo in cellInfoList) {
                                if (cellInfo is CellInfoLte) {
                                    // cast to CellInfoLte and call all the CellInfoLte methods you need
                                    // gets RSRP cell signal strength:
                                    var dbm = cellInfo.cellSignalStrength.dbm

                                    // Gets the LTE cell indentity: (returns 28-bit Cell Identity, Integer.MAX_VALUE if unknown)
                                    var ci = cellInfo.cellIdentity.ci
                                    // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                                    var pci = cellInfo.cellIdentity.pci
                                    var earfcn = -9999
                                    if (mSignalStrength != null) {
                                        var mcs = 0
                                        return BeanLteData(
                                            getNodeBId(ci.toLong()),
                                            getNodeBId(pci.toLong()),
                                            dbm,
                                            ci.toLong(),
                                            earfcn,
                                            pci,
                                            (mSignalStrength!!.rsrp ?: "-9999").toInt(),
                                            (mSignalStrength!!.rsrq ?: "-9999").toInt(),
                                            (mSignalStrength!!.rssnr ?: "-9999").toInt(),
                                            (mSignalStrength!!.cqi ?: "-9999").toInt(),
                                            mcs
                                        )
                                    } else {
                                        return null
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("SignalStrength", "++++++++++++++++++++++ null array spot 2: $e")
                            return null
                        }


                        return null
                    } else if (cellInfo is CellInfoLte) { // CellInfoLte
                        var mcs: Int = 0

                        dbm = cellInfo.cellSignalStrength.dbm
                        var ci = cellInfo.cellIdentity.ci.toLong()
                        var earfcn = -9999
                        var rsrp = -9999
                        var rsrq = -9999
                        var cqi = -9999
                        var sinr = -9999

                        // lte 상용화 시점 2004년
                        // 안드로이드 N 버전 상용화 시점 2016년 8월 22

                        earfcn = cellInfo.cellIdentity.earfcn
                        rsrp = cellInfo.cellSignalStrength.rsrp
                        rsrq = cellInfo.cellSignalStrength.rsrq
                        cqi = cellInfo.cellSignalStrength.cqi
                        sinr = cellInfo.cellSignalStrength.rssnr
                        var pci = cellInfo.cellIdentity.pci


                        return BeanLteData(
                            getNodeBId(ci),
                            getNodeBId(pci.toLong()),
                            dbm,
                            ci,
                            earfcn,
                            pci,
                            rsrp,
                            rsrq,
                            sinr,
                            cqi,
                            mcs
                        )
                    }
                }
            }
        }
        return null
    }

    private class SignalStrengthListener : PhoneStateListener() {
//        The parts[] array will then contain these elements:
//
//        part[0] = "Signalstrength:"  _ignore this, it's just the title_
//        parts[1] = GsmSignalStrength
//        parts[2] = GsmBitErrorRate
//        parts[3] = CdmaDbm
//        parts[4] = CdmaEcio
//        parts[5] = EvdoDbm
//        parts[6] = EvdoEcio
//        parts[7] = EvdoSnr
//        parts[8] = LteSignalStrength
//        parts[9] = LteRsrp
//        parts[10] = LteRsrq
//        parts[11] = LteRssnr
//        parts[12] = LteCqi
//        parts[13] = gsm|lte
//        parts[14] = _not reall sure what this number is_

        var signalStrength: String? = null
        var rsrp: String? = null
        var rsrq: String? = null
        var rssnr: String? = null
        var cqi: String? = null

        override fun onSignalStrengthsChanged(ss: SignalStrength) {
            val ssignal: String = ss.toString()
            val parts = ssignal.split(" ").toTypedArray()

            signalStrength = parts[8]
            rsrp = parts[9]
            rsrq = parts[10]
            rssnr = parts[11]
            cqi = parts[12]
        }
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
    fun isNRConnected(telephonyManager: TelephonyManager): Boolean {

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