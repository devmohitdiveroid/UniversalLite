package com.diveroid.lite.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class LogUtil {
    companion object {
        fun log(tag: String, log: String) {
            Log.d(tag, log)
        }
    }
}