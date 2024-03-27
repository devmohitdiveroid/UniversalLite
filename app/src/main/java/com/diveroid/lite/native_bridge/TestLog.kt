package com.diveroid.lite.native_bridge

import android.app.Activity
import android.util.Log
import android.webkit.WebView

class TestLog : _BaseBridge {
    override val cmd: String
        get() = "testLog"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        Log.e("test", "data = " + data)
        if (activity != null && webView != null && callback != null) {
            activity.runOnUiThread {
                webView.loadUrl("javascript:${callback}();")
            }
        }
    }
}
