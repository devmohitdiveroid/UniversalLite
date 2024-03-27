package com.diveroid.lite.native_bridge

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.util.PrefUtil
import org.json.JSONObject

class SetPrefData : _BaseBridge {
    override val cmd: String
        get() = "setPrefData"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            val jsonObj = JSONObject(data!!)
            val key = jsonObj.getString("key")
            val value = jsonObj.getString("value")
            PrefUtil.getInstance(activity).put(key, value)
        } catch (e: Exception) {
        }
    }
}
