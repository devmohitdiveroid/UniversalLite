/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diveroid.camera.underfilter

import android.app.Activity
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.opengl.EGL14
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import com.diveroid.camera.GL2PreviewOption
import com.diveroid.camera.GL2PreviewOptionLite
import com.diveroid.camera.mediaCtrl.MediaFileControl
import com.diveroid.camera.underfilter.GL2JNILib.stopRealtimeView
import com.diveroid.camera.underfilter.egl14.GLSurfaceViewEGL14
import com.diveroid.camera.utils.BitmapUtils
import com.diveroid.camera.utils.Util
import com.google.gson.Gson
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File
import javax.microedition.khronos.egl.EGL10

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * A simple GLSurfaceView sub-class that demonstrate how to perform
 * OpenGL ES 2.0 rendering into a GL Surface. Note the following important
 * details:
 *
 * - The class must use a custom context factory to enable 2.0 rendering.
 * See ContextFactory class definition below.
 *
 * - The class must use a custom EGLConfigChooser to be able to select
 * an EGLConfig that supports 2.0. This is done by providing a config
 * specification to eglChooseConfig() that has the attribute
 * EGL10.ELG_RENDERABLE_TYPE containing the EGL_OPENGL_ES2_BIT flag
 * set. See ConfigChooser class definition below.
 *
 * - The class must select the surface's format, then choose an EGLConfig
 * that matches it exactly (with regards to red/green/blue/alpha channels
 * bit depths). Failure to do so would result in an EGL_BAD_MATCH error.
 */
class GL2JNIVideoView(val activity: Activity) : GLSurfaceViewEGL14(activity.applicationContext) {
    fun onFilterThread() {
        GL2JNILib.setFilterOn(GL2JNILib.FILTER_ON)
        (mRenderer as Renderer).filterOn()
    }

    fun offFilterThread() {
        GL2JNILib.setFilterOn(GL2JNILib.FILTER_OFF)
        (mRenderer as Renderer).filterOff()
    }

    val isOnFilterThread: Boolean
        get() = (mRenderer as Renderer).isFilterOn()

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause:")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }


    private fun saveVideoThumbNail(savedFile: String?) {
        if (savedFile.isNullOrEmpty()) return

        val file = File(savedFile)
        val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ThumbnailUtils.createVideoThumbnail(file, Size(512, 368), null)
        } else {
            ThumbnailUtils.createVideoThumbnail(savedFile, MediaStore.Video.Thumbnails.MINI_KIND)
        }

        if (thumbnail != null) {
            val resizedBitmap =
                BitmapUtils.getResizedBitmap(thumbnail, MediaFileControl.SIZE_THUMBNAIL_X)
            BitmapUtils.saveBitmap(
                MediaFileControl.getLocalMediaPath(file.name, true)!!,
                resizedBitmap
            )
            thumbnail.recycle()
            resizedBitmap.recycle()
        }
    }

    fun setGL2JNILibCallback(callback: GL2JNILib.Callback) {
        GL2JNILib.mCallback = callback
    }


    companion object {
        private val TAG = "GL2JNIView"


        const val CAMERA_ANGLE_WIDE = 0
//        @Deprecated("Use CAMERA_ANGLE_WIDE")
        const val CAMERA_ANGLE_ULTRA_WIDE = 1
        const val CAMERA_ANGLE_ZOOM = 2
        const val CAMERA_ANGLE_SELFIE = 3

        //  private void init(boolean translucent, int depth, int stencil) {
        /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
         * If we want a translucent one, we should change the surface's
         * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
         * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
         */
        //     if (translucent) {
        //        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //     }
        /* Setup the context factory for 2.0 rendering.
         * See ContextFactory class definition below
         */
        //  setEGLContextFactory(new ContextFactory());
        /* We need to choose an EGLConfig that matches the format of
         * our surface exactly. This is going to be done in our
         * custom config chooser. See ConfigChooser class definition
         * below.
         */
        //  setEGLConfigChooser( translucent ?
        //                      new ConfigChooser(8, 8, 8, 8, depth, stencil) :
        //                         new ConfigChooser(5, 6, 5, 0, depth, stencil) );
        /* Set the renderer responsible for frame rendering */ //  setRenderer(new Renderer());
        //   }
        /*
    private static class ContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.w(TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

*/
        private fun checkEglError(prompt: String, egl: EGL10) {
            var error: Int
            while (egl.eglGetError().also { error = it } != EGL10.EGL_SUCCESS) {
                Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error))
            }
        }

        var TestEncode = false
    }

    init {
        //init(true, 16, 8);
        GL2JNILib.mContext = activity
        GL2JNILib.onCreate(activity.assets, GL2PreviewOption.IS_DEFAULT_FILTER_OPTION)

        mRenderer = Renderer(false, this)
        setRenderer(mRenderer)
    }
}