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
import android.graphics.Color
import android.media.ThumbnailUtils
import android.opengl.EGL14
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
import com.diveroid.camera.utils.*
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
class GL2JNIView(val activity: Activity, var option: GL2PreviewOptionLite? = null) :
    GLSurfaceViewEGL14(activity.applicationContext) {

    init {
        setBackgroundColor(Color.DKGRAY)
    }

    fun onFilterThread() {
        GL2JNILib.setFilterOn(GL2JNILib.FILTER_ON)
        (mRenderer as Renderer).filterOn()
        GL2JNILib.startAnimation()
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
        GL2JNICamera2.stopBackgroundThread()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        GL2JNICamera2.startBackgroundThread()
    }


    /**
     *  카메라 설정 관련
     */

    /**
     *  수중모드 관련
     */

    val isUnderwaterModeOn: Boolean
        get() = GL2JNICamera2.isWaterMode

    fun underwaterModeOn() {
        if(isUnderwaterModeOn) return
        GL2JNICamera2.underwaterModeOn()
    }

    fun underwaterModeOff() {
        if(!isUnderwaterModeOn) return
        GL2JNICamera2.underwaterModeOff()
    }


    /**
     *  플래시 On/Off 플래그
     */
    val isFlashOn: Boolean
        get() = GL2JNICamera2.isFlash

    /**
     *  플래시 On
     */
    fun flashOn() {
        if(isFlashOn) return
        GL2JNICamera2.flashOnOff(true)
    }

    fun flashOff() {
        if(!isFlashOn) return
        GL2JNICamera2.flashOnOff(false)
    }

    fun isOpenCamera(): Boolean = GL2JNICamera2.isCameraStarted

    /**
     * 카메라 표시 On
     */
    fun startCamera() {
        setBackgroundColor(Color.TRANSPARENT)

        computeCameraSize()

        val cameraWidth = GL2JNICamera2.mSurfaceSize!!.width
        val cameraHeight = GL2JNICamera2.mSurfaceSize!!.height

        val previewWidth = measuredWidth.toFloat()
        val previewHeight = measuredHeight.toFloat()

        // 카메라값 비율
        val cameraRateWidth = cameraWidth / previewWidth
        val cameraRateHeight = cameraHeight / previewHeight
        val cameraRate =
            if (cameraRateHeight > cameraRateWidth) cameraRateWidth else cameraRateHeight
        Log.d(
            "csson",
            "cameraRateWidth = $cameraRateWidth, " +
                    "cameraRateHeight = $cameraRateHeight, " +
                    "cameraRate = $cameraRate"
        )

        // 보여지는 카메라 뷰 영역
        var correctionWidth = cameraRate * previewWidth
        val correctionHeight = cameraRate * previewHeight

        when (GL2PreviewOptionLite.SCREEN_RATIO) {
            Util.CameraRatio.RATIO_16_9 -> {
                correctionWidth = correctionHeight * (16f / 9)
            }
            Util.CameraRatio.RATIO_4_3 -> {
                correctionWidth = correctionHeight * (4f / 3)
            }
            else -> {

            }
        }

        Log.d(
            "csson",
            "resolution: W = $cameraWidth, H = $cameraHeight, newW = $correctionWidth, newH = $correctionHeight"
        )

        GL2JNILib.startRealtimeView(
            cameraWidth,
            cameraHeight,
            correctionWidth.toInt(),
            correctionHeight.toInt(),
            option!!.PREPARE_FRAME
        )
    }

    /**
     * 카메라 표시 Off
     */
    fun stopCamera() {
        GL2JNICamera2.stopCamera()
        stopRealtimeView()

        setBackgroundColor(Color.DKGRAY)
    }


    /**
     *  카메라 캡쳐
     */
    fun captureImage() {
        if (GL2JNICamera2.mState == GL2JNICamera2.STATE_PREVIEW) {
            GL2JNILib.saveImage()
        }
    }

    /**
     * 동영상 관련 변수
     */

    val recordSize: Size
        get() {
            val baseSize = when(GL2PreviewOptionLite.getCameraConfig()?.highResolution) {
                Util.CameraResolution.UHD_4K -> SIZE_4K
                else -> SIZE_1080P
            }
            val baseW: Float = baseSize.size.height.coerceAtMost(GL2JNICamera2.mSurfaceSize!!.height).toFloat()
            val baseH: Float = baseSize.size.width.coerceAtMost(GL2JNICamera2.mSurfaceSize!!.width).toFloat()

            var newW: Int
            var newH: Int
            val w = measuredWidth.toFloat()
            val h = measuredHeight.toFloat()

            run {
                //float rate = 1080.0f / h;
                val rateH = baseW / h
                val rateW = baseH / w
                var rate = rateH
                if (rateH > rateW) {
                    rate = rateW
                }
                newW = (rate * w).toInt()
                newH = (rate * h).toInt()
                if (newW % 2 == 1) {
                    newW--
                }
                if (newH % 2 == 1) {
                    newH--
                }
            }

            return Size(newW, newH)
        }

    private var bRecordingVideo: Boolean = false
    private var nowRecording: Boolean = false
    private var recordingStartTime = 0L

    val isRecording: Boolean
        get() = bRecordingVideo

    fun startRecord() {
        if (bRecordingVideo) return

        activity.runOnUiThread {
            if (!bRecordingVideo) {  // 동영상 촬영 시작
                nowRecording = true
                if (AudioEncoderSearcher.muxableAudioFormat != null) {
                    bRecordingVideo = true
                    recordingStartTime = System.currentTimeMillis()

                    val videoFileName = MediaFileControl.createVideoFileName()
                    GL2JNILib.saveMovie(
                        MediaFileControl.getLocalMediaPath(videoFileName, false),
                        AudioEncoderSearcher.muxableAudioFormat,
                        recordSize.width,
                        recordSize.height,
                        mExpectFrame
                    )
                } else {
                    AudioEncoderSearcher.runSearch(activity)
                    Toast.makeText(activity, "초기화 중...", Toast.LENGTH_SHORT)
                        .show()
                }
            } else { // 동영상 촬영 중지
                Toast.makeText(
                    activity,
                    "이미 녹화 중입니다.\n녹화를 중지하시려면 가운데 버튼을 길게 눌러주세요.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    fun stopRecord() {
        GL2JNILib.saveMovieStop()
        nowRecording = false
        bRecordingVideo = false
    }

    var mExpectSizeWidth = 3840
    var mExpectSizeHeight = 2160
    var mExpectFrame = 30

    private var cameraMode: Int = CAMERA_ANGLE_ULTRA_WIDE//leess 20221014 초광각-광각 순서변경

    /**
     *  카메라 모드 변경 메서드
     *  @param mode
     *  @see GL2PreviewOption.OPTION_NORMAL
     *  @see GL2PreviewOption.OP
     */

    fun changeCameraMode(mode: Int) {
        if(option!!.OPTION_MAX_RANGE <= mode) return

        // 기존 모드와 동일한 경우 아무런 동작하지 않음.
        if(cameraMode == mode) return

        if(option!!.enableButtons[mode]) {
            cameraMode = mode
            stopCamera()
            startCamera()
        }
    }


    private fun computeCameraSize() {
        // TODO: 배터리가 부족할 경우에 대한 처리가 필요.
        setToOptionResolution()

        when (cameraMode) {
            CAMERA_ANGLE_ULTRA_WIDE -> {
                Log.d("csson", "computeCameraSize: ultra wide")
                GL2JNICamera2.prepareCamera(
                    context,
                    mExpectSizeWidth,
                    mExpectSizeHeight,
                    false,
                    1.0f,
                    true
                )
            }
            CAMERA_ANGLE_ZOOM -> {
                Log.d("csson", "computeCameraSize: zoom")
                GL2JNICamera2.prepareCamera(
                    context,
                    mExpectSizeWidth,
                    mExpectSizeHeight,
                    false,
                    2f,
                    false
                )
            }

            CAMERA_ANGLE_SELFIE -> {
                Log.d("csson", "computeCameraSize: selfie")
                GL2JNICamera2.prepareCamera(
                    activity,
                    mExpectSizeWidth,
                    mExpectSizeHeight,
                    true,
                    1f,
                    GL2JNICamera2.hasFrontspaceWideCamera(activity)
                )
            }
            else -> {
                Log.d("csson", "computeCameraSize: normal")
                GL2JNICamera2.prepareCamera(
                    context,
                    mExpectSizeWidth,
                    mExpectSizeHeight,
                    false,
                    1f,
                    false
                )
            }
        }
    }

    private fun setToOptionResolution() {
        GL2PreviewOptionLite.run {
            when(SCREEN_RATIO) {
                Util.CameraRatio.RATIO_FULL -> {
                    mExpectSizeWidth = Util.getScreenWidth(context)
                    mExpectSizeHeight = Util.getScreenHeight(context)
                }
                else -> {
                    mExpectSizeWidth = this@GL2JNIView.measuredWidth
                    mExpectSizeHeight = this@GL2JNIView.measuredHeight
                }
            }
            mExpectFrame = PREPARE_FRAME
        }
    }

    private fun loadOpenCVLibrary() {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onManagerConnected: load library = OpenCVLoader")
            OpenCVLoader.initAsync(
                OpenCVLoader.OPENCV_VERSION,
                context,
                mLoaderCallback
            )
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            super.onManagerConnected(status)
            if (status == SUCCESS) {
                Log.d(TAG, "onManagerConnected: load library = ImageFilter")
                System.loadLibrary("ImageFilter")
            } else {
                super.onManagerConnected(status)
            }
        }
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


        const val CAMERA_ANGLE_WIDE = 1
//        @Deprecated("Use CAMERA_ANGLE_WIDE")
        const val CAMERA_ANGLE_ULTRA_WIDE = 0
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

        var mEGLDisplay14 = EGL14.EGL_NO_DISPLAY
        var mEGLContext14 = EGL14.EGL_NO_CONTEXT
        var TestEncode = false
    }

    init {
        loadOpenCVLibrary()

        GL2JNILib.mContext = activity
        GL2JNILib.onCreate(context.assets, GL2PreviewOption.IS_DEFAULT_FILTER_OPTION)
        MediaFileControl.init(activity)
        AudioEncoderSearcher.runSearch(activity)

        mRenderer = Renderer(GL2PreviewOption.IS_DEFAULT_FILTER_OPTION, this)
        setRenderer(mRenderer)
    }
}