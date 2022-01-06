package com.kmeoung.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import android.widget.Toast

import java.io.FileWriter

import java.io.BufferedWriter

import android.os.Environment

import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException

import java.io.FileNotFoundException

import java.io.FileReader

import java.io.BufferedReader


object WriteTextManager {

    private val TAG = "WRITE_TEXT_MANAGER"

    val REQUIRED_PERMISSION = arrayOf<String>(
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.READ_EXTERNAL_STORAGE"
    )

    var saveStorage = "" //저장된 파일 경로

    var saveData = "" //저장된 파일 내용


    //TODO ==== 텍스트 저장 메소드 ====
    fun setSaveText(context: Context, data: String): String {
        try {

            val now = System.currentTimeMillis() //TODO 현재시간 받아오기
            val date = Date(now) //TODO Date 객체 생성
            var sdf = SimpleDateFormat("yyyyMMddkkmmss")
            var nowTime: String = sdf.format(date)
            sdf = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
            saveData = data //TODO 변수에 값 대입
            val textFileName = "/$nowTime.txt"
            nowTime = sdf.format(date)
            //TODO 파일 생성
            val storageDir =
                File(Environment.getExternalStorageDirectory().absolutePath + "/Wifi_And_Cellular_Checker") //TODO 저장 경로
            //TODO 폴더 생성
            if (!storageDir.exists()) { //TODO 폴더 없을 경우
                storageDir.mkdir() //TODO 폴더 생성
            }

            //BufferedWriter buf = new BufferedWriter(new FileWriter(storageDir+textFileName, true)); //TODO 다중으로 내용적음 (TRUE)
            val buf = BufferedWriter(
                FileWriter(
                    storageDir.toString() + textFileName,
                    false
                )
            ) //TODO 한개 내용만 표시됨 (FALSE)
            buf.append("[$nowTime]\n$saveData") //TODO 날짜 쓰기
            buf.newLine() //TODO 개행
            buf.close()
            saveStorage = storageDir.toString() + textFileName //TODO 경로 저장 /storage 시작
            //saveStorage = String.valueOf(storageDir.toURI()+textFileName); //TODO 경로 저장 file:/ 시작
            Log.d(TAG, "---")
            Log.w(TAG, "================================================")
            Log.d(
                TAG, """
     
     [A_TextFile > 저장한 텍스트 파일 확인 실시]
     """.trimIndent()
            )
            Log.d(TAG, "\n[경로 : $saveStorage]")
            Log.d(TAG, "\n[제목 : $nowTime]")
            Log.d(TAG, "\n[내용 : $saveData]")
            Log.w(TAG, "================================================")
            Log.d(TAG, "---")
            Toast.makeText(context, "텍스트 파일이 저장되었습니다", Toast.LENGTH_SHORT).show()
            return saveStorage
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * 필수 권한 확인
     * checkPermissions
     * @return Boolean
     */
    fun checkPermissions(context: Context): Boolean {
        var isGranted = true
        for (permission in REQUIRED_PERMISSION) {
            val per = ContextCompat.checkSelfPermission(context, permission)
            if (per != PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        return isGranted
    }

    /**
     * 필수 권한 요청
     * @param activity, requestCode
     */
    fun requestPermissions(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
            activity, REQUIRED_PERMISSION,
            requestCode
        )
    }


}