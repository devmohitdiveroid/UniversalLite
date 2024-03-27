package com.diveroid.lite.util

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

object AnimationUtil {
    fun getTranslationX(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "translationX", *values)
            ret!!.interpolator = DecelerateInterpolator()
            ret.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getTranslationY(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "translationY", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getRotation(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "rotation", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getRotationX(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "rotationX", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getRotationY(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "rotationY", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getScaleX(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "scaleX", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getScaleY(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "scaleY", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getPivotX(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "pivotX", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getPivotY(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "pivotY", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getAlpha(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "alpha", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getMoveX(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "x", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }

    fun getMoveY(v: View, duration: Long, vararg values: Float): ObjectAnimator? {
        var ret: ObjectAnimator? = null
        try {
            ret = ObjectAnimator.ofFloat(v, "y", *values)
            ret!!.duration = duration
        } catch (e: Exception) {
            ret = null
        }

        return ret
    }
}
