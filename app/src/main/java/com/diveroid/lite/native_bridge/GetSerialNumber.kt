package com.diveroid.lite.native_bridge

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.AuthActivity
import com.diveroid.lite.MainActivity
import com.diveroid.lite.SerialScanActivity

class GetSerialNumber : _BaseBridge {
    override val cmd: String
        get() = "getSerialNumber"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity != null && webView != null) {
                GetSerialNumber.webView = webView
                GetSerialNumber.callback = callback

                activity.runOnUiThread {
                    val intent = Intent(activity, SerialScanActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            if (webView != null && callback != null) {
                webView.loadUrl("javascript:${callback}('');")
            }
        }
    }

    companion object {
        var webView: WebView? = null
        var callback: String? = null
    }
}