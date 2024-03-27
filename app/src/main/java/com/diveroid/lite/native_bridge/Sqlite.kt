package com.diveroid.lite.native_bridge

import android.app.Activity
import android.util.Log
import android.webkit.WebView
import com.diveroid.lite.util.PrefUtil
import com.diveroid.lite.util.SqliteUtil
import org.json.JSONObject

class Sqlite : _BaseBridge {
    override val cmd: String
        get() = "sqlite"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            val jsonObj = JSONObject(data!!)
            val cmd = jsonObj.getString("cmd")
            val query = jsonObj.getString("query")

            if(cmd.equals("runQuery")) {
                if(query != null) {
                    var value = SqliteUtil.getInstance(activity).runQuery(query)
                    if (activity != null && webView != null && callback != null) {
                        activity.runOnUiThread {
                            webView.loadUrl("javascript:${callback}('${value}');")
                        }
                    }
                }
            } else if(cmd.equals("selectQuery")) {
                if(query != null) {
                    var value = SqliteUtil.getInstance(activity).selectQuery(query)
                    if (activity != null && webView != null && callback != null) {
                        activity.runOnUiThread {
                            webView.loadUrl("javascript:${callback}('${value}');")
                        }
                    }
                }
            }
        } catch (e: Exception) {
        }
    }
}
