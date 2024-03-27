package com.diveroid.lite.data

import android.content.ContentValues
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

data class MediaData(
    var mediaId: Long,                      // 미디어 ID
    var logId: Long = -1L,                  // 로그 ID
    var fileType: String? = null,           // 파일타입 (IMAGE, VIDEO)
    var fileName: String? = null,           // 파일경로
    var thumbName: String? = null,          // 썸네일경로
    var videoTime: String? = null,          // 동영상 길이(분:초)
    var latitude: Float? = null,            // 위도
    var longitude: Float? = null,           // 경도
    var altitude: Float? = null,            // 고도
    var createDate: String? = null,         // 생성일시 (yyyyMMddHHmmss)
    @SerializedName(COLUMN_FILTERED)
    var filtered: Int = 0                   // 필터 적용 여부 (0/1)
) {
    fun toContentValue(): ContentValues {
        return ContentValues().apply {
            if (mediaId > 0) {
                put(COLUMN_MEDIA_ID, mediaId)
            }
            if (logId > 0) {
                put(COLUMN_LOG_ID, logId)
            }
            if (fileType?.isNotEmpty() == true) {
                put(COLUMN_FILE_TYPE, fileType)
            }
            if (fileName?.isNotEmpty() == true) {
                put(COLUMN_FILE_NAME, fileName)
            }
            if (thumbName?.isNotEmpty() == true) {
                put(COLUMN_THUMB_NAME, thumbName)
            }
            if (videoTime?.isNotEmpty() == true) {
                put(COLUMN_VIDEO_TIME, videoTime)
            }
            if (latitude != null) {
                put(COLUMN_LATITUDE, latitude)
            }
            if (longitude != null) {
                put(COLUMN_LONGITUDE, longitude)
            }
            if (altitude != null) {
                put(COLUMN_ALTITUDE, altitude)
            }
            if (createDate?.isNotEmpty() == true) {
                put(COLUMN_CREATE_DATE, createDate)
            }

            put(COLUMN_FILTERED, filtered)
        }
    }

    companion object {
        const val TABLE_NAME = "TB_MEDIA"
        const val COLUMN_MEDIA_ID = "mediaId"
        const val COLUMN_LOG_ID = "logId"
        const val COLUMN_FILE_TYPE = "fileType"
        const val COLUMN_FILE_NAME = "fileName"
        const val COLUMN_THUMB_NAME = "thumbName"
        const val COLUMN_VIDEO_TIME = "videoTime"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_ALTITUDE = "altitude"
        const val COLUMN_CREATE_DATE = "createDate"
        const val COLUMN_FILTERED = "filterd"

        private const val FILE_TYPE_IMAGE = "IMAGE"
        private const val FILE_TYPE_VIDEO = "VIDEO"

        fun createImageMedia(): MediaData {
            return MediaData(-1).apply {
                fileType =  FILE_TYPE_IMAGE
            }
        }

        fun createVideoMedia(): MediaData {
            return MediaData(-1).apply {
                fileType = FILE_TYPE_VIDEO
            }
        }
    }
}
