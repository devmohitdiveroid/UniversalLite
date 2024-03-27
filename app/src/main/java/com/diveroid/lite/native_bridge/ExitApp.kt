package com.diveroid.lite.native_bridge

import android.app.Activity
import android.webkit.WebView
import com.diveroid.lite.MainActivity
import com.diveroid.lite.R

class ExitApp : _BaseBridge {
    override val cmd: String
        get() = "exitApp"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        if (activity != null && activity is MainActivity && webView != null) {
            activity.runOnUiThread {
                if (System.currentTimeMillis() > activity.back + 2000) {
                    activity.back = System.currentTimeMillis()
                    activity.showCenterToast(R.string.backkey_finish)
                } else {
                    activity.hideCenterToast()
                    activity.finish()
                }
            }
        }
    }
}
