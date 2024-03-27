package com.diveroid.lite

/**
 * Created by mjpark on 2022-06-23.
 */

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.diveroid.lite.util.KeyboardUtil

abstract class BaseActivity : ComponentActivity() {

    protected lateinit var mContext: Context
    protected lateinit var mActivity: BaseActivity
    protected lateinit var mHandler: Handler

    private var mToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        mContext = this
        mActivity = this
        mHandler = Handler()
//        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        window.statusBarColor = Color.TRANSPARENT

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT
    }

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val metrics = resources.displayMetrics
            result = (resources.getDimensionPixelSize(resourceId) / metrics.density).toInt()
        }
        return result;
    }

    fun setStatusBarFontColor(isLight: Boolean = true) {
        if(isLight) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window ?: return
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }


    override fun onResume() {
        super.onResume()
        KeyboardUtil(this, findViewById(android.R.id.content)).enable()
    }

    override fun onPause() {
        super.onPause()
        KeyboardUtil(this, findViewById(android.R.id.content)).disable()
        try {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (e: NullPointerException) {
        }
    }

    fun showCenterToast(msgRes: Int, duration: Int = Toast.LENGTH_LONG) {
        hideCenterToast()
        runOnUiThread {
            mToast = Toast.makeText(mContext, msgRes, duration)
            mToast!!.setGravity(Gravity.CENTER, 0, 0)
            mToast!!.show()
        }
    }

    fun hideCenterToast() {
        runOnUiThread {
            if (mToast != null) {
                mToast!!.cancel()
                mToast = null
            }
        }
    }
}
