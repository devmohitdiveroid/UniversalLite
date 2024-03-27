package com.diveroid.camera.utils

import android.graphics.Bitmap
import android.util.Log
import com.diveroid.camera.mediaCtrl.MediaFileControl
import com.diveroid.camera.provider.ContextProvider
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ImageSaver(
    private val bitmap: Bitmap,
    val filePath: String,
    val thumbPath: String,
    private val imageSaverListener: OnImageSaverListener?
) : CoroutineScope {
    private val TAG = this::class.java
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val job = Job()

//    private var filePath: String? = filePath
//    private var thumbPath: String? = thumbPath

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun cancel() {
        job.cancel()
    }

    fun execute() {
        launch(coroutineContext) {
            val result = doInBackground()
            onPostExecute(result)
        }
    }

    private suspend fun doInBackground(): Boolean {
        return withContext(backgroundDispatcher) {
            save()
        }
    }

    private suspend fun onPostExecute(result: Boolean) {
        withContext(Dispatchers.Main) {
            if(result) {    // Success
                Log.d("csson", "onPostExecute: 11111")
                imageSaverListener?.onImageSaveCompleted(filePath, thumbPath)
            } else {    // Fail
                Log.d("csson", "onPostExecute: 2222")
                imageSaverListener?.onImageSaveFailed("")
            }
        }
    }

    private fun save(): Boolean {
        MediaFileControl.init(ContextProvider.context!!)
        return try {
            val localPath = MediaFileControl.getLocalMediaPath(filePath, false)
            val thumbPath = MediaFileControl.getLocalMediaPath(filePath, true)

            Log.d("csson", "save: localPath = $localPath, thumbPath = $thumbPath")

            val thumbBitmap = BitmapUtils.getResizedBitmap(bitmap, MediaFileControl.SIZE_THUMBNAIL_X)
            // save thumbnail image
            BitmapUtils.saveBitmap(thumbPath!!, thumbBitmap)
            thumbBitmap.recycle()

            // save original image
            BitmapUtils.saveBitmap(localPath!!, bitmap)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    interface OnImageSaverListener {
        fun onImageSaveCompleted(filePath: String, thumbPath: String)
        fun onImageSaveFailed(msg: String)
    }
}