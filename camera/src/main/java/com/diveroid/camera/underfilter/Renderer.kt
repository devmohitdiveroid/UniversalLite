package com.diveroid.camera.underfilter

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.util.Log
import android.view.View
import com.diveroid.camera.underfilter.egl14.IRendererEGL14
import java.util.*
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class Renderer constructor(var runFilter: Boolean, var targetView: View?): IRendererEGL14 {
    private val TAG = this::class.java.simpleName
    private var currentID = 0

    var bRunThread = false
    var filterThread: Thread? = null

    var mEglDisplay10: EGLDisplay? = null
    var mEglDisplay: EGLDisplay? = null
    var mEglContext: EGLContext? = null
    var configurationsList: Array<EGLConfig?>? = null
    var mEGLSurface: EGLSurface? = null

    var mEGLDisplay14 = EGL14.EGL_NO_DISPLAY
    var mEGLContext14 = EGL14.EGL_NO_CONTEXT
    var TestEncode = false

    override fun onSurfaceCreated() {}

    override fun onSurfaceChanged(width: Int, height: Int) {
        mEGLDisplay14 = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        mEGLContext14 = EGL14.eglGetCurrentContext()
        val mEgl = EGLContext.getEGL() as EGL10

        mEglDisplay10 = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
        mEglDisplay = mEgl.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        mEglContext = mEgl.eglGetCurrentContext()

        val totalConfigurations = IntArray(1)
        mEgl.eglGetConfigs(mEglDisplay, null, 0, totalConfigurations)
        configurationsList = arrayOfNulls(totalConfigurations[0])
        mEgl.eglGetConfigs(mEglDisplay, configurationsList, totalConfigurations[0], totalConfigurations)
        mEGLSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ)

        GL2JNILib.init(width, height)
        if(runFilter) startFilterThread()
    }

    override fun onDrawFrame() {
        GL2JNILib.onDrawFrame(
            mEGLDisplay14, mEGLContext14, mEGLSurface
        )
    }

    override fun onDestroy() {
        bRunThread = false
        filterThread = null
        GL2JNILib.clearViewMode()
    }

    fun filterOn() {
        runFilter = true
        startFilterThread()
    }

    fun filterOff() {
        runFilter = false
        bRunThread = false
    }

    fun isFilterOn(): Boolean {
        return runFilter
    }


    private val timer = Timer()

    private fun startFilterThread() {
        ++currentID
        bRunThread = true

        val task = object: TimerTask() {
            private var isEnd = false
            private val threadID = currentID

            override fun run() {
                if(isEnd) return

                if(bRunThread && currentID == threadID && targetView!!.parent != null) {
                    Log.d(TAG, "run: onThread ID = $threadID")
                    GL2JNILib.runFilterProcess()
                    return
                }

                isEnd = true
                GL2JNILib.stopFilterProcess()
                cancel()
            }
        }

        timer.schedule(task, 0, 100)
    }

    companion object {

    }

}