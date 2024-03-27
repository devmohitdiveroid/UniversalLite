package com.diveroid.lite

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.diveroid.lite.native_bridge.ShowAuthView
import java.util.*

//leess 필요한 권한 한번에 모두 획득
class AuthActivity : BaseActivity() {
    private val REQUEST_PERMISSION_KEY = 714

    var view_background: View? = null
    private var permissions: Array<String>? = null
    private var grantCnt = 0
    private var failCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        view_background = findViewById(R.id.view_background)

        view_background!!.animate()
            .alpha(0.3f)
            .setDuration(500)

        permissions = needAskPermissions(this)

        if(permissions != null && permissions!!.size > 0) {
            requestPermissions(permissions!!, REQUEST_PERMISSION_KEY)
        } else {//모든 권한 OK
            ShowAuthView.webView!!.loadUrl("javascript:${ShowAuthView.callback!!}('success');")
            finishProcess()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_PERMISSION_KEY) {
            return
        }
        for (i in grantResults) {
            if (i == PackageManager.PERMISSION_GRANTED) {
                grantCnt++
            } else {
                failCnt++
            }

            if(grantCnt + failCnt == permissions!!.size) {
                if(failCnt == 0) {
                    ShowAuthView.webView!!.loadUrl("javascript:${ShowAuthView.callback!!}('success');")
                    finishProcess()
                } else {
                    ShowAuthView.webView!!.loadUrl("javascript:${ShowAuthView.callback!!}('fail');")

//                    AlertDialog.Builder(this)
//                        .setTitle("권한 설정")
//                        .setMessage("일부 권한을 거부하여 해당 기능을 사용할 수 없습니다.\n [설정 - 권한]에서 허용해주세요.")
//                        .setPositiveButton(
//                            "설정"
//                        ) { _, _ ->
//                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+packageName))
//                            intent.addCategory(Intent.CATEGORY_DEFAULT)
//                            startActivity(intent)
//
//                            finishProcess()
//                        }
//                        .setNegativeButton("취소") { _, _ ->
//                            //Toast.makeText(it, "해당 기능을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
//                            finishProcess()
//                        }
//                        .create()
//                        .show()

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+packageName))
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    startActivity(intent)
                    finishProcess()
                }
            }
        }
    }

    fun finishProcess() {
        view_background!!.animate()
            .alpha(0.0f)
            .setDuration(500)
            .withEndAction {
                finish()
            }
    }

    companion object {
        //획득하지 못한 권한 리턴
        fun needAskPermissions(context: Context): Array<String> {

            var arraylist = arrayListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                arraylist.add(Manifest.permission.READ_MEDIA_IMAGES)
                arraylist.add(Manifest.permission.READ_MEDIA_VIDEO)
                arraylist.add(Manifest.permission.READ_MEDIA_AUDIO)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                arraylist.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                arraylist.add(Manifest.permission.READ_EXTERNAL_STORAGE)

            val permissions = ArrayList(
                Arrays.asList(
                   // Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
//                    Manifest.permission.WAKE_LOCK,
                //    Manifest.permission.READ_EXTERNAL_STORAGE
//                    Manifest.permission.REORDER_TASKS,
//                    Manifest.permission.READ_PHONE_STATE,
//                    Manifest.permission.VIBRATE,
//                    Manifest.permission.READ_CONTACTS
                )
                //Manifest.permission.BLUETOOTH,
                //Manifest.permission.BLUETOOTH_ADMIN,
                //Manifest.permission.CALL_PHONE,
                //Manifest.permission.SEND_SMS)
            )

            permissions.addAll(arraylist)

            val requestPermissionList = ArrayList<String>()
            for (i in permissions) {
                if (ContextCompat.checkSelfPermission(context, i) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionList.add(i)
                }
            }
            return requestPermissionList.toTypedArray()
        }
    }
}