package com.diveroid.camera.mediaCtrl

import android.app.usage.ExternalStorageStats
import android.content.Context
import android.os.Environment
import android.util.Log
import com.diveroid.camera.underfilter.FileUtils2
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by junki-mac on 2017. 1. 11..
 *
 * getLocalMediaPath(  fileName, false ) -> 앱내 저장되는 원본 워치
 * getLocalMediaPath(  fileName, true ) -> 앱내 저장되는 썸네일 위치
 *
 */
object MediaFileControl {
    private const val TAG = "MediaFileControl"
    const val SIZE_THUMBNAIL_X = 720
    const val SIZE_THUMBNAIL_Y = 480
    private var userDirectoryPath = ""
    private var cookieThumbnailDirPath = ""
    private var cookieOriginDirPath = ""
    private var tmpThumbnailDirPath = ""
    private var tmpOriginDirPath = ""
    const val EXPORT_FOLDER_NAME = "DIVEROID"

    /**
     * 1. User 이메일별로 사진영상을 저장하기 떄문에 로그인 이후에 사용되어야 한다.
     * 2. Permission이 제대로 완료된 뒤에 처리되어야 한다.
     */
    fun init(context: Context, userEmail: String = "temp") {
//        Glide.get(DiveroidApplication.instance).setMemoryCategory(MemoryCategory.LOW)
//        if (isLogined) {
//            val (_, _, email) = getInstance().getUser()
//            loginEmail = email
//        }

        //leess
//        if(FileUtils2.isExternalStorageEnable()) {//기존 : sdcard/DIVEROID/*
//            cookieThumbnailDirPath = Environment.getExternalStorageDirectory().toString() + "/DIVEROID/.media/thumbs/" + userEmail
//            cookieOriginDirPath = Environment.getExternalStorageDirectory().toString() + "/DIVEROID/.media/origins/" + userEmail
//            userDirectoryPath = Environment.getExternalStorageDirectory().toString() + "/DIVEROID/.user/" + userEmail
//            tmpThumbnailDirPath = Environment.getExternalStorageDirectory().toString() + "/DIVEROID/.media/tmp/" + userEmail + "/thumbs"
//            tmpOriginDirPath = Environment.getExternalStorageDirectory().toString() + "/DIVEROID/.media/tmp/" + userEmail + "/origins"
//        } else {//변경 : sdcard/data/패키지명/files/*
            cookieThumbnailDirPath = context.getExternalFilesDir(null).toString() + "/media/thumbs/" + userEmail
            cookieOriginDirPath = context.getExternalFilesDir(null).toString() + "/media/origins/" + userEmail
            userDirectoryPath = context.getExternalFilesDir(null).toString() + "/user/" + userEmail
            tmpThumbnailDirPath = context.getExternalFilesDir(null).toString() + "/media/tmp/" + userEmail + "/thumbs"
            tmpOriginDirPath = context.getExternalFilesDir(null).toString() + "/media/tmp/" + userEmail + "/origins"
//        }

        Log.d(TAG, "init: Environment.getExternalStorageDirectory().absolutePath = ${Environment.getExternalStorageDirectory().absolutePath}")

        Log.d(TAG, "init: cookieThumbnailDirPath = $cookieThumbnailDirPath")
        Log.d(TAG, "init: cookieOriginDirPath = $cookieOriginDirPath")
        Log.d(TAG, "init: userDirectoryPath = $userDirectoryPath")
        Log.d(TAG, "init: tmpThumbnailDirPath = $tmpThumbnailDirPath")
        Log.d(TAG, "init: tmpOriginDirPath = $tmpOriginDirPath")

        checkDirectoryExist()
    }

    private fun checkDirectoryExist() {
        try {
            val initFile = arrayOf(
                    File(cookieThumbnailDirPath),
                    File(cookieOriginDirPath),
                    File(userDirectoryPath),
                    File(tmpThumbnailDirPath),
                    File(tmpOriginDirPath)
            )
            for (temp in initFile) if (!temp.exists() && !temp.mkdirs()) Log.d(TAG, "failed to create directory" + temp.path)
        } catch (e: Exception) {
            Log.e(TAG, "checkDirectoryExist: ${e.message}")
        }
    }

