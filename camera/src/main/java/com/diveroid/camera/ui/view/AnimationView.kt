package com.diveroid.camera.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * Created by dudbs on 2017-02-24.
 */
class AnimationView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    var w = 0
    var h = 0
    val relativeWidth = 737f
    val relativeHeight = 414f
    private val paint: Paint = Paint()
    protected fun getRelativeValX(position: Float): Float {
        val relativeRatio = relativeWidth / relativeHeight
        return (if (w > h * relativeRatio) h * relativeRatio * (position / relativeWidth) else w * (position / relativeWidth))
    }

    protected fun getRelativeValY(position: Float): Float {
        val relativeRatio = relativeHeight / relativeWidth
        return (if (h > w * relativeRatio) w * relativeRatio * (position / relativeHeight) else h * (position / relativeHeight))
    }

    private var outerRadius = 0f
    private var innerRadius = 0f
    fun setOuterRadius(outerRadius: Float) {
        this.outerRadius = outerRadius
    }

    fun setInnerRadius(innerRadius: Float) {
        this.innerRadius = innerRadius
    }

    override fun onDraw(canvas: Canvas) {
        drawFocus(canvas)
        drawTwinkleEffect(canvas)
    }

    private var backgroundAlpha = 0
    fun setBackgroundAlpha(alpha: Int) {
        backgroundAlpha = alpha
    }

    protected fun drawTwinkleEffect(canvas: Canvas) {
        if (backgroundAlpha <= 0) return
        paint.color = Color.BLACK
        paint.alpha = backgroundAlpha
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
    }

    protected fun drawFocus(canvas: Canvas) {
        if (innerRadius == 0f && outerRadius == 0f) return
        paint.alpha = 100
        paint.color = Color.YELLOW
        paint.strokeWidth = getRelativeValY(1f)
        paint.style = Paint.Style.STROKE
        val px = w / 2.0f
        val py = h / 2.0f
//        if (GlobalObjects.getSharedDivingSession() != null) {
//            val p = GlobalObjects.getSharedDivingSession().focusPoint
//            if (p.x != 0f && p.y != 0f) {
//                px = p.x
//                py = p.y
//            }
//        }
        canvas.drawCircle(px, py, getRelativeValY(outerRadius), paint)
        paint.alpha = 50
        paint.style = Paint.Style.FILL
        canvas.drawCircle(px, py, getRelativeValY(innerRadius), paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        super.onSizeChanged(w, h, oldw, oldh)
    }

}