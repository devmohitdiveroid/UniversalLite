package com.diveroid.lite.util

/**
 * Created by mjpark on 2022-06-23.
 */
import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager

class KeyboardUtil(private val act: Activity, private val contentView: View) {
    private var decorView: View = act.window.decorView

    fun enable() {
        decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    fun disable() {
        decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val r = Rect()
        decorView.getWindowVisibleDisplayFrame(r)
        val height = decorView.context.resources.displayMetrics.heightPixels
        var diff = height - r.bottom
        if (diff > 0) {
            if (Build.VERSION.SDK_INT >= 28) {
                val windowInsets: WindowInsets? = decorView.rootWindowInsets
                if (windowInsets?.displayCutout != null) {
                    diff += windowInsets.displayCutout!!.boundingRects[0]
                        .height()
                }
            }
            if (contentView.paddingBottom != diff) {
                contentView.setPadding(0, 0, 0, diff)
            }
        } else {
            if (contentView.paddingBottom != 0) {
                contentView.setPadding(0, 0, 0, 0)
            }
        }
    }

    companion object {
        /**
         * hide the keyboard
         *
         * @param activity
         */
        fun hideKeyboard(activity: Activity?) {
            if (activity?.currentFocus != null) {
                val inputMethodManager =
                    activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken, 0
                )
            }
        }
    }
}