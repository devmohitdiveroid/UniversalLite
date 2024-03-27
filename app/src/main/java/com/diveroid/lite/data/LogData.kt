package com.diveroid.lite.data

import android.content.ContentValues
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

data class LogData(
    var logId: Long = -1L,                  // 로그아이디
    var userId: String,                    // 유저아이디 (Lite에선 UUID)
    var latitude: Float? = null,           // 위도
    var longitude: Float? = null,          // 경도
    var altitude: Float? = null,           // 고도
    var fullLocation: String? = null,      // 전체주소 ex) 대한민국 수성구 황금동 213-4
    var majorLocation: String? = null,     // 국가,도시 ex) 대한민국, 수성구
    var fullLocationEn: String? = null,      // 전체주소 영문 ex) 대한민국 수성구 황금동 213-4
    var majorLocationEn: String? = null,     // 국가,도시 영문 ex) 대한민국, 수성구
    var nationCode: String? = null,        // 국가코드 ex) KR
    var coverImgFileName: String? = null,  // 커버이미지이름 (사용하지 않음)
    var startDate: String? = null,         // 다이빙 시작시간(yyyyMMddHHmmss)
    var endDate: String? = null,           // 다이빙 종료시간(yyyyMMddHHmmss)
) {


    fun toContentValue(): ContentValues {
        return ContentValues().apply {
            if (logId > 0) {
                put(COLUMN_LOG_ID, logId)
            }
            if (userId.isNotEmpty()) {
                put(COLUMN_USER_ID, userId)
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
            if (fullLocation?.isNotEmpty() == true) {
                put(COLUMN_FULL_LOCATION, fullLocation)
            }
            if (majorLocation?.isNotEmpty() == true) {
                put(COLUMN_MAJOR_LOCATION, majorLocation)
            }
            if (fullLocationEn?.isNotEmpty() == true) {
                put(COLUMN_FULL_LOCATION_EN, fullLocationEn)
            }
            if (majorLocationEn?.isNotEmpty() == true) {
                put(COLUMN_MAJOR_LOCATION_EN, majorLocationEn)
            }
            if (nationCode?.isNotEmpty() == true) {
                put(COLUMN_NATION_CODE, nationCode)
            }
            if (coverImgFileName?.isNotEmpty() == true) {
                put(COLUMN_COVER_IMG_FILE_NAME, coverImgFileName)
            }
            if (startDate?.isNotEmpty() == true) {
                put(COLUMN_START_DATE, startDate)
            }
            if (endDate?.isNotEmpty() == true) {
                put(COLUMN_END_DATE, endDate)
            }
        }
    }

    companion object {
        const val TABLE_NAME = "TB_LOG"
        const val COLUMN_LOG_ID = "logId"
        const val COLUMN_USER_ID = "userId"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_ALTITUDE = "altitude"
        const val COLUMN_FULL_LOCATION = "fullLocation"
        const val COLUMN_MAJOR_LOCATION = "majorLocation"
        const val COLUMN_FULL_LOCATION_EN = "fullLocationEn"
        const val COLUMN_MAJOR_LOCATION_EN = "majorLocationEn"
        const val COLUMN_NATION_CODE = "nationCode"
        const val COLUMN_COVER_IMG_FILE_NAME = "coverImgFileName"
        const val COLUMN_START_DATE = "startDate"
        const val COLUMN_END_DATE = "endDate"
    }
}
