package com.diveroid.camera.data

import com.diveroid.camera.utils.Util

data class CameraConfigLite(
    private var _cameraResolution: Util.CameraResolution = Util.CameraResolution.FHD,
    private var _cameraRatio: Util.CameraRatio = Util.CameraRatio.RATIO_FULL
): CameraConfig(
    _cameraResolution == Util.CameraResolution.UHD_4K,
    _cameraResolution
) {
    var cameraResolution: Util.CameraResolution
        get() = _cameraResolution
        set(value) {
            _cameraResolution = value
            super.highModeOn = _cameraResolution == Util.CameraResolution.UHD_4K
            super.highResolution = _cameraResolution
        }

    var cameraRatio: Util.CameraRatio
        get() = _cameraRatio
    set(value) {
        _cameraRatio = value
    }
}

