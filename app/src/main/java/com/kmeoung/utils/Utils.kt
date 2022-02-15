package com.kmeoung.utils

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity

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
                        if (networkInterface.hardwareAddress != null) {
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
                        }else{
                            return "-999"
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


}