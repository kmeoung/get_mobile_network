package com.kmeoung.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object Comm_Prefs {

    private lateinit var mContext: Context
    private lateinit var prefs: SharedPreferences

    fun init(context:Context){
        mContext = context
        prefs = mContext.getSharedPreferences(Comm_Prefs_Param.APP_NAME,MODE_PRIVATE)
    }

    fun setAndroidId(android_id:String?){
        prefs.edit().putString(Comm_Prefs_Param.ANDROID_ID,android_id).commit()
    }

    fun getAndroidId(): String?{
        return prefs.getString(Comm_Prefs_Param.ANDROID_ID,null)
    }
}