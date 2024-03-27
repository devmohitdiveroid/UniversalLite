package com.diveroid.camera.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.diveroid.camera.underfilter.FileUtils2
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object BitmapUtils {
    private const val DIRECTORY_DATA = "data"
    const val DIRECTORY_DIVEROID = "diveroid"
    const val DIRECTORY_STORE_IMAGE = "image"

    private val directoryImagePath: String
        get() {
            return if (FileUtils2.isExternalStorageEnable()) {
                Environment.getExternalStorageDirectory().toString() +
                        File.separator + DIRECTORY_DATA +
                        File.separator + DIRECTORY_DIVEROID +
                        File.separator + DIRECTORY_STORE_IMAGE +
                        File.separator
            } else {
                Application().getExternalFilesDir(null).toString() +
                        File.separator + DIRECTORY_DATA +
                        File.separator + DIRECTORY_DIVEROID +
                        File.separator + DIRECTORY_STORE_IMAGE +
                        File.separator
            }
        }

    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var columnIndex = 0
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, projection, null, null, null)
        if (cursor!!.moveToFirst()) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val result = cursor.getString(columnIndex)
        cursor.close()

        return result
    }

    fun rotate(src: Bitmap, degree: Float): Bitmap {
        // Matrix 객체 생성
        val matrix = Matrix()
        // 회전 각도 셋팅
        matrix.postRotate(degree)
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(
            src, 0, 0, src.width,
            src.height, matrix, true
        )
    }

    fun saveBitmap(path: String, bitmap: Bitmap?) {
        if (bitmap == null) return
        val pictureFile = File(path)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(pictureFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: IOException) {
            Log.d("onlytree2", e.toString())
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return
    }

    @SuppressLint("SimpleDateFormat")
    fun saveBitmapToFile(bitmap: Bitmap): File? {
        val dir = File(directoryImagePath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val imageFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".png"

        val fileCacheItem = File(dir.absolutePath + imageFileName)
        var out: OutputStream? = null

        try {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)

            return fileCacheItem
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return null
    }

    fun getRotatedBitmap(context: Context, contentUri: Uri): Bitmap {
        val imagePath = getRealPathFromURI(context, contentUri) // path ���
        val exif: ExifInterface? = ExifInterface(imagePath)

        val exifOrientation =
            exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val exifDegree = exifOrientationToDegrees(exifOrientation)

        val bitmap = BitmapFactory.decodeFile(imagePath)
        return rotate(bitmap, exifDegree.toFloat())
    }

    fun getRotatedBitmap(imgPath: String): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imgPath)
        val exif: ExifInterface? = ExifInterface(imgPath)

        val exifOrientation: Int
        val exifDegree: Int

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            exifDegree = exifOrientationToDegrees(exifOrientation)
        } else {
            exifDegree = 0
        }

        return rotate(bitmap, exifDegree.toFloat())
    }

    fun getRotatedBitmapPath(imgPath: String): String? {
        return saveBitmapToFile(getRotatedBitmap(imgPath))?.absolutePath
    }

    fun getRotatedBitmapPath(context: Context, contentUri: Uri): String? {
        return saveBitmapToFile(getRotatedBitmap(context, contentUri))?.absolutePath
    }

    fun bitmapToUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleRatio = newWidth.toFloat() / width
        val matrix = Matrix()
        matrix.postScale(scaleRatio, scaleRatio)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }


    fun bitmapToBase64(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val byteArrayImage = baos.toByteArray()
        return Base64.encodeToString(byteArrayImage, Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val bytePlainOrg = Base64.decode(base64String, Base64.NO_WRAP)
            val inStream = ByteArrayInputStream(bytePlainOrg)
            BitmapFactory.decodeStream(inStream)
        } catch (e: java.lang.Exception) {
            null
        }
    }


    fun bitmapToRoundDrawable(context: Context, bitmap: Bitmap): RoundedBitmapDrawable {
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        roundedBitmapDrawable.cornerRadius = 18.0f
        return roundedBitmapDrawable
    }
}

