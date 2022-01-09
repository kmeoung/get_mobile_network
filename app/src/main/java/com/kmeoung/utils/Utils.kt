package com.kmeoung.utils

import android.content.Context
import android.content.pm.PackageInfo

object Utils {

    /**
     * Android 버전 가져오기
     */
    fun getVersionInfo(context: Context):String {
        val info: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return info.versionName
    }
}