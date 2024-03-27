package com.diveroid.lite.native_bridge

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import com.diveroid.camera.data.MediaData
import com.diveroid.camera.filter.RetouchResultActivity
import com.diveroid.lite.MediaFilterActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ShowFilterView : _BaseBridge {
    override val cmd: String
        get() = "showFilterView"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {
            if (activity != null) {
                Log.d(cmd, "startAction: data = $data")
                val token = object: TypeToken<List<MediaData>>() {}.type
                val dataList = Gson().fromJson<List<MediaData>>(data, token)
                dataList.forEach {
                    Log.d(cmd, "startAction: dataList = ${Gson().toJson(it)}")
                }

                activity.runOnUiThread {
                    Intent(activity.applicationContext, MediaFilterActivity::class.java).apply {
                        callback?.let {
                            putExtra(RetouchResultActivity.CALLBACK_NAME, it)
                        }
                        putExtra(RetouchResultActivity.IMAGE_OR_VIDEO_URIS, data)
                    }.run {
                        activity.startActivityForResult(this, RetouchResultActivity.REQUEST_CODE_RETOUCH)
//                        activity.startActivity(this)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
