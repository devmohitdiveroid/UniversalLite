package com.diveroid.lite.native_bridge

import android.app.Activity
import android.content.Intent
import android.webkit.WebView
import com.diveroid.lite.data.PrefData
import com.diveroid.lite.util.PrefUtil
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.gson.Gson

class ShowLicense : _BaseBridge {
    override val cmd: String
        get() = "showLicense"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity == null) return
            activity.runOnUiThread {
                activity.startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
                var ls: String = ""
                try {
                    ls = Gson().fromJson(PrefUtil.getInstance(activity).getString("PREF_LANG_SETTING"), PrefData::class.java).value ?: ""
                } catch (e: Exception) {
                }
                OssLicensesMenuActivity.setActivityTitle(if (ls == "en") "Open source license" else "오픈소스 라이선스")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}