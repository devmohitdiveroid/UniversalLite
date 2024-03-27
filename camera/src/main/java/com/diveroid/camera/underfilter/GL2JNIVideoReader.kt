package com.diveroid.camera.underfilter

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

object GL2JNIVideoReader {
    var TAG = ""
    var SAMPLE = ""
    var playerThread: PlayerThread? = null
    var mTextureID = 0 //
    var mSurfaceTexture: SurfaceTexture? = null
    var mSurface: Surface? = null
    private var mAudioFormatInited = false
    private var mAudioFormat: MediaFormat? = null
    var running = false
    fun Start(fileName: String, textureID: Int): MediaFormat? {
        mTextureID = textureID
        SAMPLE = fileName


        // mAudioFormat = null;
        playerThread = PlayerThread()
        running = true
        GL2JNILib.frameChanged = 0
        playerThread!!.start()
        mAudioFormatInited = false
        mAudioFormat = null
        while (mAudioFormatInited == false) {
            try {
                Thread.sleep(0)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                break
            }
        }
        return mAudioFormat
    }

    fun Stop() {
        if (playerThread != null) {
            playerThread!!.interrupt()
            try {
                // playerThread.wait();
                playerThread!!.join()
            } catch (e: Exception) {
            }
            playerThread = null
            running = false
            mSurfaceTexture = null
            mSurface = null
        }
    }

    class PlayerThread  //private Surface surface;
        : Thread() {
        private var extractor: MediaExtractor? = null
        private var decoder: MediaCodec? = null

        //   this.surface = surface;
        //}
        override fun run() {
            try {
                mSurfaceTexture!!.setOnFrameAvailableListener { GL2JNILib.frameChanged = 1 }
                extractor = MediaExtractor()
                extractor!!.setDataSource(SAMPLE)
                var width = 0
                var height = 0
                var rotation = 0
                for (i in 0 until extractor!!.trackCount) {
                    val format = extractor!!.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (mime!!.startsWith("video/")) {//leess 타겟30변경 에러수정(!!붙임)
                        extractor!!.selectTrack(i)
                        decoder = MediaCodec.createDecoderByType(mime)
                        decoder!!.configure(format,  /*null*/mSurface, null, 0)
                        width = format.getInteger(MediaFormat.KEY_WIDTH)
                        height = format.getInteger(MediaFormat.KEY_HEIGHT)
                        try {
                            rotation = format.getInteger(MediaFormat.KEY_ROTATION)
                        } catch (e: Exception) {
                        }
                        break
                    }
                }


                //int selectedTrack = -1;
                for (i in 0 until extractor!!.trackCount) {
                    val format = extractor!!.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (mime!!.startsWith("audio/")) {//leess 타겟30변경 에러수정(!!붙임)
                        //extractor.selectTrack(i);
                        //selectedTrack = i;
                        mAudioFormat = extractor!!.getTrackFormat(i)
                        break
                    }
                }
                mAudioFormatInited = true
                if (decoder == null) {
                    Log.e("DecodeActivity", "Can't find video info!")
                    return
                }
                decoder!!.start()
                val inputBuffers = decoder!!.inputBuffers
                var outputBuffers = decoder!!.outputBuffers
                val info = MediaCodec.BufferInfo()
                var isEOS = false
                // long startMs = System.currentTimeMillis();


                //info.size.
                while (!interrupted()) {
                    if (!isEOS) {
                        val inIndex = decoder!!.dequeueInputBuffer(10000)
                        if (inIndex >= 0) {
                            val buffer = inputBuffers[inIndex]
                            val sampleSize = extractor!!.readSampleData(buffer, 0)
                            if (sampleSize < 0) {
                                // We shouldn't stop the playback at this point, just pass the EOS
                                // flag to decoder, we will get it again from the
                                // dequeueOutputBuffer
                                Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM")
                                decoder!!.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isEOS = true
                            } else {
                                decoder!!.queueInputBuffer(inIndex, 0, sampleSize, extractor!!.sampleTime, 0)
                                extractor!!.advance()
                            }
                        }
                    }
                    val outIndex = decoder!!.dequeueOutputBuffer(info, 10000)
                    when (outIndex) {
                        MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                            Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED")
                            outputBuffers = decoder!!.outputBuffers
                        }
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d("DecodeActivity", "New format " + decoder!!.outputFormat)
                        MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d("DecodeActivity", "dequeueOutputBuffer timed out!")
                        else -> {
                            val buffer = outputBuffers[outIndex]
                            Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, $buffer")

                            // We use a very simple clock to keep the video FPS, or the video
                            // playback will be too fast
                            //    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            //       try {
                            //           sleep(0);
                            //      } catch (InterruptedException e) {
                            //          e.printStackTrace();
                            //          break;
                            //      }

                            //  }
                            while (GL2JNILib.frameChanged != 0) {
                                if (interrupted()) {
                                    return
                                }
                                sleep(1)
                            }
                            GL2JNILib.frameChangePresentationTimeUs = info.presentationTimeUs
                            GL2JNILib.frameChanged = 2
                            decoder!!.releaseOutputBuffer(outIndex, true)
                        }
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                        break
                    }
                }
                decoder!!.stop()
                decoder!!.release()
                extractor!!.release()
            } catch (ex: Exception) {
                Log.e(TAG, "run: ", ex)
                mAudioFormatInited = true
            }
            running = false
        }
    }
}