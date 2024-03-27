package com.diveroid.lite.native_bridge

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.MainActivity
import com.diveroid.lite.R
import com.diveroid.lite.util.PrefUtil
import org.json.JSONObject

class SetStatusBarFontColor : _BaseBridge {
    override val cmd: String
        get() = "setStatusBarFontColor"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity != null && webView != null) {
                activity.runOnUiThread {
                    (activity as MainActivity).setStatusBarFontColor(data.toBoolean())
                    if (callback != null) {
                        webView.loadUrl("javascript:${callback}();")
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}
