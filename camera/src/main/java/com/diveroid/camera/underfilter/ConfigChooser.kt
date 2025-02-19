package com.diveroid.camera.underfilter

import android.opengl.GLSurfaceView
import android.util.Log
import com.diveroid.camera.BuildConfig
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

open class ConfigChooser(
    protected var mRedSize: Int,
    protected var mGreenSize: Int,
    protected var mBlueSize: Int,
    protected var mAlphaSize: Int,
    protected var mDepthSize: Int,
    protected var mStencilSize: Int
): GLSurfaceView.EGLConfigChooser {
    private val TAG = this::class.java.simpleName
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        /* Get the number of minimally matching EGL configurations
             */
        val numConfig = IntArray(1)
        egl.eglChooseConfig(display, s_configAttribs2, null, 0, numConfig)
        val numConfigs = numConfig[0]
        require(numConfigs > 0) { "No configs match configSpec" }

        /* Allocate then read the array of minimally matching EGL configs
         */
        val configs = arrayOfNulls<EGLConfig>(numConfigs)
        egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, numConfig)
        if (BuildConfig.DEBUG) {
            printConfigs(egl, display, configs)
        }
        /* Now return the "best" one
         */return chooseConfig(egl, display, configs)!!
    }

    fun chooseConfig(egl: EGL10, display: EGLDisplay,
                     configs: Array<EGLConfig?>): EGLConfig? {
        for (config in configs) {
            val d = findConfigAttrib(egl, display, config,
                EGL10.EGL_DEPTH_SIZE, 0)
            val s = findConfigAttrib(egl, display, config,
                EGL10.EGL_STENCIL_SIZE, 0)

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize) continue

            // We want an *exact* match for red/green/blue/alpha
            val r = findConfigAttrib(egl, display, config,
                EGL10.EGL_RED_SIZE, 0)
            val g = findConfigAttrib(egl, display, config,
                EGL10.EGL_GREEN_SIZE, 0)
            val b = findConfigAttrib(egl, display, config,
                EGL10.EGL_BLUE_SIZE, 0)
            val a = findConfigAttrib(egl, display, config,
                EGL10.EGL_ALPHA_SIZE, 0)
            if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize) return config
        }
        return null
    }

    private fun findConfigAttrib(egl: EGL10, display: EGLDisplay,
                                 config: EGLConfig?, attribute: Int, defaultValue: Int): Int {
        return if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            mValue[0]
        } else defaultValue
    }

    private fun printConfigs(egl: EGL10, display: EGLDisplay,
                             configs: Array<EGLConfig?>) {
        val numConfigs = configs.size
        Log.w(TAG, String.format("%d configurations", numConfigs))
        for (i in 0 until numConfigs) {
            Log.w(TAG, String.format("Configuration %d:\n", i))
            printConfig(egl, display, configs[i])
        }
    }

    private fun printConfig(egl: EGL10, display: EGLDisplay,
                            config: EGLConfig?) {
        val attributes = intArrayOf(
            EGL10.EGL_BUFFER_SIZE,
            EGL10.EGL_ALPHA_SIZE,
            EGL10.EGL_BLUE_SIZE,
            EGL10.EGL_GREEN_SIZE,
            EGL10.EGL_RED_SIZE,
            EGL10.EGL_DEPTH_SIZE,
            EGL10.EGL_STENCIL_SIZE,
            EGL10.EGL_CONFIG_CAVEAT,
            EGL10.EGL_CONFIG_ID,
            EGL10.EGL_LEVEL,
            EGL10.EGL_MAX_PBUFFER_HEIGHT,
            EGL10.EGL_MAX_PBUFFER_PIXELS,
            EGL10.EGL_MAX_PBUFFER_WIDTH,
            EGL10.EGL_NATIVE_RENDERABLE,
            EGL10.EGL_NATIVE_VISUAL_ID,
            EGL10.EGL_NATIVE_VISUAL_TYPE,
            0x3030,  // EGL10.EGL_PRESERVED_RESOURCES,
            EGL10.EGL_SAMPLES,
            EGL10.EGL_SAMPLE_BUFFERS,
            EGL10.EGL_SURFACE_TYPE,
            EGL10.EGL_TRANSPARENT_TYPE,
            EGL10.EGL_TRANSPARENT_RED_VALUE,
            EGL10.EGL_TRANSPARENT_GREEN_VALUE,
            EGL10.EGL_TRANSPARENT_BLUE_VALUE,
            0x3039,  // EGL10.EGL_BIND_TO_TEXTURE_RGB,
            0x303A,  // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
            0x303B,  // EGL10.EGL_MIN_SWAP_INTERVAL,
            0x303C,  // EGL10.EGL_MAX_SWAP_INTERVAL,
            EGL10.EGL_LUMINANCE_SIZE,
            EGL10.EGL_ALPHA_MASK_SIZE,
            EGL10.EGL_COLOR_BUFFER_TYPE,
            EGL10.EGL_RENDERABLE_TYPE,
            0x3042 // EGL10.EGL_CONFORMANT
        )
        val names = arrayOf(
            "EGL_BUFFER_SIZE",
            "EGL_ALPHA_SIZE",
            "EGL_BLUE_SIZE",
            "EGL_GREEN_SIZE",
            "EGL_RED_SIZE",
            "EGL_DEPTH_SIZE",
            "EGL_STENCIL_SIZE",
            "EGL_CONFIG_CAVEAT",
            "EGL_CONFIG_ID",
            "EGL_LEVEL",
            "EGL_MAX_PBUFFER_HEIGHT",
            "EGL_MAX_PBUFFER_PIXELS",
            "EGL_MAX_PBUFFER_WIDTH",
            "EGL_NATIVE_RENDERABLE",
            "EGL_NATIVE_VISUAL_ID",
            "EGL_NATIVE_VISUAL_TYPE",
            "EGL_PRESERVED_RESOURCES",
            "EGL_SAMPLES",
            "EGL_SAMPLE_BUFFERS",
            "EGL_SURFACE_TYPE",
            "EGL_TRANSPARENT_TYPE",
            "EGL_TRANSPARENT_RED_VALUE",
            "EGL_TRANSPARENT_GREEN_VALUE",
            "EGL_TRANSPARENT_BLUE_VALUE",
            "EGL_BIND_TO_TEXTURE_RGB",
            "EGL_BIND_TO_TEXTURE_RGBA",
            "EGL_MIN_SWAP_INTERVAL",
            "EGL_MAX_SWAP_INTERVAL",
            "EGL_LUMINANCE_SIZE",
            "EGL_ALPHA_MASK_SIZE",
            "EGL_COLOR_BUFFER_TYPE",
            "EGL_RENDERABLE_TYPE",
            "EGL_CONFORMANT"
        )
        val value = IntArray(1)
        for (i in attributes.indices) {
            val attribute = attributes[i]
            val name = names[i]
            if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                Log.w(TAG, String.format("  %s: %d\n", name, value[0]))
            } else {
                // Log.w(TAG, String.format("  %s: failed\n", name));
                while (egl.eglGetError() != EGL10.EGL_SUCCESS);
            }
        }
    }

    private val mValue = IntArray(1)

    companion object {
        /* This EGL config specification is used to specify 2.0 rendering.
     * We use a minimum size of 4 bits for red/green/blue, but will
     * perform actual matching in chooseConfig() below.
     */
        private const val EGL_OPENGL_ES2_BIT = 4
        private val s_configAttribs2 = intArrayOf(
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
            EGL10.EGL_SAMPLES, 4,
            EGL10.EGL_NONE
        )
    }
}