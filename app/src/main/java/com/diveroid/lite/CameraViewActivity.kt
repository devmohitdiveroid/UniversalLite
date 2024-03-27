package com.diveroid.lite

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.location.*
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.diveroid.camera.ui.CameraActivity
import com.diveroid.camera.underfilter.FileUtils2
import com.diveroid.lite.data.LogData
import com.diveroid.lite.data.MediaData
import com.diveroid.lite.data.PrefData
import com.diveroid.lite.util.AppUtil
import com.diveroid.lite.util.PrefUtil
import com.diveroid.lite.util.SqliteUtil
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraViewActivity: CameraActivity() {
    private val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    private val db: SQLiteDatabase = SqliteUtil.getInstance(this).database
    private val preference = PrefUtil.getInstance(this)

    private lateinit var logData: LogData

    override fun attachBaseContext(newBase: Context?) {
        val prefValue = preference.getString(Const.PREF_LANG_SETTING, null)
        var languageCode = "ko"
        prefValue?.let {
            val prefData = Gson().fromJson(
                preference.getString(Const.PREF_LANG_SETTING, ""),
                PrefData::class.java
            )
            languageCode = prefData.value ?: "ko"
        }

        val currentLocale = if(languageCode == "ko") {
            Locale.KOREA
        } else {
            Locale.ENGLISH
        }

        val configuration = Configuration()
        configuration.setLocale(currentLocale)
        val context = newBase?.createConfigurationContext(configuration)
        super.attachBaseContext(context)
    }

    private var startDiveTime: Long = 0

    override fun startDive(startTimeMillis: Long) {
        // 다이빙 시작할 때 Log 생성
        // 위도, 경도, 주소, 시작시간
        startDiveTime = startTimeMillis
        logData = LogData(userId = AppUtil.getDeviceUuid(this))

        // 다이빙 시간 변환
        logData.startDate = formatter.format(Date(startTimeMillis))

        // 위,경도 가져오기
        getLocation()

        /**
         * 특정시간(1분) 이하로 사진&동영상 촬영하지 않고 다이빙을 종료하는 경우,
         * 로그를 남기지 않게 하기 위해 기존 코드 주석 처리
         *
         * 사진 & 동영상 저장 시 LogData.logId 체크하여 log를 추가하는 형식으로 처리
         * @see savePicture
         * @see saveVideo
         */
        // insert LogData in Local Database
//        logData.logId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            SQLiteQueryBuilder().apply {
//                tables = LogData.TABLE_NAME
//            }.insert(db, logData.toContentValue())
//        } else {
//            db.insert(
//                LogData.TABLE_NAME,
//                null,
//                logData.toContentValue()
//            )
//        }
//
//        if(logData.logId < 0) {    // LogBook 데이터를 db에 삽입하지 못한 경우. 이전 화면으로 처리
//            finish()
//        }
    }

    override fun endDive(endTimeMillis: Long) {
        if(logData.logId < 0) {
            return
        }

        if(logData.logId > 0) {
            logData.endDate = formatter.format(Date(endTimeMillis))
        }

        val cursor = SQLiteQueryBuilder().apply {
            tables = MediaData.TABLE_NAME
            appendWhere("${MediaData.COLUMN_LOG_ID} == ${logData.logId}")
        }.query(db, null, null, null, null, null, null)


        // 2022.08.30 
        if(endTimeMillis - startDiveTime < 60 * 1000 && cursor.count == 0) {
            return
        }

        if(logData.logId <= 0) {
            insertLogData()
        } else {
            updateLogData()
        }
    }

    override fun enabledSendLocationSMS(): Boolean {
        val setting = Gson().fromJson(
            preference.getString("PREF_CAMERA_SETTING_LOCATION"),
            PrefData::class.java
        )
        val telData = Gson().fromJson(
            preference.getString("PREF_CAMERA_SETTING_TEL"),
            PrefData::class.java
        )

        return setting.value.toBoolean() && !telData.value.isNullOrEmpty()
    }

    // 문자 전송
    override fun sendLocationSMS(msg: String) {
        val phoneData = Gson().fromJson(
            preference.getString("PREF_CAMERA_SETTING_TEL", ""),
            PrefData::class.java
        )

        if(phoneData == null) {     // 전송할 연락처가 없는 경우 처리
            Toast.makeText(
                this,
                getString(com.diveroid.camera.R.string.diving_location_contact_empty),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // FIXME:
        //  1. 전송할 문자 포맷 결정되지 않음.
        //     현재 다이브로이드 2.0에 있는 문구를 참고하여 만듬
        //  2. 현재 위치를 새롭게 갱신해서 전송해야 하는지 아니면 다이빙시작 시점의 위치로 전송하면 되는지?
        val smsManger = getSystemService(SmsManager::class.java) as SmsManager
        val sendSmsList = listOf(
            msg,
            "https://google.com/maps/search/?api=1&query=${logData.latitude},${logData.longitude}"
        )

        sendSmsList.forEach {
            Log.d("csson", "sendLocationSMS: send message = $it, length = ${it.length}")
            smsManger.sendTextMessage(phoneData.value, null, it, null, null)
        }
    }

    // 사진 촬영
    override fun savePicture(filePath: String, thumbPath: String) {
        if(logData.logId <= 0) {
            insertLogData()
        }

        val imageUri = createUriFromFilePath(filePath)
        val thumbUri = createUriFromFilePath(thumbPath)

        val insertData = MediaData.createImageMedia().apply {
            logId = logData.logId
            fileName = imageUri.toString()
            thumbName = thumbUri.toString()
            latitude = logData.latitude
            longitude = logData.longitude
            createDate = formatter.format(Date(System.currentTimeMillis()))
            surfaceView?.let {
                filtered = if(it.isOnFilterThread) 1 else 0
            }
        }.toContentValue()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SQLiteQueryBuilder().apply {
                tables = MediaData.TABLE_NAME
            }.insert(db, insertData)
        } else {
            db.insert(
                MediaData.TABLE_NAME,
                null,
                insertData
            )
        }
    }

    // 동영상 촬영
    override fun saveVideo(filePath: String, thumbPath: String) {
        if(logData.logId <= 0) {
            insertLogData()
        }

        val insertData = MediaData.createVideoMedia().apply {
            logId = logData.logId
            fileName = createUriFromFilePath(filePath).toString()
            thumbName = createUriFromFilePath(thumbPath).toString()
            latitude = logData.latitude
            longitude = logData.longitude
            createDate = formatter.format(Date(System.currentTimeMillis()))
            videoTime = FileUtils2.getVideoDuration(filePath)
            surfaceView?.let {
                filtered = if(it.isOnFilterThread) 1 else 0
            }
        }.toContentValue()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SQLiteQueryBuilder().apply {
                tables = MediaData.TABLE_NAME
            }.insert(db, insertData)
        } else {
            db.insert(
                MediaData.TABLE_NAME,
                null,
                insertData
            )
        }
    }

    override fun enableGuideView(): Boolean {
        return PrefUtil.getInstance(this).getBoolean(Const.PREF_VIEW_DIVING_MANUAL, true)
    }

    override fun setEnableGuideView(isExit: Boolean) {
        PrefUtil.getInstance(this).put(
            Const.PREF_VIEW_DIVING_MANUAL,
            isExit
        )
    }

    private val locationListener = LocationListener { location ->
        logData.latitude = location.latitude.toFloat()
        logData.longitude = location.longitude.toFloat()

        // FIXME:
        //  Android에서 제공해주는 Geocoder 통해 위,경도을 주소로 변환
        //  단, 리턴되는 주소 값이 정확하지 않음
        // 위,경도 기반으로 주소 변환
        val geocoder = Geocoder(this@CameraViewActivity, Locale.KOREA)
        geocoder.getFromLocation(location.latitude, location.longitude, 5)
            ?.firstOrNull()?.let { address ->
                logData.fullLocation = address.getAddressLine(0)
                logData.majorLocation = "${address.countryName}, ${address.adminArea}"
                logData.nationCode = address.countryCode
                updateLogData()
            }
        val geocoderEn = Geocoder(this@CameraViewActivity, Locale.US)
        geocoderEn.getFromLocation(location.latitude, location.longitude, 5)
            ?.firstOrNull()?.let { address ->
                logData.fullLocationEn = address.getAddressLine(0)
                logData.majorLocationEn = "${address.countryName}, ${address.adminArea}"
                //logData.nationCode = address.countryCode
                updateLogData()
            }
    }

    /**
     *  GPS 정보 가져오기
     */
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE
        }
        val locationProvider = lm.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER
        lm.requestLocationUpdates(locationProvider, 1000L, 1f, locationListener)
    }

    private fun insertLogData() {
        logData.logId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SQLiteQueryBuilder().apply {
                tables = LogData.TABLE_NAME
            }.insert(db, logData.toContentValue())
        } else {
            db.insert(
                LogData.TABLE_NAME, null, logData.toContentValue()
            )
        }
    }

    private fun updateLogData() {
        if(logData.logId <= 0 ) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SQLiteQueryBuilder().apply {
                tables = LogData.TABLE_NAME
                appendWhere("${LogData.COLUMN_LOG_ID} == ${logData.logId}")
            }.update(db, logData.toContentValue(), null, null)
        } else {
            db.update(
                LogData.TABLE_NAME,
                logData.toContentValue(),
                "${LogData.COLUMN_LOG_ID} == ${logData.logId}",
                null
            )
        }
    }

    private fun deleteLogData(logId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SQLiteQueryBuilder().apply {
                tables = LogData.TABLE_NAME
                appendWhere("${LogData.COLUMN_LOG_ID} == $logId")
            }.delete(db, null, null)
        } else {
            db.delete(
                LogData.TABLE_NAME,
                "${LogData.COLUMN_LOG_ID} == $logId",
                null
            )
        }
    }

    /**
     *  파일경로를 Uri 로 변환
     */
    private fun createUriFromFilePath(filePath: String): Uri {
        val file = File(filePath)
        val result: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "com.diveroid.lite.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        return result
    }
}
