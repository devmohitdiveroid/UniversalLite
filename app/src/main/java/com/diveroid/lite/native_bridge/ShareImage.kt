package com.diveroid.lite.native_bridge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import org.json.JSONObject


class ShareImage : _BaseBridge {
    override val cmd: String
        get() = "shareImage"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            val json = JSONObject(data!!)
            val shareIntent: Intent = Intent().apply {
                type = if (json.getString("fileType") == "VIDEO") "video/*" else "image/*"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, Uri.parse(json.getString("fileName")))
            }
//            activity!!.startActivity(Intent.createChooser(shareIntent, "DIVEROID Lite"))
            activity!!.startActivity(shareIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
