/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diveroid.camera.underfilter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.diveroid.camera.underfilter.GL2JNICamera2.getAvailableHighSpeedUniverse
import com.diveroid.camera.underfilter.GL2JNICamera2.getHighSpeedResolution
import com.diveroid.camera.underfilter.GL2JNICamera2.hasBackspaceWideCamera
import com.diveroid.camera.R
import com.google.android.material.snackbar.Snackbar
import java.io.File

/**
 * 김윤태님 작업하던화면, 사용하지 않습니다.
 * 단!!! 몇가지 static으로 사용하고 있는 변수들의 정리가 필요합니다.
 */
@Deprecated("")
class GL2MainActivity : Activity() {
    private val mLayout: View? = null

    // GL2JNIView mView;
    var running = false
    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

    inner class Callback : GL2JNILib.Callback() {
        fun GL2JNIVideoFinished(savedFile: String?) {
            runOnUiThread(object : Runnable {
                override fun run() {
                    run {
                        val buttonLoadMovie = findViewById<Button>(R.id.buttonLoadMovie)
                        buttonLoadMovie.visibility = View.VISIBLE
                    }
                    run {
                        val buttonIncodeMovie = findViewById<Button>(R.id.buttonIncodeMovie)
                        buttonIncodeMovie.visibility = View.VISIBLE
                    }
                    run {
                        val buttonStopMovie = findViewById<Button>(R.id.buttonStopMovie)
                        buttonStopMovie.visibility = View.GONE
                    }
                }
            })
        }
    }

    fun updateFrameRadio() {
        val rg_btnuhd = findViewById<View>(R.id.rg_btnuhd) as RadioButton
        val rg_btnqhd = findViewById<View>(R.id.rg_btnqhd) as RadioButton
        val rg_btnfhd = findViewById<View>(R.id.rg_btnfhd) as RadioButton
        val rg_btnu30 = findViewById<View>(R.id.rg_btnu30) as RadioButton
        val rg_btnq60 = findViewById<View>(R.id.rg_btnq60) as RadioButton
        val rg_btnNoWide = findViewById<View>(R.id.rg_btnNoWide) as RadioButton
        val rg_btnWide = findViewById<View>(R.id.rg_btnWide) as RadioButton
        val radioGroupResolution = findViewById<View>(R.id.radioGroupResolution) as RadioGroup


        // rg_btnNoWide.setChecked(false);
        //  rg_btnWide.setChecked(false);
        val isFront = (findViewById<View>(R.id.front_btn) as RadioButton).isChecked //.get;
        val is60 = rg_btnq60.isChecked //.get;
        val isWide = if ((findViewById<View>(R.id.rg_btnWide) as RadioButton).isChecked) 1 else 0 //.get;
        if (is60) {
            run {
                rg_btnuhd.visibility = View.INVISIBLE
                rg_btnuhd.isChecked = false
                var arrSpeed: Array<Range<Int>>? = null
                arrSpeed = getAvailableHighSpeedUniverse(this@GL2MainActivity, isFront, isWide, 3840, 2160)
                if (arrSpeed != null) {
                    for (i in arrSpeed.indices) {
                        if (arrSpeed[i].lower >= 60) {
                            rg_btnuhd.visibility = View.VISIBLE
                        }
                    }
                }
            }
            run {
                rg_btnqhd.visibility = View.INVISIBLE
                rg_btnqhd.isChecked = false
                var arrSpeed: Array<Range<Int>>? = null
                arrSpeed = getAvailableHighSpeedUniverse(this@GL2MainActivity, isFront, isWide, 2560, 1440)
                if (arrSpeed != null) {
                    for (i in arrSpeed!!.indices) {
                        if (arrSpeed!![i].lower >= 60) {
                            rg_btnqhd.visibility = View.VISIBLE
                        }
                    }
                }
            }
            run {

                //  Range<Integer>[] arrSpeed = GL2JNICamera2.getAvailableHighSpeed(GL2MainActivity.this, 1920, 1080);
                rg_btnfhd.visibility = View.INVISIBLE
                rg_btnfhd.isChecked = false
                var arrSpeed: Array<Range<Int>>? = null
                arrSpeed = getAvailableHighSpeedUniverse(this@GL2MainActivity, isFront, isWide, 1920, 1080)
                if (arrSpeed != null) {
                    for (i in arrSpeed!!.indices) {
                        if (arrSpeed!![i].lower >= 60) {
                            rg_btnfhd.visibility = View.VISIBLE
                        }
                    }
                }
            }
        } else {
            rg_btnuhd.visibility = View.VISIBLE
            rg_btnqhd.visibility = View.VISIBLE
            rg_btnfhd.visibility = View.VISIBLE


            //rg_btnuhd.setChecked(false);
            //rg_btnqhd.setChecked(false);
            //rg_btnqhd.setChecked(false);
            radioGroupResolution.clearCheck()
        }
    }