    fun createImgFileName(): String {
        val date = Date()
        date.time = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH)
        return PreFix.IMG_.name + sdf.format(date) + ".jpeg"
    }

    fun createVideoFileName(): String {
        val date = Date()
        date.time = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH)
        return PreFix.VID_.name + sdf.format(date) + ".mp4"
    }

    fun createLogColorGraphFileName(logID: String): String {
        return PreFix.GRAPH_.name + logID + ".jpeg"
    }

    fun deleteMediaFileInLocal(fileName: String?): Boolean {
        val localPath = getLocalMediaPath(fileName, false)
        val thumbPath = getLocalMediaPath(fileName, true)
        var file = File(localPath)
        if (file.exists()) file.delete()
        file = File(thumbPath)
        if (file.exists()) file.delete()
        return true
    }

    fun moveTmpFileToLocal(fileName: String?) {
//        DLog.dd("fileName:"+fileName);
        val tmpLocalPath = getTmpMediaPath(fileName, false)
        val tmpThumbPath = getTmpMediaPath(fileName, true)
        val localPath = getLocalMediaPath(fileName, false)
        val thumbPath = getLocalMediaPath(fileName, true)

//        try {
//            Files.move(Paths.get(tmpLocalPath), Paths.get(localPath));
//            Files.move(Paths.get(tmpThumbPath), Paths.get(thumbPath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        val tmpLocal = File(tmpLocalPath)
        val tmpThumb = File(tmpThumbPath)
        ////        DLog.dd("tmpLocal:"+tmpLocal+", exists:"+tmpLocal.exists());
////        DLog.dd("tmpThumb:"+tmpThumb+", exists:"+tmpThumb.exists());
        val local = File(localPath)
        val thumb = File(thumbPath)
        //
        tmpLocal.renameTo(local)
        tmpThumb.renameTo(thumb)
        //        DLog.dd("local:"+local+", exists:"+local.exists());
//        DLog.dd("thumb:"+thumb+", exists:"+thumb.exists());
    }

    fun moveLocalFileToTmp(fileName: String?) {
//        DLog.dd("fileName:"+fileName);
        val localPath = getLocalMediaPath(fileName, false)
        val thumbPath = getLocalMediaPath(fileName, true)
        val tmpLocalPath = getTmpMediaPath(fileName, false)
        val tmpThumbPath = getTmpMediaPath(fileName, true)


//        try {
//            Files.move(Paths.get(localPath), Paths.get(tmpLocalPath));
//            Files.move(Paths.get(thumbPath), Paths.get(tmpThumbPath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        val local = File(localPath)
        val thumb = File(thumbPath)
        //        DLog.dd("local:"+local+", exists:"+local.exists());
//        DLog.dd("thumb:"+thumb+", exists:"+thumb.exists());
        val tmpLocal = File(tmpLocalPath)
        val tmpThumb = File(tmpThumbPath)
        //
        local.renameTo(tmpLocal)
        thumb.renameTo(tmpThumb)
        //        DLog.dd("tmpLocal:"+tmpLocal+", exists:"+tmpLocal.exists());
//        DLog.dd("tmpThumb:"+tmpThumb+", exists:"+tmpThumb.exists());
    }

    fun deleteAllTmpFile(): Boolean {
        try {
            val localPath = tmpOriginDirPath
            val tmpLocal = File(localPath)
            val tmpLocalChildFileList = tmpLocal.listFiles()
            if(tmpLocalChildFileList != null) {
                for (childFile in tmpLocalChildFileList) {
                    childFile.delete()
                }
            }
            val thumbPath = tmpThumbnailDirPath
            val tmpThumb = File(thumbPath)
            val tmpThumbChildFileList = tmpThumb.listFiles()
            if(tmpThumbChildFileList != null) {
                for (childFile in tmpThumbChildFileList) {
                    childFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getLocalMediaPath(fileName: String?, isThumbnail: Boolean): String? {
        if (fileName == null) return null
        return if (isThumbnail) cookieThumbnailDirPath + "/" + fileName.replace(".mp4", ".jpeg") else "$cookieOriginDirPath/$fileName"
    }

    fun getTmpMediaPath(fileName: String?, isThumbnail: Boolean): String? {
        if (fileName == null) return null
        return if (isThumbnail) tmpThumbnailDirPath + "/" + fileName.replace(".mp4", ".jpeg") else "$tmpOriginDirPath/$fileName"
    }

    fun getLogGraphPath(fileName: String): String {
        return "$cookieThumbnailDirPath/$fileName"
    }

    internal enum class PreFix {
        IMG_, VID_, GRAPH_
    }
}