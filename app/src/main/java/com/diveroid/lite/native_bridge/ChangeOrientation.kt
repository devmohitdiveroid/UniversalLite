package com.diveroid.lite.native_bridge

import android.app.Activity
import android.webkit.WebView
import com.diveroid.lite.MainActivity


class ChangeOrientation : _BaseBridge {
    override val cmd: String
        get() = "changeOrientation"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        if (activity != null && activity is MainActivity && webView != null) {
            activity.runOnUiThread {
                var ret = true
                try {
                    activity.changeConfiguraiont(Integer.parseInt(data!!))
                } catch (e: Exception) {
                    ret = false
                }
                if (callback != null) {
                    webView.loadUrl("javascript:${callback}(${ret});")
                }
            }
        }
    }
}