    fun gotoRealtime() {
//        final RadioButton rg_btn1 = (RadioButton) findViewById(R.id.rg_btn1);
//        final RadioButton rg_btn2 = (RadioButton) findViewById(R.id.rg_btn2);
//
//
//        final RadioButton front_btn = (RadioButton) findViewById(R.id.front_btn);
//        final RadioButton back_btn = (RadioButton) findViewById(R.id.back_btn);
//
//
//        final RadioButton rg_btnuhd = (RadioButton) findViewById(R.id.rg_btnuhd);
//        final RadioButton rg_btnqhd = (RadioButton) findViewById(R.id.rg_btnqhd);
//        final RadioButton rg_btnfhd = (RadioButton) findViewById(R.id.rg_btnfhd);
//        final RadioButton rg_btnu30 = (RadioButton) findViewById(R.id.rg_btnu30);
//        final RadioButton rg_btnq60 = (RadioButton) findViewById(R.id.rg_btnq60);
//
//
//
//        final RadioButton rg_btnNoWide = (RadioButton) findViewById(R.id.rg_btnNoWide);
//        final RadioButton rg_btnWide = (RadioButton) findViewById(R.id.rg_btnWide);
//
//
//        if( rg_btn1.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_HIGH_MODE_ON = true;
//
//        }else if( rg_btn2.isChecked() )
//        {
//
//            GL2PreviewOption.PREPARE_HIGH_MODE_ON = false;
//        }else
//        {
//            Toast.makeText(GL2MainActivity.this, "고성능 옵션을 션택해 주세요", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//        if( front_btn.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_IS_SELFIE_MODE = true;
//
//        }else if( back_btn.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_IS_SELFIE_MODE = false;
//        }else
//        {
//            Toast.makeText(GL2MainActivity.this, "카메레 앞뒤를 선택해 주세요", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//        if( rg_btnNoWide.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_WIDE_MODE = 0;
//
//        }else if( rg_btnWide.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_WIDE_MODE = 1;
//        }else
//        {
//            Toast.makeText(GL2MainActivity.this, "Wide 여부를 선택 해 주세요.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//
//
//
//
//
//        if( rg_btnuhd.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_HIGH_WIDTH = 3840;
//            GL2PreviewOption.PREPARE_HIGH_HEIGHT = 2160;
//
//        }else if( rg_btnqhd.isChecked() )
//        {
//
//            GL2PreviewOption.PREPARE_HIGH_WIDTH = 2560;
//            GL2PreviewOption.PREPARE_HIGH_HEIGHT = 1440;
//        }else if( rg_btnfhd.isChecked() )
//        {
//
//            GL2PreviewOption.PREPARE_HIGH_WIDTH = 1920;
//            GL2PreviewOption.PREPARE_HIGH_HEIGHT = 1080;
//        }else
//        {
//            Toast.makeText(GL2MainActivity.this, "카메레 해상도를 선택해 주세요", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//
//        if( rg_btnu30.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_HIGH_FRAME = 30;
//        }else   if( rg_btnq60.isChecked() )
//        {
//            GL2PreviewOption.PREPARE_HIGH_FRAME = 60;
//        }else
//        {
//            Toast.makeText(GL2MainActivity.this, "카메레 프레임를 선택해 주세요", Toast.LENGTH_SHORT).show();
//            return;
//
//        }
//        GL2RealtimeActivity.mCaptureWidth = 1920
//        GL2RealtimeActivity.mCaptureHeight = 1080
//        val intent = Intent(this@GL2MainActivity, GL2RealtimeActivity::class.java)
//        startActivity(intent)
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        createNeedDirectory()
        setContentView(R.layout.activity_gl2_main)


        // 화면 켜진 상태를 유지합니다.
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            val cameraPermission = ContextCompat.checkSelfPermission(this@GL2MainActivity, Manifest.permission.CAMERA)
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(this@GL2MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@GL2MainActivity, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this@GL2MainActivity, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout!!, "이 앱을 실행하려면 카메라와 마이크, 외부 저장소 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인") {
                        ActivityCompat.requestPermissions(this@GL2MainActivity, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE
                        )
                    }.show()
                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions(this@GL2MainActivity, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE
                    )
                }
            }
        } else {
            val snackbar = Snackbar.make(mLayout!!, "디바이스가 카메라를 지원하지 않습니다.",
                    Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("확인") { snackbar.dismiss() }
            snackbar.show()
        }
        val rg_btnuhd = findViewById<View>(R.id.rg_btnuhd) as RadioButton
        val rg_btnqhd = findViewById<View>(R.id.rg_btnqhd) as RadioButton
        val rg_btnfhd = findViewById<View>(R.id.rg_btnfhd) as RadioButton
        val rg_btnu30 = findViewById<View>(R.id.rg_btnu30) as RadioButton
        val rg_btnq60 = findViewById<View>(R.id.rg_btnq60) as RadioButton
        val front_btn = findViewById<View>(R.id.front_btn) as RadioButton
        val back_btn = findViewById<View>(R.id.back_btn) as RadioButton
        val rg_btnNoWide = findViewById<View>(R.id.rg_btnNoWide) as RadioButton
        val rg_btnWide = findViewById<View>(R.id.rg_btnWide) as RadioButton
        run {
            front_btn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(arg0: View) {
                    run {
                        rg_btnNoWide.isChecked = true
                        rg_btnWide.isChecked = false
                        rg_btnWide.visibility = View.INVISIBLE
                    }
                    rg_btnuhd.isChecked = false
                    rg_btnqhd.isChecked = false
                    rg_btnfhd.isChecked = false
                    updateFrameRadio()
                }
            })
        }
        run {
            back_btn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(arg0: View) {
                    run {
                        if (hasBackspaceWideCamera(this@GL2MainActivity)) {
                            rg_btnWide.visibility = View.VISIBLE
                        } else {
                            rg_btnNoWide.isChecked = true
                            rg_btnWide.isChecked = false
                            rg_btnWide.visibility = View.INVISIBLE
                        }
                    }
                    rg_btnuhd.isChecked = false
                    rg_btnqhd.isChecked = false
                    rg_btnfhd.isChecked = false
                    updateFrameRadio()
                }
            })
        }
        run {
            rg_btnu30.setOnClickListener {
                rg_btnuhd.visibility = View.VISIBLE
                rg_btnqhd.visibility = View.VISIBLE
                rg_btnfhd.visibility = View.VISIBLE
                updateFrameRadio()
            }
        }
        run {
            rg_btnNoWide.setOnClickListener {
                rg_btnuhd.visibility = View.VISIBLE
                rg_btnqhd.visibility = View.VISIBLE
                rg_btnfhd.visibility = View.VISIBLE
                updateFrameRadio()
            }
        }
        run {
            rg_btnWide.setOnClickListener {
                rg_btnuhd.visibility = View.VISIBLE
                rg_btnqhd.visibility = View.VISIBLE
                rg_btnfhd.visibility = View.VISIBLE
                updateFrameRadio()
            }
        }
        run {
            rg_btnu30.setOnClickListener {
                rg_btnuhd.visibility = View.VISIBLE
                rg_btnqhd.visibility = View.VISIBLE
                rg_btnfhd.visibility = View.VISIBLE
                updateFrameRadio()
            }
        }
        run {
            rg_btnq60.setOnClickListener {
                rg_btnuhd.visibility = View.INVISIBLE
                rg_btnqhd.visibility = View.INVISIBLE
                rg_btnfhd.visibility = View.INVISIBLE
                updateFrameRadio()
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonModeRealtimeFHDCurrent) as Button
            buttonLoadImage.setOnClickListener {
                GL2JNILib.USE_RENDERTEXTURE = true
                gotoRealtime()
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonModeRealtimeFHDTest) as Button
            buttonLoadImage.setOnClickListener {
                GL2JNILib.USE_RENDERTEXTURE = false
                gotoRealtime()
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonModeMovie) as Button
            buttonLoadImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                intent.type = "video/mp4"
                startActivityForResult(intent, REQUEST_LOAD_VIDEO)
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonModeImage) as Button
            buttonLoadImage.setOnClickListener {
                val i = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(i, RESULT_LOAD_IMAGE)
            }
        }
    }

    private fun startCamera() {
        val cameraID = Camera.CameraInfo.CAMERA_FACING_BACK
        try {
            val mCamera = Camera.open(cameraID) // attempt to get a Camera instance
            val parameters = mCamera.parameters

            // android.hardware.camera2.CameraAccessException
            var sz = parameters.supportedVideoSizes //.getSupportedPictureSizes();//.getSupportedPreviewSizes();
            var content = "Camera 1(old android)  \n VideoSize \r\n"
            for (i in sz.indices) {
                content += """${sz[i].width} X ${sz[i].height}
"""
            }
            sz = parameters.supportedPreviewSizes //.getSupportedPictureSizes();//.getSupportedPreviewSizes();
            content += "PreviewSize \r\n"
            for (i in sz.indices) {
                content += """${sz[i].width} X ${sz[i].height}
"""
            }


            //  StreamConfigurationMap streamConfigurationMap = cameraCharacterizsics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            content = getHighSpeedResolution(this)
            (findViewById<View>(R.id.editTextResolution) as EditText).setText(content)
            content = "Camera 2(android 5.0)  \n VideoSize \r\n"
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId2 = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId2)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            val imageDimension = map.getOutputSizes(SurfaceTexture::class.java)
            mCameraSize = imageDimension[0]
            for (size in imageDimension) {
                content += """${size.width} X ${size.height}
"""
            }
            (findViewById<View>(R.id.editTextResolution2) as EditText).setText(content)
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
//            Log.e(TAG, "Camera " + cameraID + " is not available: " + e.message)
        }
    }

    fun computeCameraSize() {
        val cameraID = Camera.CameraInfo.CAMERA_FACING_BACK
        try {
            val mCamera = Camera.open(cameraID) // attempt to get a Camera instance
            val parameters = mCamera.parameters

            // android.hardware.camera2.CameraAccessException
            var sz = parameters.supportedVideoSizes //.getSupportedPictureSizes();//.getSupportedPreviewSizes();
            var content = "Camera 1(old android)  \n VideoSize \r\n"
            for (i in sz.indices) {
                content += """${sz[i].width} X ${sz[i].height}
"""
            }
            sz = parameters.supportedPreviewSizes //.getSupportedPictureSizes();//.getSupportedPreviewSizes();
            content += "PreviewSize \r\n"
            for (i in sz.indices) {
                content += """${sz[i].width} X ${sz[i].height}
"""
            }


            //  StreamConfigurationMap streamConfigurationMap = cameraCharacterizsics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            (findViewById<View>(R.id.editTextResolution) as EditText).setText(content)
            content = "Camera 2(android 5.0)  \n VideoSize \r\n"
            val manager = getSystemService(CAMERA_SERVICE) as CameraManager
            val cameraId2 = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId2)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            val imageDimension = map.getOutputSizes(SurfaceTexture::class.java)
            mCameraSize = imageDimension[0]


            // for (Size size : imageDimension) {

            //    content += size.getWidth() + " X " + size.getHeight() + "\r\n";


            //}


            // ((EditText)findViewById(R.id.editTextResolution2)).setText(content);
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
//            Log.e(TAG, "Camera " + cameraID + " is not available: " + e.message)
        }
    }

    fun stopCamera() {}
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grandResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {
            var check_result = true
            for (result in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {
                startCamera()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout!!, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인") { finish() }.show()
                } else {
                    Snackbar.make(mLayout!!, "설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인") { finish() }.show()
                }
            }
        }
    }

    // UPDATED!
    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    private val SAMPLE = "" //Environment.getExternalStorageDirectory() + "/video.mp4";
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOAD_VIDEO && resultCode == RESULT_OK && null != data) {
            selectedVideos = getSelectedVideos(requestCode, data)
            Log.d("path", selectedVideos.toString())

            //GL2JNILib.startVideo(selectedVideos.get(0) , "");
            // videoView.setVideoPath(selectedVideos.get(0));
            //videoView.requestFocus();
            //videoView.start();

            //if(selectedVideos.size() > 1) {
            //   videoView2.setVideoPath(selectedVideos.get(1));
            //   videoView2.requestFocus();
            //   videoView2.start();
            //}
            var filePath = selectedVideos.toString()
            filePath = filePath.replace("[", "")
            filePath = filePath.replace("]", "")


            // GL2VideoActivity.isMovieMode = true;
            // GL2VideoActivity.videoModeFilePath = filePath;//selectedVideos.toString();
//            GL2Controller.getInstance().showMovie(this, null)
            //Intent intent=new Intent(GL2MainActivity.this, GL2VideoActivity.class);
            //intent.putExtra("file", filePath);
            //startActivity(intent);
        } else if (requestCode == REQUEST_INCODE_VIDEO && resultCode == RESULT_OK && null != data) {
            selectedVideos = getSelectedVideos(requestCode, data)
            Log.d("path", selectedVideos.toString())
            GL2JNILib.startVideo(selectedVideos!![0]!!, "")
            // videoView.setVideoPath(selectedVideos.get(0));
            //videoView.requestFocus();
            //videoView.start();

            //if(selectedVideos.size() > 1) {
            //   videoView2.setVideoPath(selectedVideos.get(1));
            //   videoView2.requestFocus();
            //   videoView2.start();
            //}
            run {
                val buttonLoadMovie = findViewById<Button>(R.id.buttonLoadMovie)
                buttonLoadMovie.visibility = View.VISIBLE
            }
            run {
                val buttonIncodeMovie = findViewById<Button>(R.id.buttonIncodeMovie)
                buttonIncodeMovie.visibility = View.VISIBLE
            }
            run {
                val buttonStopMovie = findViewById<Button>(R.id.buttonStopMovie)
                buttonStopMovie.visibility = View.GONE
            }
        } else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            val op = BitmapFactory.Options()
            //op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            var bitmap = BitmapFactory.decodeFile(picturePath, op)

            //bitmap.
            // b//itmap.getConfig().
            //if( bitmap.getConfig())
            bitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            if (bitmap == null) {
                Log.e("Bitmap", "Bitmap == null")
            } else {


                // Log.d("Bitmap", "Bitmap is Loaded ");
                //  GL2JNILib.setTexture(bitmap);

                //int w  = bitmap.getWidth();
                // int h = bitmap.getHeight();


                //   GL2JNILib.startImage(bitmap,  w,h, "");


                /*--------------------------------------------
                    비트맵의 회전값을 구해온다.
                ---------------------------------------------*/
                var bitmapOrientation = ExifInterface.ORIENTATION_NORMAL
                bitmapOrientation = try {
                    val exif = ExifInterface(picturePath)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL) //ORIENTATION_UNDEFINED);
                } catch (e: Exception) {
                    Log.e("ImagePicker", e.message + e.stackTrace)
                    // returnEmptyImage(photoPath, ident1, ident2, loadType);
                    // returnEmptyImage(photoPath, ident1, ident2, loadType);
                    return
                }


                //int photoW = bmOptions.outWidth;
                //int photoH = bmOptions.outHeight;
                val scaleFactor = 1

                //  if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                //          bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                /*-------------------------------------------------
                이미지에 회전값이 있으면 넓이와 높이를 반대로 넣어서 계산한다.
                -------------------------------------------------*/
                //   scaleFactor = calculateInSampleSize(bmOptions, targetH, targetW);
                // } else {
                //     scaleFactor = calculateInSampleSize(bmOptions, targetW, targetH);
                // }

