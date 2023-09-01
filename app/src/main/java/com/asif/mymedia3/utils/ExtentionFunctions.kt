package com.asif.mymedia3.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

fun getFilePath(context: Context): String? {

    val currentTimeMillis = System.currentTimeMillis()
    val today = Date(currentTimeMillis)
    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
    val fileName: String = "media3_" + dateFormat.format(today) + ".mp4"

    val documentsDirectory = context.getExternalFilesDir(null)?.absolutePath ?: ""
    val file = File(documentsDirectory, fileName)

    if (!file.exists()) {
        file.createNewFile()
        println("No file found file created ${file.absolutePath}")
    }

    return file.absolutePath
}

fun logE(tag: String, message: String){
    Log.e(tag, message)
}