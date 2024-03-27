package com.diveroid.camera.filter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.diveroid.camera.underfilter.FileUtils2

object RetouchUtils {
    fun getSelectedVideos(context: Context, data: Intent): List<String> {
        val result = ArrayList<String>()

        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val videoItem = clipData.getItemAt(i)
                val videoURI = videoItem.uri
                getPath(context, videoURI)?.let {  result.add(it) }
            }
        } else {
            val videoURI = data.data
            getPath(context, videoURI)?.let { result.add(it) }
        }
        return result
    }

    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri?): String? {
        try {
            val file = FileUtils2.getFileFromUri(context, uri!!)
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

}