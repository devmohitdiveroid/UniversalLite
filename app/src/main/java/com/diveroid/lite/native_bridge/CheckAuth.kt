package com.diveroid.lite.native_bridge

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.AuthActivity
import com.diveroid.lite.MainActivity

class CheckAuth : _BaseBridge {
    override val cmd: String
        get() = "checkAuth"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity != null && webView != null) {
                activity.runOnUiThread {
                    //받을 권한이 있음
                    if (AuthActivity.needAskPermissions(activity).size > 0) {
                        if (callback != null) {
                            webView.loadUrl("javascript:${callback}('fail');")
                        }
                    } else {
                        if (callback != null) {
                            webView.loadUrl("javascript:${callback}('success');")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (activity != null && webView != null && callback != null) {
                activity.runOnUiThread {
                    webView.loadUrl("javascript:${callback}('fail');")
                }
            }
        }
    }

    companion object {
        var webView: WebView? = null
        var callback: String? = null
    }
}