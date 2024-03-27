package com.diveroid.camera.data

import com.diveroid.camera.utils.Util

open class CameraConfig(
    var highModeOn: Boolean = false,
    var highResolution: Util.CameraResolution = Util.CameraResolution.DEFAULT,
    var highFrame: Int = 30,
    var highAngle: Util.CameraAngle = Util.CameraAngle.WIDE
)
