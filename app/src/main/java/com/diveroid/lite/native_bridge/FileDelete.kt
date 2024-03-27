package com.diveroid.lite.native_bridge

import android.app.Activity
import android.net.Uri
import android.webkit.WebView
import com.diveroid.lite.util.LogUtil
import com.diveroid.lite.util.SqliteUtil
import org.json.JSONArray
import java.io.File


class FileDelete : _BaseBridge {
    override val cmd: String
        get() = "fileDelete"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            val arr = JSONArray(data!!)
            if (arr.length() < 1) {
                throw Exception("no data")
            }
            val ids = ArrayList<String>()
            (0 until arr.length()).forEach {
                val json = arr.getJSONObject(it)
                ids.add(json.getString("mediaId"))
//                val fileName = Uri.parse(json.getString("fileName")).lastPathSegment!!
                val uri = Uri.parse(json.getString("fileName"))
                val path = activity!!.getExternalFilesDir(null).toString() + uri.path!!.replace("files/", "")
                try {
                    val file = File(path);
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {
                    LogUtil.log(cmd, e.toString())
                }
                if (json.has("thumbName") && json.getString("thumbName").isNotEmpty()) {
                    val thumbUri = Uri.parse(json.getString("thumbName"))
                    val thumbPath = activity.getExternalFilesDir(null).toString() + thumbUri.path!!.replace("files/", "")
                    try {
                        val thumbFile = File(thumbPath);
                        if (thumbFile.exists()) {
                            thumbFile.delete()
                        }
                    } catch (e: Exception) {
                        LogUtil.log(cmd, e.toString())
                    }
                }
            }
            val sql = " UPDATE " +
                    "       TB_MEDIA" +
                    "   SET" +
                    "       deleted = 1" +
                    "   WHERE " +
                    "       mediaId in (${ids.joinToString(",")})"
            val sql2 = " UPDATE " +
                    "       TB_COLLECTION_MEDIA" +
                    "   SET" +
                    "       deleted = 1" +
                    "   WHERE " +
                    "       mediaId in (${ids.joinToString(",")})"
            SqliteUtil.getInstance(activity).runQuery(sql)
            SqliteUtil.getInstance(activity).runQuery(sql)
            if (webView != null && callback != null) {
                activity!!.runOnUiThread {
                    webView.loadUrl("javascript:${callback}('success');")
                }
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
