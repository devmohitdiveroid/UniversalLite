package com.diveroid.lite.native_bridge

import android.app.Activity
import android.webkit.WebView
import com.diveroid.lite.util.PrefUtil

class GetPrefData : _BaseBridge {
    override val cmd: String
        get() = "getPrefData"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            val value = PrefUtil.getInstance(activity).getString(data, "")
            if (activity != null && webView != null && callback != null) {
                activity.runOnUiThread {
                    webView.loadUrl("javascript:${callback}('${value}');")
                }
            }
        } catch (e: Exception) {
        }
    }
}
