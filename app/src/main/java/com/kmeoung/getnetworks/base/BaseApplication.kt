package com.kmeoung.getnetworks.base

import android.app.Application
import com.kmeoung.utils.Comm_Prefs

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val prefs = Comm_Prefs
        prefs.init(applicationContext)
    }
}