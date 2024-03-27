package com.diveroid.camera.mediaCtrl

import android.graphics.Bitmap
import android.os.AsyncTask
import com.diveroid.camera.utils.CvUtil
import kotlinx.coroutines.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import kotlin.coroutines.CoroutineContext

/**
 * Created by yunsuk on 24/10/2017.
 *
 * Edited by csson on 2022-09-07.
 * - AsyncTask 에서 Coroutine 으로 변경
 */

class ImageFilter(private val target: Bitmap?, private val callback: Callback) :
    CoroutineScope {
    private val backgroundDispatcher = Dispatchers.IO
    private val job = Job()

    private var level = 1.0

    constructor(target: Bitmap?, level: Double, callback: Callback) : this(target, callback) {
        this.level = level
    }

    fun execute() {
        launch(coroutineContext) {
            val result = doInBackground()
            onPostExecute(result)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private fun filter(target: Bitmap?) {
        val rgbaMat = loadRGBA(target) ?: return

        CvUtil.filter(rgbaMat, level)
        Utils.matToBitmap(rgbaMat, target)
        rgbaMat.release()
    }

    private fun loadRGBA(bitmap: Bitmap?): Mat? {
        if(bitmap == null) return null

        val targetImage = Mat()
        Utils.bitmapToMat(bitmap, targetImage)
        return targetImage
    }

    private suspend fun doInBackground(): Boolean {
        if(target == null || target.isRecycled) return false
        return withContext(backgroundDispatcher) {
            try {
                filter(target)
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                return@withContext false
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            }
            return@withContext true
        }
    }

    private suspend fun onPostExecute(result: Boolean) {
        if(job.isCancelled) {
            target?.recycle()
            return
        }
        withContext(Dispatchers.Main) {
            callback.onResult(result, target)
        }
    }

    interface Callback {
        fun onResult(success: Boolean, result: Bitmap?)
    }
}