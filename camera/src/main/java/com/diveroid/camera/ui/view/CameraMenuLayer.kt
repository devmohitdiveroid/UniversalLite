package com.diveroid.camera.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import com.diveroid.camera.GL2PreviewOptionLite
import com.diveroid.camera.R
import com.diveroid.camera.databinding.LayoutCameraModeMenuBinding

/**
 * TODO: document your custom view class.
 */
class CameraMenuLayer : LinearLayoutCompat {
    private val TAG = CameraMenuLayer::class.java.simpleName

    private val visibilityListener = ViewTreeObserver.OnGlobalLayoutListener {
        if(visibility == View.VISIBLE) {
            postDelayed(hiddenRunnable, HIDDEN_DELAY_TIME)
        } else {
            removeCallbacks(hiddenRunnable)
        }
    }

    private val hiddenRunnable = Runnable {
        visibility = View.GONE
    }

    val isVisible: Boolean
        get() = visibility == View.VISIBLE

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private var menuCount = 0

    fun initMenu(option: GL2PreviewOptionLite?) {
        if(option == null) return

        removeAllViews()
        menuCount = option.enableButtons.filter {
            it
        }.size

        option.enableButtons.forEachIndexed { index: Int, value: Boolean ->
            if(value) {
                val item = itemMenu().apply {
                    tag = index
                }
                val icon = item.findViewById<ImageView>(R.id.icon)
                val title = item.findViewById<TextView>(R.id.title)

                when(index) {
                    option.OPTION_WIDE -> {
                        icon.setImageResource(R.drawable.ic_camera_mode_wide_large)
                        title.text = resources.getString(R.string.camera_menu_wide)
                    }
                    option.OPTION_ULTRA_WIDE -> {
                        icon.setImageResource(R.drawable.ic_camera_mode_ultra_wide_large)
                        title.text = resources.getString(R.string.camera_menu_ultra_wide)
                    }
                    option.OPTION_ZOOM -> {
                        icon.setImageResource(R.drawable.ic_camera_mode_zoom_large)
                        title.text = resources.getString(R.string.camera_menu_zoom)
                    }
                    option.OPTION_SELFIE-> {
                        icon.setImageResource(R.drawable.ic_camera_mode_selfie_large)
                        title.text = resources.getString(R.string.camera_menu_selfie)
                    }
                }
                addView(item)
            }
        }
    }

    private fun itemMenu(): View {
        val binding = LayoutCameraModeMenuBinding.inflate(LayoutInflater.from(context), this, false)
        (binding.root.layoutParams as LayoutParams).apply {
            weight = 1.0f
            marginEnd = 2
            marginStart = 2
        }
        return binding.root
    }

    fun selectMenu(index: Int) {
        children.forEach {
            it.isSelected = it.tag == index
        }

        for(i in GL2PreviewOptionLite.selectButtons.indices) {
            GL2PreviewOptionLite.selectButtons[i] = i == index
        }
    }

    fun hideLayer() {
        if(!isVisible) return
        visibility = View.GONE
    }

    fun showLayer() {
        if(isVisible) return
        visibility = View.VISIBLE
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
    }

    companion object {
        private const val HIDDEN_DELAY_TIME = 1500L
    }

    interface OnCameraMenuLayerListener {
        fun getEnabledButtons(): BooleanArray
    }
}