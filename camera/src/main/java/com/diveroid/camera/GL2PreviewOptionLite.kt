package com.diveroid.camera

import android.util.Log
import com.diveroid.camera.data.CameraConfig
import com.diveroid.camera.data.CameraConfigLite
import com.diveroid.camera.provider.ContextProvider
import com.diveroid.camera.underfilter.GL2JNIView
import com.diveroid.camera.utils.Util
import com.google.gson.Gson

object GL2PreviewOptionLite {
    private val TAG = GL2PreviewOptionLite::class.java.simpleName


    const val IS_DEFAULT_FILTER_OPTION = false

    const val OPTION_WIDE = GL2JNIView.CAMERA_ANGLE_WIDE
    const val OPTION_ULTRA_WIDE = GL2JNIView.CAMERA_ANGLE_ULTRA_WIDE
    const val OPTION_ZOOM = GL2JNIView.CAMERA_ANGLE_ZOOM
    const val OPTION_SELFIE = GL2JNIView.CAMERA_ANGLE_SELFIE

    private var cameraConfig: CameraConfig? = null
    var isScubaOrSnorkelMode: Boolean = true
        set(value) {
            field = value
            initOptionVisibility(value, isWideAvailable)
        }
    var isWideAvailable: Boolean = false
        set(value) {
            field = value
            initOptionVisibility(isScubaOrSnorkelMode, value)
        }

    var RESOLUTION_WIDTH = Util.CameraResolution.FHD.size.width
    var RESOLUTION_HEIGHT = Util.CameraResolution.FHD.size.height

    var OUTPUT_WIDTH = 0
    var OUTPUT_HEIGHT = 0


    var SCREEN_RATIO = Util.CameraRatio.RATIO_FULL

    var PREPARE_HIGH_MODE_ON = false
    var PREPARE_IS_SELFIE_MODE = false
    var PREPARE_WIDE_MODE = false
    var PREPARE_FRAME = 0

    /**
     *  옵션 최대 갯수
     *  추후 옵션이 추가될 경우 해당 값을 변경해줘야 할 수도 있음 (6/10)
     *
     *  @see OPTION_WIDE
     *  @see OPTION_ULTRA_WIDE
     *  @see OPTION_ZOOM
     *  @see OPTION_SELFIE
     */
    const val OPTION_MAX_RANGE = 10

    /*------------------------
    3개는 하나로 묶어서 라디오 버턴 처럼 작동 해야함
     -----------------------------*/
    /**
     *  옵션 버튼 선택 여부 배열
     */
    val selectButtons: BooleanArray = BooleanArray(OPTION_MAX_RANGE)

    /**
     *  사용가능한 옵션 버튼 표시 여부
     */
    var enableButtons: BooleanArray = BooleanArray(OPTION_MAX_RANGE)

    /**
     *  현재 선택한 값
     */
    var currentSelect = 0

    fun initCameraConfig(config: CameraConfig?) {
        cameraConfig = config
        cameraConfig?.let {
            PREPARE_HIGH_MODE_ON = it.highModeOn
            PREPARE_WIDE_MODE = it.highAngle == Util.CameraAngle.SUPER_WIDE

            val refWidth = it.highResolution.size.width
            var refHeight = it.highResolution.size.height

            if(it is CameraConfigLite) {
                SCREEN_RATIO = it.cameraRatio
                refHeight = when(SCREEN_RATIO) {
                    Util.CameraRatio.RATIO_FULL -> {
                        (refWidth / (16f / 10)).toInt()
                    }
                    Util.CameraRatio.RATIO_4_3 -> {
                        (refWidth / (4f / 3)).toInt()
                    }
                    else -> {
                        refHeight
                    }
                }
            }

            RESOLUTION_WIDTH = refWidth
            RESOLUTION_HEIGHT = refHeight
            PREPARE_FRAME = it.highFrame
        }
    }



    private fun initOptionVisibility(isScubaOrSnorkelMode: Boolean, isWideAvailable: Boolean) {
        Log.d(TAG, "initOptionVisibility: isScubaOrSnorkelMode = $isScubaOrSnorkelMode")
        Log.d(TAG, "initOptionVisibility: isWideAvailable = $isWideAvailable")

        if (isScubaOrSnorkelMode) {
            enableButtons[OPTION_WIDE] = true
            enableButtons[OPTION_ULTRA_WIDE] = isWideAvailable
        } else {
            enableButtons[OPTION_WIDE] = !(cameraConfig?.highModeOn?: false) && !isWideAvailable
            enableButtons[OPTION_ULTRA_WIDE] = !(cameraConfig?.highModeOn?: false) && isWideAvailable
        }
        enableButtons[OPTION_ZOOM] = isScubaOrSnorkelMode
        enableButtons[OPTION_SELFIE] = true

        Log.d("csson", "initOptionVisibility: enableButtons = ${Gson().toJson(enableButtons)}")

        currentSelect = enableButtons.withIndex().firstOrNull {
            it.value
        }?.index ?: 0

        Log.d("csson", "initOptionVisibility: currentSelect = $currentSelect")
    }

    fun nextOptionIndex(): Int {
        val indexedValues = enableButtons.withIndex().filter {
            it.value
        }
        val indexedValue = indexedValues.firstOrNull {
            it.index > currentSelect
        } ?: indexedValues.first()

        Log.d("csson", "nextOptionIndex: ${indexedValue.index}")

        return indexedValue.index
    }

    fun getCameraConfig(): CameraConfig? = cameraConfig
}