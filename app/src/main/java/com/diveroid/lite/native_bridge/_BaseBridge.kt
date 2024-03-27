package com.diveroid.lite.native_bridge

import android.app.Activity
import android.net.Uri
import android.webkit.WebView

interface _BaseBridge {
    val cmd: String

    fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?)
}
