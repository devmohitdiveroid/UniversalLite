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
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.diveroid.camera.R
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class GL2JNIActivity : Activity() {
    private var mLayout: View? = null
    var mView: GL2JNIView? = null
    var running = false
    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,
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

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.activity_gl2_jni2)
        val layoutAll: ConstraintLayout = findViewById(R.id.layoutAll)

        // 화면 켜진 상태를 유지합니다.
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        mView = GL2JNIView(this)
        val params = WindowManager.LayoutParams()
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutAll.addView(mView, 0, params)
        mLayout = findViewById(R.id.layoutAll)
        GL2JNILib.mContext = this
        Companion.assets = assets
        GL2JNILib.onCreate(Companion.assets)
        var bitmap: Bitmap? = null
        try {
            //GL2JNILib.insertUIImage("recording", bitmap);
            bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            bitmap.eraseColor(Color.WHITE)
            canvas.drawColor(0xfff)

            //  WallpaperManager wall = WallpaperManager.getInstance(this);
            //  try {
            //      wall.setBitmap(bitmap);
            //  } catch (IOException e) {
            //      e.printStackTrace();
            //  }


            //private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();
            val OUTPUT_DIR = Environment.getExternalStorageDirectory()

            // hard-coded output directory.
            val outputPath = File(OUTPUT_DIR,
                    "result_texture.png").toString()
                GL2JNILib.startImage(bitmap, bitmap.width, bitmap.height, outputPath)
        } catch (e: Exception) {
        }


        /*
            crete Event
        */run {
            val editTextRfromG = findViewById<View>(R.id.editTextRfromG) as EditText
            val seekBarRfromG = findViewById<View>(R.id.seekBarRfromG) as SeekBar
            val value = seekBarRfromG.progress.toFloat() / 100.toFloat()
            editTextRfromG.setText(value.toString())
            GL2JNILib.setValueRfromG(value)
            seekBarRfromG.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val value = progress.toFloat() / 100.toFloat()
                    editTextRfromG.setText(value.toString())
                    GL2JNILib.setValueRfromG(value)
                }
            })
        }
        run {
            val editTextRfromG = findViewById<View>(R.id.editTextRfromB) as EditText
            val seekBarRfromG = findViewById<View>(R.id.seekBarRfromB) as SeekBar
            val value = seekBarRfromG.progress.toFloat() / 100.toFloat()
            editTextRfromG.setText(value.toString())
            GL2JNILib.setValueRfromB(value)
            seekBarRfromG.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val value = progress.toFloat() / 100.toFloat()
                    editTextRfromG.setText(value.toString())
                    GL2JNILib.setValueRfromB(value)
                }
            })
        }
        run {
            val editTextRfromG = findViewById<View>(R.id.editTextBreduceB) as EditText
            val seekBarRfromG = findViewById<View>(R.id.seekBarBreduceB) as SeekBar
            val value = seekBarRfromG.progress.toFloat() / 100.toFloat()
            editTextRfromG.setText(value.toString())
            GL2JNILib.setValueBreduceB(value)
            seekBarRfromG.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    val value = progress.toFloat() / 100.toFloat()
                    editTextRfromG.setText(value.toString())
                    GL2JNILib.setValueBreduceB(value)
                }
            })
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonLoadMovie) as Button
            buttonLoadImage.setOnClickListener {
                if (Build.VERSION.SDK_INT < 19) {
                    val intent = Intent()
                    intent.type = "video/mp4"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Select videos"), REQUEST_LOAD_VIDEO)
                } else {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    intent.type = "video/mp4"
                    startActivityForResult(intent, REQUEST_LOAD_VIDEO)
                }
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonIncodeMovie) as Button
            buttonLoadImage.setOnClickListener {
                if (Build.VERSION.SDK_INT < 19) {
                    val intent = Intent()
                    intent.type = "video/mp4"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Select videos"), REQUEST_INCODE_VIDEO)
                } else {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    intent.type = "video/mp4"
                    startActivityForResult(intent, REQUEST_INCODE_VIDEO)
                }
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonStopMovie) as Button
            buttonLoadImage.setOnClickListener(object : View.OnClickListener {
                override fun onClick(arg0: View) {
                    GL2JNILib.stopVideo()
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
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonRealtime) as Button
            buttonLoadImage.setOnClickListener {
                if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    val cameraPermission = ContextCompat.checkSelfPermission(this@GL2JNIActivity, Manifest.permission.CAMERA)
                    val writeExternalStoragePermission = ContextCompat.checkSelfPermission(this@GL2JNIActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (cameraPermission == PackageManager.PERMISSION_GRANTED
                            && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                        startCamera()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this@GL2JNIActivity, REQUIRED_PERMISSIONS[0])
                                || ActivityCompat.shouldShowRequestPermissionRationale(this@GL2JNIActivity, REQUIRED_PERMISSIONS[1])) {
                            Snackbar.make(mLayout!!, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                                    Snackbar.LENGTH_INDEFINITE).setAction("확인") {
                                ActivityCompat.requestPermissions(this@GL2JNIActivity, REQUIRED_PERMISSIONS,
                                        PERMISSIONS_REQUEST_CODE
                                )
                            }.show()
                        } else {
                            // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                            // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                            ActivityCompat.requestPermissions(this@GL2JNIActivity, REQUIRED_PERMISSIONS,
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
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonLoadImage) as Button
            buttonLoadImage.setOnClickListener {
                val i = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(i, RESULT_LOAD_IMAGE)
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonIncodeImage) as Button
            buttonLoadImage.setOnClickListener {
                val i = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(i, RESULT_INCODE_IMAGE)
            }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonOfftime) as Button
            buttonLoadImage.setOnClickListener { stopCamera() }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonSaveImage) as Button
            buttonLoadImage.setOnClickListener { GL2JNILib.saveImage() }
        }
        run {
            val buttonLoadImage = findViewById<View>(R.id.buttonSaveMovie) as Button
            buttonLoadImage.setOnClickListener {
                // GL2JNILib.saveVideo("");
            }
        }
    }

    fun startCamera() {}
    fun stopCamera() {
        findViewById<View>(R.id.laytoutOfftimeMode).visibility = View.VISIBLE
        findViewById<View>(R.id.laytoutRealMode).visibility = View.GONE
        GL2JNILib.stopRealtimeView()
    }

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
            val bitmap = BitmapFactory.decodeFile(picturePath)
            if (bitmap == null) {
                Log.e("Bitmap", "Bitmap == null")
            } else {
                Log.d("Bitmap", "Bitmap is Loaded ")
                //  GL2JNILib.setTexture(bitmap);
                val w = bitmap.width
                val h = bitmap.height
                GL2JNILib.startImage(bitmap, w, h, "")
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
                val w = bitmap.width
                val h = bitmap.height
                GL2JNILib.startImage(bitmap, w, h, "")
            }

            //  ImageView imageView = (ImageView) findViewById(R.id.imgView);
            //  imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        mView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        mView!!.onResume()
        Thread {
            running = true
            while (running) {
                try {
                    Thread.sleep(20)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                GL2JNILib.tick()
            }
        }.start()
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
        var assets: AssetManager? = null
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val RESULT_LOAD_IMAGE = 1
        private const val REQUEST_LOAD_VIDEO = 2
        private const val RESULT_INCODE_IMAGE = 11
        private const val REQUEST_INCODE_VIDEO = 12
        @SuppressLint("NewApi")
        fun getPath(context: Context, uri: Uri?): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(
                            split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                          selectionArgs: Array<String>?): String? {
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
        fun isExternalStorageDocument(uri: Uri?): Boolean {
            return "com.android.externalstorage.documents" == uri!!.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        fun isDownloadsDocument(uri: Uri?): Boolean {
            return "com.android.providers.downloads.documents" == uri!!.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        fun isMediaDocument(uri: Uri?): Boolean {
            return "com.android.providers.media.documents" == uri!!.authority
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is Google Photos.
         */
        fun isGooglePhotosUri(uri: Uri?): Boolean {
            return "com.google.android.apps.photos.content" == uri!!.authority
        }
    }
}