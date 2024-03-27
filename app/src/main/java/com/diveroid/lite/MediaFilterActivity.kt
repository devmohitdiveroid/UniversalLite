package com.diveroid.lite

import android.database.sqlite.SQLiteQueryBuilder
import android.os.Build
import android.util.Log
import com.diveroid.camera.filter.RetouchResultActivity
import com.diveroid.camera.underfilter.FileUtils2
import com.diveroid.lite.data.MediaData
import com.diveroid.lite.util.SqliteUtil

class MediaFilterActivity: RetouchResultActivity() {
    private val TAG = this::class.java.simpleName
    private val db = SqliteUtil.getInstance(this).database

    override fun saveCorrectedVideo(mediaId: Long, filePath: String, thumbPath: String, filtered: Boolean) {
        val fileUri = FileUtils2.getUriFromFilePath(this@MediaFilterActivity, filePath)
        val thumbUri = FileUtils2.getUriFromFilePath(this@MediaFilterActivity, thumbPath)

        val updateData = MediaData.createVideoMedia().apply {
            this.mediaId = mediaId
            this.fileName = fileUri.toString()
            this.thumbName = thumbUri.toString()
            this.filtered = if(filtered) 1 else 0
        }

        val row = updateMediaData(updateData)
        Log.d(TAG, "saveCorrectedVideo: rowId = $row")
    }

    override fun saveCorrectedImage(mediaId: Long, filtered: Boolean) {
        val updateData = MediaData.createImageMedia().apply {
            this.mediaId = mediaId
            this.filtered = if(filtered) 1 else 0
        }
        updateMediaData(updateData)
    }

    private fun updateMediaData(mediaData: MediaData): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SQLiteQueryBuilder().apply {
                tables = MediaData.TABLE_NAME
                appendWhere("${MediaData.COLUMN_MEDIA_ID} == ${mediaData.mediaId}")
            }.update(db, mediaData.toContentValue(), null, null)
        } else {
            db.update(
                MediaData.TABLE_NAME,
                mediaData.toContentValue(),
                "${MediaData.COLUMN_MEDIA_ID} == ${mediaData.mediaId}",
                null
            )
        }
    }
}