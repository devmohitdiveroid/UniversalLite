package com.diveroid.camera.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.util.*
import android.view.Display
import android.view.WindowInsets
import android.view.WindowManager
import java.util.ArrayList
import java.util.Comparator
import kotlin.math.atan

object Util {
    enum class CameraResolution(var size: Size, var widthList : List<Int> ){
        UHD_4K( Size(3840,2160), listOf( 3840, 4032, 3360, 3264, 3984, 4160, 4000 )),
        QHD(Size(2560,1440), listOf( 2560 )),
        FHD(Size(1920,1080), listOf( 1920 )),
        HD(Size(1280,720), listOf( 1280 )),
        DEFAULT(Size(0,0), listOf( 0 ))
    }

    enum class CameraRatio {
        RATIO_4_3,
        RATIO_16_9,
        RATIO_FULL  // 16:10
    }

    enum class CameraAngle {
        SUPER_WIDE, WIDE, DEFAULT
    }

    fun hasBackSpaceWideCamera(context: Context): Boolean {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var findFist = false
            var firstAngle = 0f
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!
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

    fun hasFrontSpaceWideCamera(context: Context): Boolean {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var findFist = false
            var firstAngle = 0f

            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
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

    /**
     * getSupportResolution 현재 핸드폰이 지원하는 화질리스트이고 CameraResolution 리스트를 반환한다.
     * @see CameraResolution
     */
    fun getSupportResolutions( context: Context ) : List<CameraResolution> {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId2 = manager!!.cameraIdList[0]
        val characteristics = manager.getCameraCharacteristics(cameraId2)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        val imageDimension = map.getOutputSizes(SurfaceTexture::class.java)
        val list = mutableListOf<CameraResolution>()

        CameraResolution.values().forEach { resolution ->
            resolution.widthList.forEach inner@ {width ->
                imageDimension.sortedWith(compareBy { it.height * it.width }).reversed().forEach dimension@ {
                    if ( it.width == width ){
                        resolution.size = it
                        list.add(resolution)
                        return@inner
                    }
                }
            }
        }

        return list
    }

    fun getCameraSupportResolutions(context: Context): List<Size>{
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
        val cameraId2 = manager!!.cameraIdList[0]
        val characteristics = manager.getCameraCharacteristics(cameraId2)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        return map.getOutputSizes(SurfaceTexture::class.java).asList()
    }

    /**
     * getSupportResolution 현재 핸드폰이 지원하는 화질리스트이고 CameraResolution 리스트를 반환한다.
     * @see CameraResolution
     */
    fun getResolutionSize( context: Context, resolution : CameraResolution) : Size {
        val imageDimension = getCameraSupportResolutions(context)
        resolution.widthList.forEach { width ->
            imageDimension
                .sortedWith(compareBy { it.height * it.width })
                .reversed().forEach {
                    if (it.width <= width) {
                        return it
                    }
                }
        }

        return Size(1920,1080)
    }

    /**
     * Compares two `Size`s based on their areas.
     */
    class CompareSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(
                lhs.width.toLong() * lhs.height -
                        rhs.width.toLong() * rhs.height
            )
        }
    }

    fun isAvailable60Frame(context: Context, resolution : CameraResolution, isFront: Boolean = false, angle: CameraAngle = CameraAngle.WIDE) : Boolean{
        var arrSpeed: Array<Range<Int>>? = null
        arrSpeed = getAvailableHighSpeedUniverse(context, isFront, if( angle == CameraAngle.SUPER_WIDE ) 1 else 0 , resolution.size.width, resolution.size.height )

        if (arrSpeed != null) {
            for (i in arrSpeed.indices) {
                if (arrSpeed[i].lower >= 60) {
                    return true
                }
            }
        }
        return false
    }

    private fun getAvailableHighSpeedUniverse(context: Context, isFront: Boolean, isWide: Int, width: Int, height: Int): Array<Range<Int>>? {
        var oneCameraResult = ""
        val targetOrientation = if (isFront) {
            CameraCharacteristics.LENS_FACING_FRONT
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }

        val list = ArrayList<Pair<Float, Array<Range<Int>>>>()
        val cManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {

            var index = 0
            for (cameraId in cManager.cameraIdList) {
                val characteristics = cManager.getCameraCharacteristics(cameraId)
                val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)!!

                ++index

                if (cOrientation == targetOrientation) {
                    val secondAngle = computeAngle(cManager, cameraId)
                    val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    val sz = Size(width, height)
                    try {
                        list.add(Pair(secondAngle, map!!.getHighSpeedVideoFpsRangesFor(sz)))
                    } catch (e: Exception) {
                        //     return null;
                    }
                } else {
                    oneCameraResult += "front $index\r\n"
                }
            }

            list.sortWith { o1, o2 ->
                o1.first.compareTo(o2.first)
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

    private fun computeAngle(manager: CameraManager, cameraId: String): Float {
        val characteristics = manager.getCameraCharacteristics(cameraId)

        // 회각 계산
        val maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        var strMaxFocus = ""

        for (j in maxFocus!!.indices) {
            strMaxFocus += "(" + maxFocus[j] + ")"
        }

        val size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val w = size!!.width
        val h = size.height
        val horizonalAngle = (2 * atan((w / (maxFocus[0] * 2)).toDouble())).toFloat()
        val verticalAngle = (2 * atan((h / (maxFocus[0] * 2)).toDouble())).toFloat()
        return horizonalAngle
    }

//    fun rateCameraView



    fun <T>getPreviewOutputSize(
        display: Display,
        characteristics: CameraCharacteristics,
        targetClass: Class<T>,
        format: Int? = null
    ): Size {
        // Find which is smaller: screen or 1080p
        val screenSize = getDisplaySmartSize(display)
        val hdScreen = screenSize.long >= SIZE_1080P.long || screenSize.short >= SIZE_1080P.short
        val maxSize = if (hdScreen) SIZE_1080P else screenSize

        // If image format is provided, use it to determine supported sizes; else use target class
        val config = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        if (format == null)
            assert(StreamConfigurationMap.isOutputSupportedFor(targetClass))
        else
            assert(config.isOutputSupportedFor(format))
        val allSizes = if (format == null)
            config.getOutputSizes(targetClass) else config.getOutputSizes(format)

        // Get available sizes and sort them by area from largest to smallest
        val validSizes = allSizes
            .sortedWith(compareBy { it.height * it.width })
            .map { SmartSize(it.width, it.height) }.reversed()

        // Then, get the largest output size that is smaller or equal than our max size
        return validSizes.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
    }

    /**
     * Get screen width
     *
     * @param context
     * @return
     */
    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    /**
     * Get screen height
     *
     * @param context
     * @return
     */
    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height()
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)

            displayMetrics.heightPixels
        }
    }







}