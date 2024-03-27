package com.diveroid.lite.native_bridge

import android.app.Activity
import android.webkit.WebView
import com.diveroid.lite.MainActivity
import com.diveroid.lite.util.PrefUtil

class GetContact : _BaseBridge {
    override val cmd: String
        get() = "getContact"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            (activity as MainActivity).getContact(callback)
//            if (activity != null && webView != null && callback != null) {
//                activity.runOnUiThread {
//                    webView.loadUrl("javascript:${callback}('${value}');")
//                }
//            }
        } catch (e: Exception) {
        }
    }
}
