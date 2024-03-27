package com.diveroid.lite.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object {
        fun toString(date: Date, format: String): String {
            val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
            return dateFormatter.format(date)
        }
    }
}