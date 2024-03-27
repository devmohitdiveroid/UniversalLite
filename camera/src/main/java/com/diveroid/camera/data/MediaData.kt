package com.diveroid.camera.data

import android.graphics.Bitmap
import java.io.Serializable

data class MediaData(
    var mediaId: Long,
    val fileType: String,
    val fileName: String,
    val thumbName: String,
): Serializable {
    val isImage: Boolean
        get() = fileType == FILE_TYPE_IMAGE

    val isVideo: Boolean
        get() = fileType == FILE_TYPE_VIDEO

    var before: Bitmap? = null
    var after: Bitmap? = null

    // 동영상 관련
    var playing: Boolean = false

    companion object {
        const val FILE_TYPE_IMAGE = "IMAGE"
        const val FILE_TYPE_VIDEO = "VIDEO"
    }
}

