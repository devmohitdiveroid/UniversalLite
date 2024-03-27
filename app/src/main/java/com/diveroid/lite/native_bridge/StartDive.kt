package com.diveroid.lite.native_bridge

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import com.diveroid.camera.GL2PreviewOptionLite
import com.diveroid.camera.data.CameraConfigLite
import com.diveroid.camera.utils.Util
import com.diveroid.lite.CameraViewActivity
import com.diveroid.lite.data.PrefData
import com.diveroid.lite.util.PrefUtil
import com.google.gson.Gson


class StartDive : _BaseBridge {
    override val cmd: String
        get() = "startDive"

    override fun startAction(activity: Activity?, webView: WebView?, data: String?, callback: String?) {
        try {

            if (activity == null) return
            val requestPermissionList = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
            ).toTypedArray()

            val deniedPermissions = requestPermissionList.filter {
                val isDenied = activity.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
                if (isDenied) {
                    Log.d("csson", "startAction: permission = $it")
                }
                isDenied
            }

            if (deniedPermissions.isEmpty()) {   // 거절된 권한 없는 경우
                startCameraViewActivity(activity)
            } else {    // 거절된 권한이 있는 경우
                activity.requestPermissions(requestPermissionList, REQUEST_CAMERA_VIEW_PERMISSION_CODE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startCameraViewActivity(activity: Activity?) {
        activity?.let {
            val prefUtil = PrefUtil.getInstance(activity)

            //
            val resolutionData = Gson().fromJson(prefUtil.getString("PREF_CAMERA_SETTING_RESOLUTION", ""), PrefData::class.java)
            val ratioData = Gson().fromJson(prefUtil.getString("PREF_CAMERA_SETTING_RATIO", ""), PrefData::class.java)


            var resolution = Util.CameraResolution.FHD
            resolutionData?.let {
                resolution = if (resolutionData.value == "FULLHD") {
                    Util.CameraResolution.FHD
                } else {
                    Util.CameraResolution.UHD_4K
                }
            }

            var ratio = Util.CameraRatio.RATIO_FULL
            ratioData?.let {
                ratio = when (ratioData.value ?: "") {
                    "16:9" -> Util.CameraRatio.RATIO_16_9
                    "4:3" -> Util.CameraRatio.RATIO_4_3
                    else -> Util.CameraRatio.RATIO_FULL
                }
            }

            Log.d(cmd, "startCameraViewActivity: resolution = $resolution")
            Log.d(cmd, "startCameraViewActivity: ratio = $ratio")

            GL2PreviewOptionLite.initCameraConfig(CameraConfigLite(resolution, ratio))
            val intent = Intent(it, CameraViewActivity::class.java)
            it.startActivity(intent)
        }
    }

    fun retryPermissionCheck(activity: Activity?) {
        activity?.let {
            AlertDialog.Builder(activity)
                .setTitle("권한 설정")
                .setMessage("일부 권한을 거부하여 해당 기능을 사용할 수 없습니다.\n [설정 - 권한]에서 허용해주세요.")
                .setPositiveButton(
                    "설정"
                ) { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:${it.packageName}"))
                    it.startActivity(intent)
                }
                .setNegativeButton("취소") { _, _ ->
                    Toast.makeText(it, "해당 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                .create()
                .show()

        }
    }


    companion object {
        const val REQUEST_CAMERA_VIEW_PERMISSION_CODE = 10000
    }
}
