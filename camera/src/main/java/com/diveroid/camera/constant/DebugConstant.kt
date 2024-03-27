package com.diveroid.camera.constant

import android.content.Context
import android.widget.Toast

object DebugConstant {
    var DEPTH_SUMMERY_HIDE = 1f
    var DEPTH_START_LOGGING = 0.5f
    var DEPTH_MIN_START_DIVING = 1.5f
    var DEPTH_CANCEL_LOGGING = 0.1f
    var DEPTH_FINISH_DIVING = 1.5f
    var TIME_S_START_DIVING = 10
    var TIME_S_TERM_TO_FINISH_DIVING = 120 // 120
    var TIME_TERM_RECORD_DIVING_DATA = 30 //30
    var divingDebugCnt = 0
    fun clearStaticValues() {
        initDivingConstants()
        initScaleFactor()
    }

    fun initDivingConstants() {
        divingDebugCnt = 0
        DEPTH_SUMMERY_HIDE = 1f
        DEPTH_START_LOGGING = 0.5f
        DEPTH_MIN_START_DIVING = 1.5f
        DEPTH_CANCEL_LOGGING = 0.1f
        DEPTH_FINISH_DIVING = 1.5f
        TIME_S_START_DIVING = 10
        TIME_S_TERM_TO_FINISH_DIVING = 120
        TIME_TERM_RECORD_DIVING_DATA = 30
    }

    fun nextDebugDivingControl(context: Context?) {
        divingDebugCnt++
        if (divingDebugCnt == 1) {
            val toast = Toast.makeText(context, "start_depth = 0.3f//1s, finish=30s", Toast.LENGTH_SHORT)
//leess 타겟30변경후 아래에서 크래시
//            val viewGroup = toast.view as ViewGroup
//            val toastTextView = viewGroup.getChildAt(0) as TextView
//            toastTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            toast.show()
            TIME_S_START_DIVING = 1
            DEPTH_START_LOGGING = 0.1f
            DEPTH_MIN_START_DIVING = 0.3f
        } else if (divingDebugCnt < 4) {
            TIME_S_TERM_TO_FINISH_DIVING *= 8
            val toast = Toast.makeText(context, "finish=" + TIME_S_TERM_TO_FINISH_DIVING / 60, Toast.LENGTH_SHORT)
//leess 타겟30변경후 아래에서 크래시
//            val viewGroup = toast.view as ViewGroup
//            val toastTextView = viewGroup.getChildAt(0) as TextView
//            toastTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            toast.show()
        } else if (divingDebugCnt == 4) {
            initDivingConstants()
        }
    }

    var timeScaleFactor = 1
    var depthScaleFactor = 1
    fun initScaleFactor() {
        timeScaleFactor = 1
        depthScaleFactor = 1
    }

    @Synchronized
    fun scaleTime() {
        if (timeScaleFactor > 16) {
            timeScaleFactor = 1
        } else {
            timeScaleFactor *= 2
        }
    }

    @Synchronized
    fun scaleDepth() {
        if (depthScaleFactor > 16) {
            depthScaleFactor = 1
        } else {
            depthScaleFactor *= 2
        }
    }
}