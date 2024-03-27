package com.diveroid.camera.constant

/**
 * Created by dudbs on 2016-07-26.
 */
object DivingConstants {
    var METRIC = 0
    var FEET = 1

    /* Exposure Up Down */
    const val EXPOSURE_UP = 0
    const val EXPOSURE_DOWN = 1

    /* Default Mode Details */
    const val COMPASS_MODE = 0
    const val GRAPH_MODE = 1

    /* Popup Mode */
    const val NONE_POPUP_MODE = 0
    const val MODE_SELECT_POPUP_MODE = 1

    /* Language Mode */
    const val NONE_LANGUAGE = 0
    const val KOREAN = 1
    const val ENGLISH = 2
    const val JAPANESE = 3
    const val CHINESE = 4

    /** Housing Type  */
    const val SEATYPE_SEA = 0
    const val SEATYPE_FRESH = 1

    /** Housing Type
     */
    //"Universal pro", "Meikon", "Seafrogs", "Lenzo", "Mpac", "Xpoovv", "Patima"
    const val HOUSING_PRV_DIVEROID = 0
    const val HOUSING_DEBUG = 1
    const val HOUSING_MPAC = 4
    const val HOUSING_PATIMA = 8
    const val HOUSING_UNIVERSIAL_PRO = 11
    const val HOUSING_XPOOVV = 12

    /**HOUSING CONTROL  */
    const val MPAC_SELFIE = 0
    const val MPAC_SLEEP = 1
    const val MPAC_COSING = 4
    const val MPAC_FOCUS = 5
    const val MPAC_SLEEP_HANDLER = 6
    const val MPAC_WIDE_NORMAL_CHANGE = 7
    const val UART_CONNECTION = 0
    const val BLUETOOTH_CONNECTION = 1
    const val DEBUG_CONNECTION = 2
    const val SIMULATION_CONNECTION = 3
    const val COMPUTER_MODE = 0
    const val PHOTO_MODE = 1
    const val VIDEO_MODE = 2
    const val FINISH_ACTIVITY = 3
    const val POWER_SAVE_MODE = 4
    const val REFRESH_FLOATING_VIEW = 5
    const val MODE_ORIGIN = -1
    const val FOCUS_ANIMATION = 22
    const val SHUTTER_ANIMATION = 23
    const val CHANGE_DETAIL_COMPASS_MODE = 24
    const val CHANGE_DETAIL_GRAPH_MODE = 25
    const val MODE_CHANGE_ANIMATION = 26
    const val CLOSE_SUMMARY_VIEW = 27

    /* Diving Data Types */
    const val DIVING_DATA_SENSOR = 1
    const val DIVING_DATA_EVENT = 2
    const val DIVING_DATA_PHOTO = 3
    const val DIVING_DATA_VIDEO = 4
    const val DIVING_SENSING_LIMIT_DEPTH = 110
    const val HEALTH_CONDITION_GREAT = 0
    const val HEALTH_CONDITION_NORMAL = 1
    const val HEALTH_CONDITION_POOR = 2
    const val UNIT_SYSTEM_METRIC = 0
    const val UNIT_SYSTEM_IMPERIAL = 1
    const val FILTER_COLOR = "#66FF0000"
    const val FASTUP_MAX_VELOCITY_METER = 0.19f * 4 // 250~400
    const val FASTUP_MAX_VELOCITY_METER_WITHER_WARNING = 0.195f * 4 // 250~400

    //배터리 영향줄 수 있는 변수들 아래쪽에 모아쓰ㄲㄷ
    const val UI_UPDATE_INTERVAL = 400 // 250~400
    const val FREE_DIVE_UI_UPDATE_INTERVAL = 200 // 250~400
    const val VELOCITY_UPDATE_INTERVAL_MILISECOND = 100 //100~200
}