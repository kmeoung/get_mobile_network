package com.kmeoung.utils

interface IOMobileNetworkListener {

    fun denidedPermission()

    fun disableGps()

    fun disableInternet()

    fun wifiSearchCountOver()

    fun successFindInfo(jsonString : String,dataList:ArrayList<Any>? = null)
}