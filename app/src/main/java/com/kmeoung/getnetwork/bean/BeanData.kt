package com.kmeoung.getnetwork.bean

enum class DATA_TYPE{
    WIFI,CELLULAR
}

data class BeanData(var name:String,var strength :Int,var dataType :DATA_TYPE,)