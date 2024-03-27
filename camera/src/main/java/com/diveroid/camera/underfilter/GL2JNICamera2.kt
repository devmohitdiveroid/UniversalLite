package com.diveroid.camera.underfilter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Pair
import android.util.Range
import android.util.Size
import android.view.Surface
import androidx.core.content.ContextCompat
import com.diveroid.camera.GL2PreviewOptionLite
import com.diveroid.camera.mediaCtrl.ImageFilter
import com.diveroid.camera.mediaCtrl.MediaFileControl
import com.diveroid.camera.mediaCtrl.MediaFileControl.createImgFileName
import com.diveroid.camera.mediaCtrl.MediaFileControl.getLocalMediaPath
import com.diveroid.camera.utils.*
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@SuppressLint("StaticFieldLeak")
object GL2JNICamera2 {
    private val TAG = this::class.java.simpleName

    /**********************************************************************************************************
     * 카메라 캡처 를 하는 클래스 이다.
     *
     * 참조 소스 (캡쳐 관련)
     * https://medium.com/androiddevelopers/understanding-android-camera-capture-sessions-and-requests-4e54d9150295
     */
    var isCameraStarted = false

    const val focusDivider = 5f

    const val STATE_PREVIEW = 0
    const val STATE_WAITING_LOCK = 1
    const val STATE_WAITING_PRECAPTURE = 2
    const val STATE_WAITING_NON_PRECAPTURE = 3
    const val STATE_PICTURE_TAKEN = 4
    var mState = STATE_PREVIEW

    private val mCameraOpenCloseLock = Semaphore(1)

