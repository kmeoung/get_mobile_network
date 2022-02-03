package com.kmeoung.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.kmeoung.getnetwork.bean.BeanMobileNetwork
import com.kmeoung.getnetwork.bean.BeanWifiData
import org.json.JSONArray
import org.json.JSONObject

import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import kotlin.experimental.and

object Utils {

    /**
     * Android Mac Address 가져오기
     */
    fun getMacAddress(): String {
        try {
            val networkInterfaceList: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            var stringMac = ""
            for (networkInterface in networkInterfaceList) {
                if (networkInterface.name.equals("wlan0", ignoreCase = true)) {
                    run {
                        for (i in networkInterface.hardwareAddress.indices) {
                            val aa = networkInterface.hardwareAddress[i] and 0xFF.toByte()
                            var stringMacByte = Integer.toHexString(
                                aa.toInt()
                            )

                            if (stringMacByte.length == 1) {
                                stringMacByte = "0$stringMacByte"
                            } else if (stringMacByte.length > 2) {
                                stringMacByte = stringMacByte.substring(
                                    stringMacByte.length - 2,
                                    stringMacByte.length
                                )
                            }
                            stringMac =
                                stringMac + stringMacByte.uppercase() + if (i < networkInterface.hardwareAddress.size - 1) ":" else ""
                        }
                    }
                }
            }
            return stringMac
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return "-999"
    }

    /**
     * Sim Operator 가져오
     */
    @SuppressLint("MissingPermission")
    fun getSimOperator(context: Context): String {
        //for dual sim mobile
        val localSubscriptionManager = SubscriptionManager.from(context)
        return if (localSubscriptionManager.activeSubscriptionInfoCount > 1) {
            //if there are two sims in dual sim mobile
            val localList: List<*> = localSubscriptionManager.activeSubscriptionInfoList
            val simInfo = localList[0] as SubscriptionInfo
//            val simInfo1 = localList[1] as SubscriptionInfo
            simInfo.displayName.toString()
            //                val sim2 = simInfo1.displayName.toString()
        } else {
            //if there is 1 sim in dual sim mobile
            val tManager = context
                .getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager
            tManager.networkOperatorName
        }
    }

    fun dataFormatToJson(dataList: ArrayList<Any>, context: Context):JSONObject {
//        cal = Calendar.getInstance()
//        endDate = sdf.format(cal!!.time)
//        binding.tvDate.text = "Start Scan : $startDate\nEnd Scan : $endDate"

        val json = JSONObject()

        val jsonGenieverse = JSONObject()
        jsonGenieverse.put("home_id", "")
        jsonGenieverse.put("meas_mode", 0)
        jsonGenieverse.put("meas_proc", 0)
        jsonGenieverse.put("position_idx_A", JSONArray())

        json.put("Com_Genieverse", jsonGenieverse)

        val jsonMRFH = JSONObject()

        jsonMRFH.put("ue_mac", getMacAddress())
        jsonMRFH.put("sim_operator", getSimOperator(context))
        jsonMRFH.put("android_api", Build.VERSION.SDK_INT)

        json.put("Com_MRFH", jsonMRFH)

        val lteNetworks = JSONArray()
        val nrNetworks = JSONArray()
        val wifiConn = JSONArray()
        val wifiScan = JSONArray()
        for (data in dataList) {
            when (data) {
                is BeanMobileNetwork -> {
                    val networks = JSONObject()
                    networks.put("meas_idx", data.meas_idx)
                    networks.put("data_idx", data.data_idx)
                    networks.put("meas_time", data.meas_time)
                    networks.put("cell_id", data.CELL_ID)
                    networks.put("arfnc", data.ARFCN)
                    networks.put("pci", data.PCI)
                    networks.put("rsrp", data.RSRP)
                    networks.put("rsrq", data.RSRQ)
                    networks.put("cqi", data.CQI)
                    networks.put("mcs", data.MCS)

                    if (data.currentNetworkType == "LTE") {
                        lteNetworks.put(networks)
                    } else {
                        nrNetworks.put(networks)
                    }
                }
                is BeanWifiData -> {
                    val wifi = JSONObject()
                    wifi.put("meas_idx", data.meas_idx)
                    wifi.put("data_idx", data.data_idx)
                    wifi.put("meas_time", data.meas_time)
                    wifi.put("bssid", data.BSSID)
                    wifi.put("ssid", data.SSID)
                    wifi.put("freq", data.frequency)
                    wifi.put("bw", data.bandWidth)
                    wifi.put("ch", data.channel)
                    wifi.put("rssi", data.RSSI)
                    wifi.put("cinr", data.CINR)
                    wifi.put("mcs", data.MCS)
                    wifi.put("standard", data.standard)

                    if (data.isConnected) {
                        wifiConn.put(wifi)
                    } else {
                        wifiScan.put(wifi)
                    }

                }
            }
        }
        json.put("LTE", lteNetworks)
        json.put("5G", nrNetworks)
        json.put("WIFI_Conn", wifiConn)
        json.put("WIFI_Scan", wifiScan)
        return json
    }
}