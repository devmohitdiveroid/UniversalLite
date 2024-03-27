package com.diveroid.camera.housing

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import com.diveroid.camera.provider.ContextProvider

/**
 * ojk
 * https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
 */
class ClickViewLayer(context: Context?, attrs: AttributeSet?) : FrameLayout(context!!, attrs) {
    private val TAG = ClickViewLayer::class.java.simpleName
    private var controlView: TouchReceiverBaseView? = null
    override fun invalidate() {
        super.invalidate()
        controlView!!.invalidate()
    }


//    @BindingAdapter("app:isPortrait")
//    fun initPortrait(isPortrait: Boolean = false) {
//        Log.d(TAG, "initPortrait: ")
//        val p = Point()
//        val windowManager = ContextProvider.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        windowManager.defaultDisplay.getRealSize(p)
//        val dm = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(dm)
//
//        /**
//         *
//         *  FIXME: Lite 버전에선 다이브로이드 하우징만 사용한다고 생각되기 때문에 아래의 부분을 주석 처리
//         *
//         */
////            when (getInstance().getConfig().housingMode) {
////                Housing.UNIVERSAL -> layer.controlView = UniversalTouchReceiver(layer.context)
////                Housing.XPOOVV -> layer.controlView = XpoovvTouchReceiver(layer.context)
////                Housing.MPAC -> layer.controlView = MpacTouchReceiver(layer.context)
////                Housing.PATIMA -> layer.controlView = PatimaTouchReceiver(layer.context)
////                else -> layer.controlView = MpacTouchReceiver(layer.context)
////            }
//
//        controlView = UniversalTouchReceiver(context).apply {
//            initType(
//                isPortrait,
//                (p.x / dm.xdpi).toDouble().pow(2.0).toFloat(),
//                (p.y / dm.ydpi).toDouble().pow(2.0).toFloat()
//            )
//        }
//        controlView?.let {
//            Log.d(TAG, "initPortrait: addView")
//            controlView?.setBackgroundColor(Color.BLUE)
//            addView(it, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
//            it.invalidate()
//        }
//    }

    companion object {
        @BindingAdapter("app:isPortrait")
        @JvmStatic
        fun initPortrait(layer: ClickViewLayer, isPortrait: Boolean?) {
            val p = Point()
            val wm = ContextProvider.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealSize(p)
            val dm = DisplayMetrics()
            wm.defaultDisplay.getMetrics(dm)
            /**
             *  FIXME: Lite 버전에선 다이브로이드 하우징만 사용한다고 생각되기 때문에 아래의 부분을 주석 처리
             */
            layer.controlView = UniversalTouchReceiver(layer.context)
//            when (getInstance().getConfig().housingMode) {
//                Housing.UNIVERSAL -> layer.controlView = UniversalTouchReceiver(layer.context)
//                Housing.XPOOVV -> layer.controlView = XpoovvTouchReceiver(layer.context)
//                Housing.MPAC -> layer.controlView = MpacTouchReceiver(layer.context)
//                Housing.PATIMA -> layer.controlView = PatimaTouchReceiver(layer.context)
//                else -> layer.controlView = MpacTouchReceiver(layer.context)
//            }
            layer.controlView?.let {
                it.initType(isPortrait!!, Math.pow((p.x / dm.xdpi).toDouble(), 2.0).toFloat(), Math.pow((p.y / dm.ydpi).toDouble(), 2.0).toFloat())
            }
            layer.addView(layer.controlView, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }
}