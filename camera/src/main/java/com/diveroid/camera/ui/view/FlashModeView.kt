package com.diveroid.camera.ui.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import com.diveroid.camera.R
import com.diveroid.camera.underfilter.GL2JNIView

class FlashModeView: LinearLayoutCompat {
    val icon: ImageView = ImageView(context)
    val title: TextView = TextView(context).apply {
        setTextColor(Color.WHITE)
        typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(R.font.pppangramsans_medium)
        } else {
            ResourcesCompat.getFont(context, R.font.pppangramsans_medium)
        }
    }

    constructor(context: Context) : super(context) {
        init(null)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int? = 0) {
        addView(icon)
        addView(title).run {
            (title.layoutParams as LayoutParams).apply {
                weight = 1f
                marginStart = 16
            }
        }
        setBackgroundResource(R.drawable.bg_mode)
        setMode(false)
    }

    /**
     *  @param mode
     */
    fun setMode(mode: Boolean) {
        if(mode) {
            setTitle(context.getString(R.string.on))
            setIcon(R.drawable.ic_flash_mode_on)
        } else {
            setTitle(context.getString(R.string.off))
            setIcon(R.drawable.ic_flash_mode_off)
        }
    }

    private fun setTitle(text: String) {
        this.title.text = text
    }

    private fun setIcon(resId: Int) {
        this.icon.setImageResource(resId)
    }

    init {
        orientation = HORIZONTAL
        setPadding(18, 18,18, 18)
        gravity = Gravity.CENTER_VERTICAL
    }
}