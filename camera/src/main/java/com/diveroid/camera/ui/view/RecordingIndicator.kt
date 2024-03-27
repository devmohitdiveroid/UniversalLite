package com.diveroid.camera.ui.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.diveroid.camera.R
import java.util.*

class RecordingIndicator: LinearLayoutCompat {
    private val imgRedDat = ImageView(context).apply {
        setImageResource(R.drawable.ic_record_dot)
    }
    private val txtRec = TextView(context).apply {
        setTextColor(Color.WHITE)
        text = "REC"
        typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(R.font.pppangramsans_medium)
        } else {
            ResourcesCompat.getFont(context, R.font.pppangramsans_medium)
        }
    }
    private val txtRecTime = TextView(context).apply {
        setTextColor(Color.WHITE)
        text = "00:00:00"
        typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            resources.getFont(R.font.pppangramsans_medium)
        } else {
            ResourcesCompat.getFont(context, R.font.pppangramsans_medium)
        }
    }

    private var recordStartTime: Long = -1
    private val recordTimer = Timer()
    private var recordTimerTask: TimerTask? = null

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { init() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        addView(imgRedDat)
        addView(txtRec).run {
            val tLayoutParams = (txtRec.layoutParams as LayoutParams).apply {
                marginStart = 24
                marginEnd = 24
            }
            txtRec.layoutParams = tLayoutParams
        }
        addView(txtRecTime)


    }

    fun startRecord() {
        imgRedDat.startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_rec_blink))
        recordStartTime = System.currentTimeMillis()

        recordTimerTask = object: TimerTask() {
            override fun run() {
                if(recordStartTime < 0) return
                val diffSec = (System.currentTimeMillis() - recordStartTime) / 1000 // sec
                val hour = diffSec / 60 / 60
                val minute = diffSec / 60
                val second = diffSec % 60
                post {
                    txtRecTime.text = String.format("%02d:%02d:%02d", hour.toInt(), minute.toInt(), second.toInt())
                }
            }
        }

        recordTimer.schedule(recordTimerTask, 0, 500)
        visibility = View.VISIBLE
    }

    fun stopRecord() {
        imgRedDat.clearAnimation()
        recordStartTime = 0
        recordTimerTask?.cancel()
        recordTimerTask = null
        visibility = View.GONE
    }

    init {
        orientation = HORIZONTAL
        setPadding(8)
        gravity = Gravity.CENTER_VERTICAL
        visibility = View.GONE
    }
}