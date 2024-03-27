package com.diveroid.camera

import android.content.Context
import android.util.Log
import com.diveroid.camera.data.CameraConfig
import com.diveroid.camera.utils.Util

class GL2PreviewOption(
    private val context: Context,
    private val config: CameraConfig,
    isScubaOrSnorkelMode: Boolean,
    isWideAvailable: Boolean
) {
    private val TAG = GL2PreviewOption::class.java.simpleName

    var PREPARE_HIGH_MODE_ON = false
    var PREPARE_IS_SELFIE_MODE = false
    var PREPARE_WIDE_MODE = 0

    //HIGH 세팅
    var PREPARE_HIGH_WIDTH = 0
    var PREPARE_HIGH_HEIGHT = 0
    var PREPARE_HIGH_FRAME = 0

    //기본세팅
    var PREPARE_LOW_WIDTH = 1920
    var PREPARE_LOW_HEIGHT = 1080
    var PREPARE_LOW_FRAME = 30


    var OPTION_FR_STOPWATCH = 0
    var OPTION_FR_DEPTH_ALARM = 0

    @JvmField
    var OPTION_WIDE = 0
    @JvmField
    var OPTION_NORMAL = 0
    @JvmField
    var OPTION_ZOOM = 0
    @JvmField
    var OPTION_HIGH_QUALITY = 0
    @JvmField
    var OPTION_SELFIE = 0
    var OPTION_SCUBA_TEST = 0

    /**
     *  옵션 최대 갯수
     *  추후 옵션이 추가될 경우 해당 값을 변경해줘야 할 수도 있음 (6/10)
     *
     *  @see OPTION_NORMAL
     *  @see OPTION_WIDE
     *  @see OPTION_ZOOM
     *  @see OPTION_HIGH_QUALITY
     *  @see OPTION_FR_DEPTH_ALARM
     *  @see OPTION_FR_STOPWATCH
     *  @see OPTION_SCUBA_TEST
     */
    private val OPTION_MAX_RANGE = 10

    /*------------------------
    3개는 하나로 묶어서 라디오 버턴 처럼 작동 해야함
     -----------------------------*/
    /**
     *  옵션 버튼 선택 여부 배열
     */
    @JvmField
    var selectButtons: BooleanArray = BooleanArray(OPTION_MAX_RANGE)

    /**
     *  사용가능한 옵션 버튼 표시 여부
     */
    var enableButtons: BooleanArray = BooleanArray(OPTION_MAX_RANGE)

    /**
     *  현재 선택한 값
     */
    var currentSelect = 0

    /**
     *  현재 적용된 값
     */
    var currentApplied = 0

    var curOdapted = 0
    var prvOdapted = 0

    private fun initConfigSetting() {
        PREPARE_HIGH_MODE_ON = config.highModeOn
        PREPARE_WIDE_MODE = if (config.highAngle === Util.CameraAngle.SUPER_WIDE) 1 else 0
        val size = Util.getResolutionSize(context, config.highResolution)
        PREPARE_HIGH_WIDTH = size.width
        PREPARE_HIGH_HEIGHT = size.height
        PREPARE_HIGH_FRAME = config.highFrame
    }

    private fun initOptionOrder() {
        OPTION_WIDE = 0
        OPTION_NORMAL = 1
        OPTION_ZOOM = 2
        OPTION_HIGH_QUALITY = 3
        OPTION_SELFIE = 4
        OPTION_FR_STOPWATCH = 5
        OPTION_FR_DEPTH_ALARM = 6
        OPTION_SCUBA_TEST = OPTION_MAX_RANGE - 1
    }

    private fun initOptionVisibility(isScubaOrSnorkelMode: Boolean, isWideAvailable: Boolean) {
        Log.d(TAG, "initOptionVisibility: isScubaOrSnorkelMode = $isScubaOrSnorkelMode")
        Log.d(TAG, "initOptionVisibility: isWideAvailable = $isWideAvailable")

        if (isScubaOrSnorkelMode) {
            enableButtons[OPTION_NORMAL] = true
            enableButtons[OPTION_WIDE] = isWideAvailable
        } else {
            enableButtons[OPTION_NORMAL] = !config.highModeOn && !isWideAvailable
            enableButtons[OPTION_WIDE] = !config.highModeOn && isWideAvailable
        }
        enableButtons[OPTION_ZOOM] = isScubaOrSnorkelMode
        enableButtons[OPTION_HIGH_QUALITY] = config.highModeOn
        enableButtons[OPTION_SELFIE] = true
        enableButtons[OPTION_FR_STOPWATCH] = !isScubaOrSnorkelMode
        enableButtons[OPTION_FR_DEPTH_ALARM] = !isScubaOrSnorkelMode
//        enableButtons[OPTION_SCUBA_TEST] = BuildConfig.DEBUG && isVelocityTestMode
    }

    private fun initFirstSelectOption(isWideAvailable: Boolean) {
        currentSelect = if (PREPARE_HIGH_MODE_ON) OPTION_HIGH_QUALITY else {
            if (isWideAvailable) OPTION_WIDE else OPTION_NORMAL
        }
        selectButtons[currentSelect] = true
        curOdapted = currentSelect
        prvOdapted = currentSelect
    }

    fun getCameraConfig(): CameraConfig = config

    companion object {
        const val IS_DEFAULT_FILTER_OPTION = false
    }

    init {
        //실행중인 기능들
        //선택가능한 기능들
        initConfigSetting()
        initOptionOrder()
        initOptionVisibility(isScubaOrSnorkelMode, isWideAvailable)
        initFirstSelectOption(isWideAvailable)
    }
}