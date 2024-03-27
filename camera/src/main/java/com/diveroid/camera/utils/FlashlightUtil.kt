package com.diveroid.camera.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.google.gson.Gson


object FlashlightUtil {
    private val TAG = FlashlightUtil::class.java.simpleName
    private var isFlash: Boolean = false

    fun isFlash():Boolean = isFlash

    /**
     *  디바이스 카메라 플래시 기능 사용가능 여부
     */
    fun hasFlashlight(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    /**
     * 카메라 플래시 On/Off
     *
     * @param isOn true이면 플래시 On, false이면 Off
     */
    fun flashlightOnOff(context: Context, isOn: Boolean) {
        if(isOn) {  // 플래시 켜짐
            flashlightOn(context)
        } else {
            flashlightOff(context)
        }
    }

    /**
     *  카메라 플래시 On
     */
    fun flashlightOn(context: Context) {
        if(isFlash) return

        Log.d("csson", "flashlightOn")

        val cameraId = getFlashlightCameraId(context)

        cameraId?.let {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.setTorchMode(cameraId, true)
            isFlash = true
        }
    }

    /**
     *  카메라 플래시 Off
     */
    fun flashlightOff(context: Context) {
        if(!isFlash) return

        Log.d("csson", "flashlightOff")

        val cameraId = getFlashlightCameraId(context)
        cameraId?.let {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.setTorchMode(cameraId, false)
            isFlash = false
        }
    }

    private fun getFlashlightCameraId(context: Context): String? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        Log.d("csson", "getFlashlightCameraIds: ${Gson().toJson(manager.cameraIdList)}")

        for(id in manager.cameraIdList) {
            val c = manager.getCameraCharacteristics(id)

            val flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
            Log.d("csson", "flashAvailable: $flashAvailable")
            val lensFacing = c.get(CameraCharacteristics.LENS_FACING)!!
            Log.d("csson", "flashAvailable: $lensFacing")

            if(flashAvailable && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                Log.d("csson", "getFlashlightCameraId = $id")
                return id
            }
        }
        return null
    }
}