//
                //  bitmap.getConfig();
                var mCapturedBitmap: Bitmap? = null
                mCapturedBitmap = if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    val matrix = Matrix()
                    matrix.postRotate(180f)
                    //if (testRescale) {
                    //    matrix.postScale(testScale, testScale);
                    //}
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    //if (testRescale) {
                    //    matrix.postScale(testScale, testScale);
                    //}
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    //i//f (testRescale) {
                    //   Matrix matrix = new Matrix();
                    //   matrix.postScale(testScale, testScale);
                    //   mCapturedBitmap = bitmap;//.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    //}
                    bitmap
                }


                //   GL2ImageActivity.isMovieMode = false;
                GL2ImageActivity.imageModeBitmap = mCapturedBitmap
                val intent = Intent(this@GL2MainActivity, GL2ImageActivity::class.java)
                startActivity(intent)
            }

            //  ImageView imageView = (ImageView) findViewById(R.id.imgView);
            //  imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        } else if (requestCode == RESULT_INCODE_IMAGE && resultCode == RESULT_OK && null != data) {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage!!,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()
            val bitmap = BitmapFactory.decodeFile(picturePath)
            if (bitmap == null) {
                Log.e("Bitmap", "Bitmap == null")
            } else {
                Log.d("Bitmap", "Bitmap is Loaded ")
                //  GL2JNILib.setTexture(bitmap);


                /*--------------------------------------------
                    비트맵의 회전값을 구해온다.
                ---------------------------------------------*/
                var bitmapOrientation = ExifInterface.ORIENTATION_NORMAL
                bitmapOrientation = try {
                    val exif = ExifInterface(picturePath)
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL) //ORIENTATION_UNDEFINED);
                } catch (e: Exception) {
                    Log.e("ImagePicker", e.message + e.stackTrace)
                    // returnEmptyImage(photoPath, ident1, ident2, loadType);
                    // returnEmptyImage(photoPath, ident1, ident2, loadType);
                    return
                }


                //int photoW = bmOptions.outWidth;
                //int photoH = bmOptions.outHeight;
                val scaleFactor = 1

                //  if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                //          bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                /*-------------------------------------------------
                이미지에 회전값이 있으면 넓이와 높이를 반대로 넣어서 계산한다.
                -------------------------------------------------*/
                //   scaleFactor = calculateInSampleSize(bmOptions, targetH, targetW);
                // } else {
                //     scaleFactor = calculateInSampleSize(bmOptions, targetW, targetH);
                // }
                var mCapturedBitmap: Bitmap? = null
                mCapturedBitmap = if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
                    val matrix = Matrix()
                    matrix.postRotate(180f)
                    //if (testRescale) {
                    //    matrix.postScale(testScale, testScale);
                    //}
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else if (bitmapOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    //if (testRescale) {
                    //    matrix.postScale(testScale, testScale);
                    //}
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    //i//f (testRescale) {
                    //   Matrix matrix = new Matrix();
                    //   matrix.postScale(testScale, testScale);
                    //   mCapturedBitmap = bitmap;//.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    //}
                    bitmap
                }
                val w = mCapturedBitmap!!.width
                val h = mCapturedBitmap.height
                GL2JNILib.startImage(mCapturedBitmap, w, h, "")
            }

            //  ImageView imageView = (ImageView) findViewById(R.id.imgView);
            //  imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        //  mView.onPause();
    }

    override fun onResume() {
        super.onResume()
        // mView.onResume();
    }

    override fun onDestroy() {
        GL2JNILib.onDestroy()
        super.onDestroy()
    }

    /*-----------------------
    MovieSelect Util
    ------------------------ */
    private var selectedVideos: List<String?>? = null
    private fun getSelectedVideos(requestCode: Int, data: Intent): List<String?> {
        val result: MutableList<String?> = ArrayList()
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val videoItem = clipData.getItemAt(i)
                val videoURI = videoItem.uri
                val filePath = getPath(this, videoURI)
                result.add(filePath)
            }
        } else {
            val videoURI = data.data
            val filePath = getPath(this, videoURI)
            result.add(filePath)
        }
        return result
    }

    companion object {
//        fun GetSavePath(ext: String): String {
//            val m_path = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_DCIM).absolutePath
//            val date = Date()
//            val random = Random()
//            val filename = "new" + date.year + "_" + date.month + "_" + date.day + "_" + date.hours + "_" + date.minutes + "_" + date.seconds + random.nextInt(9999) + "." + ext
//            return "$m_path/Diveroid/RedFilter/$filename"
//        }

        var assets: AssetManager? = null
        private const val PERMISSIONS_REQUEST_CODE = 100
        var mCameraSize: Size? = null
        fun createNeedDirectory() {
            var m_path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).absolutePath
            run {
                m_path += "/Diveroid"
                val wallpaperDirectory = File(m_path)
                // have the object build the directory structure, if needed.
                wallpaperDirectory.mkdirs()
            }
            run {
                m_path += "/RedFilter"
                val wallpaperDirectory = File(m_path)
                // have the object build the directory structure, if needed.
                wallpaperDirectory.mkdirs()
            }
        }

        private const val RESULT_LOAD_IMAGE = 1
        private const val REQUEST_LOAD_VIDEO = 2
        private const val RESULT_INCODE_IMAGE = 11
        private const val REQUEST_INCODE_VIDEO = 12
        @SuppressLint("NewApi")
        fun getPath(context: Context?, uri: Uri?): String? {
            try {
                val file = FileUtils2.getFileFromUri(context!!, uri!!)
                return file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
//
//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
//
//                // TODO handle non-primary volumes
//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[] {
//                        split[1]
//                };
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }else
//            {
//
//            }
//        }
//        // MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//
//            // Return the remote address
//            if (isGooglePhotosUri(uri))
//                return uri.getLastPathSegment();
//
//            return getDataColumn(context, uri, null, null);
//        }
//        // File
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
            return null
        }

        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String?>?): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(
                    column
            )
            try {
                cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs,
                        null)
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }
    }
}