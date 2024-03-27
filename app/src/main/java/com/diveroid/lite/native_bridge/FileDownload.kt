package com.diveroid.lite.native_bridge

import android.app.Activity
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.webkit.WebView
import org.json.JSONArray
import java.io.File


class FileDownload : _BaseBridge {
    override val cmd: String
        get() = "fileDownload"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity == null) {
                throw  Exception("")
            }
            val arr = JSONArray(data!!)
            (0 until arr.length()).forEach {
                val json = arr.getJSONObject(it)
                val uri = Uri.parse(json.getString("fileName"))



                val file = File(activity.getExternalFilesDir(null).toString() + uri.path!!.replace("files/", ""))
                val fileName = Uri.parse(json.getString("fileName")).lastPathSegment!!
                var to = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + fileName)
                if (to.exists()) {
                    var seq = 1
                    val ext = to.extension
                    while (to.exists()) {
                        to = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + File.separator + (fileName.dropLast(
                                ext.length + 1
                            ) + " (" + seq + ").") + ext
                        )
                        seq++
                    }
                }
                file.copyTo(to, true)
                MediaScannerConnection.scanFile(activity, arrayOf(to.absolutePath), null, MediaScannerConnection.OnScanCompletedListener { _, _ ->
                    if (it == arr.length() - 1 && webView != null && callback != null) {
                        activity.runOnUiThread {
                            webView.loadUrl("javascript:${callback}('success');")
                        }
                    }
                })
            }
        } catch (e: Exception) {
            if (activity != null && webView != null && callback != null) {
                activity.runOnUiThread {
                    webView.loadUrl("javascript:${callback}('fail');")
                }
            }
            e.printStackTrace()
        }
    }
}