    private var mFrameSem: Semaphore? = null
    private val frameListener = OnFrameAvailableListener { }
    var mPreviewSize: Size? = null
    var mSurfaceSize: Size? = null

//    @Deprecated("use mSurfaceSize")
//    lateinit var mOutputSize: Size
    private var mContext: Context? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var mPreviewRequest: CaptureRequest? = null
    private var mImageReader: ImageReader? = null
    var isSaveDone = true
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        if (isSaveDone) {
            mBackgroundHandler!!.post(ImageSaver(reader.acquireLatestImage()))
        }
    }
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mFrame = 30

    var isFlash: Boolean = false
    var isWaterMode: Boolean = false

    private fun getTelephotoCameraId(manager: CameraManager): String {
        val backCameraIdList = manager.cameraIdList.filter {
            //leess 20221226 줌모드크래시수정 : output사이즈 있는지 체크하여 없을 경우 제외처리
            //manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            (manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) &&
                    (manager.getCameraCharacteristics(it).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(SurfaceTexture::class.java) != null ||
                    manager.getCameraCharacteristics(it).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(ImageReader::class.java) != null )
        }

//        backCameraIdList.forEach {
//            Log.d(
//                "csson",
//                "getTelephotoCameraId: camera id : $it , angle = ${computeAngle(manager, it)}"
//            )
//        }

        // 화각이 가장 작은 카메라
        val telephoteCameraId =  backCameraIdList.minByOrNull {
            computeAngle(manager, it)
        }!!

//        Log.d("csson", "getTelephotoCameraId: telephoteCameraId = $telephoteCameraId")
        return telephoteCameraId
    }

    private fun getBackFacingCameraId(cManager: CameraManager, isWide: Boolean): String? {
        try {
            val hashMap: MutableMap<String, Float> = HashMap()
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = map!!.getOutputSizes(SurfaceTexture::class.java)
                        ?: continue
                    val tmpAngle = computeAngle(cManager, cameraId)
                    hashMap[cameraId] = tmpAngle
                }
            }
            val keyset: Set<String> = hashMap.keys
            val list: MutableList<String?> = ArrayList<String?>()
            list.addAll(keyset)
            list.sortWith { o1, o2 ->
                val v1: Float = hashMap[o1]!!
                val v2: Float = hashMap[o2]!!
                v2.compareTo(v1)
            }

            Log.d("csson", "getBackFacingCameraId: ${Gson().toJson(list)}")
            val cameraId = if (isWide) { // 앵글 제일 큰 렌즈를 반환
                if (list.size == 0) null else list[0]
            } else { // 앵글 두번째 큰 렌즈를 반환
                when (list.size) {
                    0 -> null
                    1 -> list[0]
                    else -> list[1]
                }
            }
            return cameraId
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFrontFacingCameraId(cManager: CameraManager, isWide: Boolean): String? {
        try {
            val prevCameraId: String? = null
            val hashMap: MutableMap<String, Float> = HashMap()
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sizes = map!!.getOutputSizes(SurfaceTexture::class.java)
                        ?: continue
                    val tmpAngle = computeAngle(cManager, cameraId)
                    hashMap[cameraId] = tmpAngle
                }
            }
            val keyset: Set<String> = hashMap.keys
            val list: MutableList<String?> = ArrayList<String?>()
            list.addAll(keyset)
            list.sortWith { o1, o2 ->
                val v1: Float = hashMap[o1]!!
                val v2: Float = hashMap[o2]!!
                v2.compareTo(v1)
            }
            return if (isWide) { // 앵글 제일 큰 렌즈를 반환
                if (list.size == 0) null else list[0]
            } else { // 앵글 두번째 큰 렌즈를 반환
                when (list.size) {
                    0 -> null
                    1 -> list[0]
                    else -> list[1]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    var mSurface: Surface? = null
    var mSurfaceTexture: SurfaceTexture? = null
    fun stopCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (cameraCaptureSession != null) {
                cameraCaptureSession!!.close()
                cameraCaptureSession = null
            }
            if (mCameraDevice != null) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
            if (mImageReader != null) {
                mImageReader!!.close()
                mImageReader = null
            }

            //stopBackgroundThread();
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
            isCameraStarted = false
        }
    }

    private val AUDIO_SOURCES = intArrayOf(
        MediaRecorder.AudioSource.MIC,
        MediaRecorder.AudioSource.DEFAULT,
        MediaRecorder.AudioSource.CAMCORDER,
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        MediaRecorder.AudioSource.VOICE_RECOGNITION
    )

    // 화각 계산
    @Throws(CameraAccessException::class)
    fun computeAngle(cameraManager: CameraManager, cameraId2: String?): Float {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId2!!)
        val maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        var strMaxFocus = ""
        for (j in maxFocus!!.indices) {
            strMaxFocus += "(" + maxFocus[j] + ")"
        }
        val size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val w = size!!.width
        val h = size.height
        val horizonalAngle = (2 * Math.atan((w / (maxFocus[0] * 2)).toDouble())).toFloat()
        val verticalAngle = (2 * Math.atan((h / (maxFocus[0] * 2)).toDouble())).toFloat()
        return horizonalAngle
    }

    fun hasBackspaceWideCamera(context: Context): Boolean {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var findFist = false
            var firstAngle = 0f
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    if (!findFist) {
                        findFist = true
                        firstAngle = computeAngle(manager, cameraId)
                    } else {
                        val secondAngle = computeAngle(manager, cameraId)
                        if (secondAngle > firstAngle) {
                            return true
                        }
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    fun hasFrontspaceWideCamera(context: Context): Boolean {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var findFist = false
            var firstAngle = 0f
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId!!)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    if (!findFist) {
                        findFist = true
                        firstAngle = computeAngle(manager, cameraId)
                    } else {
                        val secondAngle = computeAngle(manager, cameraId)
                        if (secondAngle > firstAngle) {
                            return true
                        }
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return false
    }

    var mCameraID: String? = ""
    var mIsSelfie = false
    var mIsWide = false
    lateinit var mFpsRangesHighAll: Array<Range<Int>>
    var mFpsRangesHigh: Array<Range<Int>>? = null
    lateinit var mFpsRanges: Array<Range<Int>>
    var mISHigh = false
    fun getHighSpeedResolution(context: Context?): String {
        mContext = context
        var oneCameraResult = ""
        val cManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var index = 0
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId!!)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                ++index
                oneCameraResult += if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    "front $index\r\n"
                } else {
                    "back $index\r\n"
                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                mFpsRangesHighAll = map!!.highSpeedVideoFpsRanges
                for (integerRange in mFpsRangesHighAll) {
                    val low = integerRange.lower
                    val high = integerRange.upper
                    oneCameraResult += """
                        ${low}X$high
                        
                        """.trimIndent()
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return oneCameraResult
    }

    fun getAvailableHighSpeed(context: Context?, width: Int, height: Int): Array<Range<Int>>? {
        mContext = context
        var oneCameraResult = ""
        val cManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var index = 0
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId!!)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                ++index
                oneCameraResult += if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    "front $index\r\n"
                } else {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sz = Size(width, height)
                    return try {
                        map!!.getHighSpeedVideoFpsRangesFor(sz)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    fun getAvailableHighSpeedFront(context: Context?, width: Int, height: Int): Array<Range<Int>>? {
        mContext = context
        var oneCameraResult = ""
        val cManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var index = 0
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId!!)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                ++index
                oneCameraResult += if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    "front $index\r\n"
                } else {
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sz = Size(width, height)
                    return try {
                        map!!.getHighSpeedVideoFpsRangesFor(sz)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    fun getAvailableHighSpeedUniverse(
        context: Context?,
        isFront: Boolean,
        isWide: Int,
        width: Int,
        height: Int
    ): Array<Range<Int>>? {
        mContext = context
        var oneCameraResult = ""
        var targetOrientation = CameraCharacteristics.LENS_FACING_BACK
        targetOrientation = if (isFront) {
            CameraCharacteristics.LENS_FACING_FRONT
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }
        val list = ArrayList<Pair<Float?, Array<Range<Int>>?>>()
        val cManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var index = 0
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId!!)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
                ++index
                if (cOrientation == targetOrientation) {
                    val secondAngle = computeAngle(cManager, cameraId)
                    val map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sz = Size(width, height)
                    try {
                        list.add(
                            Pair<Float?, Array<Range<Int>>?>(
                                secondAngle,
                                map!!.getHighSpeedVideoFpsRangesFor(sz)
                            )
                        )
                    } catch (e: Exception) {
                        //     return null;
                    }
                } else {
                    oneCameraResult += "front $index\r\n"
                }
            }
            list.sortWith { o1, o2 ->
                o1.first!!.compareTo(o2.first!!)
            }
            return if (list.size > isWide) {
                list[isWide].second
            } else {
                null
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    //leess zoomLevel: Int -> Float
    //fun prepareCamera(context: Context, wantWidth: Int, wantHeight: Int, isSelfie: Boolean, zoomLevel: Int, isWide: Boolean) {
    fun prepareCamera(
        context: Context,
        wantWidth: Int,
        wantHeight: Int,
        isSelfie: Boolean,
        zoomLevel: Float,
        isWide: Boolean
    ) {
        mIsWide = isWide
        mZoomLevel = zoomLevel
        mIsSelfie = isSelfie
        mContext = context
        val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            mCameraID = if (mIsSelfie) {
                getFrontFacingCameraId(manager, mIsWide)
            } else {
                if(zoomLevel > 1f) {
                    getTelephotoCameraId(manager)
                } else {
                    getBackFacingCameraId(manager, mIsWide)
                }
            }

            Log.d(TAG, "prepareCamera: mCameraID = $mCameraID")
            val characteristics = manager.getCameraCharacteristics(mCameraID!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            mFpsRanges =
                characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)!!
            mFpsRangesHighAll = map!!.highSpeedVideoFpsRanges

            val sizess = map.getOutputSizes(SurfaceTexture::class.java) ?: map.getOutputSizes(ImageReader::class.java)
            if(sizess != null) {//leess 20221226 null체크
                Arrays.sort(sizess) { o1, o2 -> (o2.width * o2.height).compareTo(o1.width * o1.height) }
            }

            // previewSize
            mPreviewSize = Size(wantWidth, wantHeight)
//            getOptimalSize(
//                sizess.toList(),
//                wantWidth,
//                wantHeight,
//                GL2PreviewOptionLite.getCameraConfig()!!.highResolution
//            )
            Log.d("csson", "prepareCamera: mPreviewSize = $mPreviewSize")
            mSurfaceSize = getOptimalSize(
                sizess.toList(),
                wantWidth,
                wantHeight,
                GL2PreviewOptionLite.getCameraConfig()!!.highResolution
            )
            Log.d("csson", "prepareCamera: mSurfaceSize = $mSurfaceSize")
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     *
     * @param context
     * @param frame
     * @param surface   카메라가 생성의 frame을 전달할 surface
     * @param surfaceTexture  카메라의 frame을 전달할 surfaceTexture
     */
    fun startCamera(
        context: Context?,
        frame: Int,
        surface: Surface?,
        surfaceTexture: SurfaceTexture?
    ) {
        mContext = context
        mFrame = frame
        mFrameSem = Semaphore(0)
        mState = STATE_PREVIEW
        mSurface = surface
        mSurfaceTexture = surfaceTexture
        mSurfaceTexture!!.setOnFrameAvailableListener(frameListener)

        try {
            mImageReader = ImageReader.newInstance(
                mSurfaceSize!!.width,
                mSurfaceSize!!.height,
                ImageFormat.JPEG,
                /*maxImages*/
                5
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        mImageReader?.setOnImageAvailableListener(
            mOnImageAvailableListener, mBackgroundHandler
        )
        val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val permissionCamera =
                ContextCompat.checkSelfPermission(mContext!!, Manifest.permission.CAMERA)
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            } else {
                manager.openCamera(mCameraID!!, mStateCallback, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        isCameraStarted = true
    }

    private val mStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            startPreview()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }
    }

    private fun set3A(captureRequestBuilder: CaptureRequest.Builder?) {
        captureRequestBuilder?.let {
            it.set(
                CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO
            )

            it.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )

            it.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO
            )

            it.set(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )
        }
    }

    private fun set3A(
        captureRequestBuilder: CaptureRequest.Builder?,
        cameraCharacteristics: CameraCharacteristics
    ) {
        captureRequestBuilder!!.set(
            CaptureRequest.CONTROL_MODE,
            CaptureRequest.CONTROL_MODE_AUTO
        )

        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON
        )

        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_AUTO
        )

        captureRequestBuilder.set(
            CaptureRequest.CONTROL_AWB_MODE,
            CaptureRequest.CONTROL_AWB_MODE_AUTO
        )

        val controlAeAvailableTargetFpsRanges = cameraCharacteristics.get(
            CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
        )
        if (controlAeAvailableTargetFpsRanges != null) {
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                controlAeAvailableTargetFpsRanges[controlAeAvailableTargetFpsRanges.size - 1]
            )
        }
    }

    private fun chooseStabilizationMode(captureRequestBuilder: CaptureRequest.Builder?) {
        captureRequestBuilder?.let {
            it.set(
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
            )

            it.set(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            )
        }
    }

    private fun chooseStabilizationMode(
        captureRequestBuilder: CaptureRequest.Builder?,
        cameraCharacteristics: CameraCharacteristics
    ) {
        captureRequestBuilder!!.set(
            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
            CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
        )
        captureRequestBuilder.set(
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
        )
    }

//    fun flashAvailable(): Boolean {
//        // FIXME: 사용중인 카메라의 플래시 사용가능 여부를 체크하는 메서스
//        // 지금 당장은 필요없을 것으로 보여짐.
//        return false
//    }

    fun flashOnOff(bFlash: Boolean) {
        mPreviewBuilder?.let {
            isFlash = bFlash
            setFlashMode(it, bFlash)
            mPreviewRequest = it.build()
        }
        updatePreview()
    }

    private fun setFlashMode(captureRequestBuilder: CaptureRequest.Builder, bFlash: Boolean) {
        captureRequestBuilder.set(
            CaptureRequest.FLASH_MODE,
            if (bFlash) CaptureRequest.FLASH_MODE_TORCH else null
        )
    }

    /**
     * Underwater mode on
     *
     */
    fun underwaterModeOn() {
        mPreviewBuilder?.let {
            it.set(
                CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO
            )

            it.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )

            val nanoseconds = 1000000000L
            Log.d(TAG, "value = ${(nanoseconds * (1 / 500.0)).toLong()}")
            it.set(
                CaptureRequest.SENSOR_EXPOSURE_TIME,
                (nanoseconds * (1 / 500.0)).toLong()
            )
            it.set(
                CaptureRequest.SENSOR_SENSITIVITY,
                1000
            )
            it.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO
            )
//
            it.set(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )

            mPreviewRequest = it.build()
        }
        isWaterMode = true
        updatePreview()
    }

    fun underwaterModeOff() {
        mPreviewBuilder?.let {
            set3A(it)
            chooseStabilizationMode(it)
            mPreviewRequest = it.build()
        }
        isWaterMode = false
        updatePreview()
    }

    internal fun startPreview() {
        mSurfaceTexture!!.setDefaultBufferSize(mSurfaceSize!!.width, mSurfaceSize!!.height)
        try {
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        if (mPreviewBuilder != null && mSurface != null) {
            mPreviewBuilder!!.addTarget(mSurface!!)
        }
        try {
            val cameraManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraCharacteristics: CameraCharacteristics =
                cameraManager.getCameraCharacteristics(mCameraID!!)

            if (isWaterMode) {
                underwaterModeOn()
            } else {
                set3A(mPreviewBuilder, cameraCharacteristics)
            }
            chooseStabilizationMode(mPreviewBuilder, cameraCharacteristics)
            //
            mPreviewBuilder?.let {
                if (mIsSelfie) isFlash = false
                setFlashMode(it, isFlash)
            }
            //setTrigger(mPreviewBuilder, cameraCharacteristics);
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        if (mZoomLevel > 1) {
            computeZoomArea(mPreviewBuilder)
        }

        try {
            mCameraDevice!!.createCaptureSession(
                listOf(mSurface, mImageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cameraCaptureSession = session
                        mPreviewRequest = mPreviewBuilder!!.build()
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private var mCaptureCallback: CaptureCallback = object : CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                    val afTrigger = result.get(CaptureResult.CONTROL_AF_TRIGGER)
                    if (afTrigger == CaptureResult.CONTROL_AF_TRIGGER_START) {
                        mPreviewBuilder?.set(
                            CaptureRequest.CONTROL_AF_TRIGGER,
                            CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
                        )
                        updatePreview()
                    }
                }
                STATE_WAITING_LOCK -> {
//                    dd("STATE_WAITING_LOCK")
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    mState = STATE_PICTURE_TAKEN
                    captureStillPicture()
                }
                STATE_WAITING_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
//                    dd("STATE_WAITING_PRECAPTURE")
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {

                    // CONTROL_AE_STATE can be null on some devices
//                    dd("STATE_WAITING_NON_PRECAPTURE")
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    internal fun updatePreview() {
        try {
            cameraCaptureSession?.stopRepeating()

            if (mPreviewBuilder != null) {
                mPreviewRequest = mPreviewBuilder?.build()
            }

            if (cameraCaptureSession != null && mPreviewBuilder != null) {
                cameraCaptureSession!!.setRepeatingRequest(
                    mPreviewRequest!!,
                    object : CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                            super.onCaptureCompleted(session, request, result)
                            if (result.get(CaptureResult.CONTROL_AF_TRIGGER) == CaptureResult.CONTROL_AF_TRIGGER_START) {
                                Log.d(TAG, "onCaptureCompleted: 222")
                                mPreviewBuilder?.let {
                                    it.set(
                                        CaptureRequest.CONTROL_AF_TRIGGER,
                                        CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
                                    )
                                    mPreviewRequest = it.build()
                                }
                                updatePreview()
                            }
                        }
                    },
                    mBackgroundHandler
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun autoFocus() {
        try {
            val cameraManager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraCharacteristics: CameraCharacteristics =
                cameraManager.getCameraCharacteristics(
                    mCameraID!!
                )

            val minFocusDistance =
                cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
                    ?.toLong() ?: 0
            Log.d(TAG, "autoFocus: minFocusDistance = $minFocusDistance")
            val minFocusDistanceCALIBRATION =
                cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION)
            Log.d(
                TAG,
                "autoFocus: minFocusDistance = ${Gson().toJson(minFocusDistanceCALIBRATION)}"
            )

            mPreviewBuilder?.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_START
            )
            updatePreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun resetFocus() {
        updatePreview()
    }

    private val isMeteringAreaAESupported: Boolean
        get() {
            val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var mCameraInfo: CameraCharacteristics? = null
            try {
                mCameraInfo = manager.getCameraCharacteristics(mCameraID!!)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            return mCameraInfo!!.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)!! >= 1
        }
    private val isMeteringAreaAFSupported: Boolean
        get() {
            val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var mCameraInfo: CameraCharacteristics? = null
            try {
                mCameraInfo = manager.getCameraCharacteristics(mCameraID!!)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
            return mCameraInfo!!.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)!! >= 1
        }

    //leess zoomLevel: Int -> Float
    var mZoomLevel = 1f

    //fun setZoom(zoomLevel: Int): Boolean {
    fun setZoom(zoomLevel: Float): Boolean {
        mZoomLevel = zoomLevel
        val captureCallbackHandler: CaptureCallback = object : CaptureCallback() {

        }
        return try {
            computeZoomArea(mPreviewBuilder)
            cameraCaptureSession!!.setRepeatingRequest(
                mPreviewBuilder!!.build(),
                captureCallbackHandler,
                mBackgroundHandler
            )
            true
        } catch (e: Exception) {
            //Error handling up to you
            true
        }
    }

    fun computeZoomArea(captureRequestBuilder: CaptureRequest.Builder?): Boolean {
        val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var mCameraInfo: CameraCharacteristics? = null
        try {
            mCameraInfo = manager.getCameraCharacteristics(mCameraID!!)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        return try {
                val rect = mCameraInfo!!.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                    ?: return false
                //leess 20221226 줌모드크래시수정 : 2.0은 너무 확대가 되는것 같아서 1.5로 수정
                //val ratio = 1.toFloat() / mZoomLevel //This ratio is the ratio of cropped Rect to Camera's original(Maximum) Rect //croppedWidth and croppedHeight are the pixels cropped away, not pixels after cropped
                val ratio = 1.toFloat() / 1.5f

                val croppedWidth = rect.width() - (rect.width().toFloat() * ratio).roundToInt()
                val croppedHeight = rect.height() - (rect.height().toFloat() * ratio).roundToInt()
                //Finally, zoom represents the zoomed visible area
                val zoom = Rect(
                    croppedWidth / 2,
                    croppedHeight / 2,
                    rect.width() - croppedWidth / 2,
                    rect.height() - croppedHeight / 2
                )
                captureRequestBuilder!!.set(CaptureRequest.SCALER_CROP_REGION, zoom)
                true
            } catch (e: Exception) {
                //Error handling up to you
                false
            }
//        } else {
//            false
//        }
    }

    fun captureStillPicture() {
        Log.d(TAG, "captureStillPicture: ")
        if (mCameraDevice == null) {
            return
        }
        try {
            val manager = mContext!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraCharacteristics = manager.getCameraCharacteristics(mCameraID!!)
            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)
            if (mZoomLevel > 1) {
                computeZoomArea(captureBuilder)
            }

            captureBuilder.set(
                CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO
            )
            captureBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
            captureBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            captureBuilder.set(
                CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO
            )


            val controlAeAvailableTargetFpsRanges = cameraCharacteristics.get(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES
            )
            if (controlAeAvailableTargetFpsRanges != null) {
                captureBuilder.set(
                    CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                    controlAeAvailableTargetFpsRanges[controlAeAvailableTargetFpsRanges.size - 1]
                )
            }

            captureBuilder.set(
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
            )
            captureBuilder.set(
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            )

            val captureCallback: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    unlockFocus()
                }
            }
            if (cameraCaptureSession != null) {
                cameraCaptureSession!!.capture(captureBuilder.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun takePicture() {
        lockFocus()
    }

    fun lockAE() {
        if (cameraCaptureSession == null) {
            return
        }
        try {
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AE_LOCK,
                java.lang.Boolean.TRUE
            )
            cameraCaptureSession!!.capture(
                mPreviewBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
            cameraCaptureSession!!.setRepeatingRequest(
                mPreviewBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun unlockAE() {
        if (cameraCaptureSession == null) {
            return
        }
        try {
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AE_LOCK,
                java.lang.Boolean.FALSE
            )
            cameraCaptureSession!!.capture(
                mPreviewBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
            cameraCaptureSession!!.setRepeatingRequest(
                mPreviewBuilder!!.build(),
                null,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun lockFocus() {
        if (cameraCaptureSession == null) {
            return
        }
        try {
            // This is how to tell the camera to lock focus.
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )

            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            cameraCaptureSession!!.capture(
                mPreviewBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun unlockFocus() {
        if (cameraCaptureSession == null) {
            return
        }
        try {
            // Reset the auto-focus trigger
//            mPreviewBuilder!!.set(
//                CaptureRequest.CONTROL_AF_TRIGGER,
//                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
//            )
//            mPreviewBuilder!!.set(
//                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL
//            )
            cameraCaptureSession!!.capture(
                mPreviewBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW

            updatePreview()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewBuilder!!.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE
            cameraCaptureSession!!.capture(
                mPreviewBuilder!!.build(),
                mCaptureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    fun stopBackgroundThread() {
        if (mBackgroundThread != null && mBackgroundThread!!.isAlive) {
            mBackgroundThread!!.quitSafely()
            try {
                mBackgroundThread!!.join()
                mBackgroundThread = null
                mBackgroundHandler = null
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private class ImageSaver(private val mImage: Image) : Runnable {
        private val TAG = this::class.java.simpleName
        private var fileName: String? = null
        private var localPath: String? = null
        private var thumbPath: String? = null
        override fun run() {
            try {
                Log.d(TAG, "run Start")
                Log.d(TAG, "run: csson image size = ${mImage.width}x${mImage.height}")
                isSaveDone = false

                fileName = createImgFileName()
                localPath = getLocalMediaPath(fileName, false)
                thumbPath = getLocalMediaPath(fileName, true)

                val buffer = mImage.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer[bytes]

                var bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                if(mIsSelfie) {
                    val matrix = Matrix()
                    matrix.preScale(-1f , 1f)
                    bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, matrix, true)
                }

                Log.d(TAG, "bitmapImage size = ${bitmapImage.width}x${bitmapImage.height}")

                Log.d(TAG, "run: fileName = $fileName")
                Log.d(TAG, "run: localPath = $localPath")
                Log.d(TAG, "run: thumbPath = $thumbPath")

                if (GL2JNILib.getFilterOn() == 1) { // 필터 적용
                    ImageFilter(bitmapImage, object : ImageFilter.Callback {
                        override fun onResult(success: Boolean, result: Bitmap?) {
                            save(result)
                        }
                    }).execute()
                } else {
                    save(bitmapImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                mImage.close()
                isSaveDone = true
            }
        }

        private fun save(bitmap: Bitmap?) {
            try {
                if (bitmap == null) {
                    throw java.lang.Exception("bitmap is Null")
                }
                // save original image
                BitmapUtils.saveBitmap(localPath!!, bitmap)

                // save thumbnail image
                val thumbBitmap =
                    BitmapUtils.getResizedBitmap(bitmap, MediaFileControl.SIZE_THUMBNAIL_X)
                BitmapUtils.saveBitmap(thumbPath!!, thumbBitmap)
                thumbBitmap.recycle()

                imageSaverListener?.onImageSaveCompleted(localPath!!, thumbPath!!)
            } catch (e: Exception) {
                e.printStackTrace()
                imageSaverListener?.onImageSaveFailed(e.message ?: "")
            }
        }

    }

    /**
     * Compares two `Size`s based on their areas.
     */
    internal class CompareSizesByArea : Comparator<Size> {
        override fun compare(o1: Size, o2: Size): Int {
            return java.lang.Long.signum(
                o1.width.toLong() * o1.height -
                        o2.width.toLong() * o2.height
            )
        }
    }

    private var imageSaverListener: OnImageSaverListener? = null

    fun setOnImageSaverListener(l: OnImageSaverListener) {
        imageSaverListener = l
    }

    interface OnImageSaverListener {
        fun onImageSaveCompleted(filePath: String, thumbPath: String)
        fun onImageSaveFailed(msg: String)
    }


}