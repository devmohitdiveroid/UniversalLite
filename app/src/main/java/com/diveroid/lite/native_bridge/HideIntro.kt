package com.diveroid.lite.native_bridge

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.MainActivity

class HideIntro : _BaseBridge {
    override val cmd: String
        get() = "hideIntro"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        if(activity != null && activity is MainActivity && webView != null) {
            activity.runOnUiThread {
                activity.hideIntroAction()
            }
        }
    }
}