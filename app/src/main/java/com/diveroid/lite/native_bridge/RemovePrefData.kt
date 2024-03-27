package com.diveroid.lite.native_bridge

import android.app.Activity
import android.webkit.WebView
import com.diveroid.lite.util.PrefUtil

class RemovePrefData : _BaseBridge {
    override val cmd: String
        get() = "removePrefData"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        if (data != null) {
            PrefUtil.getInstance(activity).remove(data)
        }
    }
}
