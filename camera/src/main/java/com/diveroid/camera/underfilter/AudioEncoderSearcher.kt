package com.diveroid.camera.underfilter

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.io.IOException
import java.util.concurrent.Executors

/**
 * prepare
 * EncodeableAudioFormat;
 */
class AudioEncoderSearcher(var c: Context?) {
    var eosReceived = false
    var eosSentToAudioEncoder = false
    var stopReceived = false
    var audioStartTime: Long = 0
    var frameCount = 0
    var totalInputAudioFrameCount = 0 // testing
    var totalOutputAudioFrameCount = 0
    var encodingServiceQueueLength = 0
    private var audioFormat: MediaFormat? = null
    private var mAudioEncoder: MediaCodec? = null
    private val mAudioTrackIndex: TrackIndex = TrackIndex()

    //private MediaMuxer mMuxer;
    //private boolean mMuxerStarted;
    private var mAudioBufferInfo: MediaCodec.BufferInfo? = null
    private var encodingService = Executors.newSingleThreadExecutor() // re-use encodingService
    private fun prepare() {
        audioBytesReceived = 0
        numTracksAdded = 0
        frameCount = 0
        eosReceived = false
        eosSentToAudioEncoder = false
        stopReceived = false
        //File f = FileUtils.createTempFileInRootAppStorage(c, "test_" + new Date().getTime() + ".m4a");
        //Toast.makeText(c, "Saving audio to: " + f.getAbsolutePath(), Toast.LENGTH_LONG).show();
        mAudioBufferInfo = MediaCodec.BufferInfo()
        audioFormat = MediaFormat()
        audioFormat!!.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE)
        audioFormat!!.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        audioFormat!!.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
        audioFormat!!.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
        audioFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        audioFormat!!.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        try {
            mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mAudioEncoder!!.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mAudioEncoder!!.start()


        //wait for Format Configure
        val inputData = ByteArray(16384)
        offerAudioEncoder(inputData, 0)

        //mAudioEncoder.getOutputFormat();

        //try {
        //mMuxer = new MediaMuxer(f.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //} catch (IOException ioe) {
        //    throw new RuntimeException("MediaMuxer creation failed", ioe);
        //}
    }

    fun stop() {
        if (!encodingService!!.isShutdown) encodingService!!.submit(EncoderTask(this,
            EncoderTaskType.FINALIZE_ENCODER
        ))
    }

    /**
     * Called from encodingService
     */
    fun _stop() {
        stopReceived = true
        eosReceived = true
        logStatistics()
    }

    fun closeEncoderAndMuxer(encoder: MediaCodec?, bufferInfo: MediaCodec.BufferInfo?, trackIndex: TrackIndex) {
        var encoder = encoder
        try {
            encoder!!.stop()
            encoder.release()
            encoder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            encodingService!!.shutdown()
            encodingService = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mAudioEncoder!!.stop()
            mAudioEncoder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        drainEncoder(encoder, bufferInfo, trackIndex, true)
    }

    /**
     * temp restriction: Always call after offerVideoEncoder
     *
     * @param input
     */
    fun offerAudioEncoder(input: ByteArray?, presentationTimeStampNs: Long) {
        if (!encodingService!!.isShutdown) {
            //long thisFrameTime = (presentationTimeNs == 0) ? System.nanoTime() : presentationTimeNs;
            encodingService!!.submit(EncoderTask(this, input!!, presentationTimeStampNs))
            encodingServiceQueueLength++
        } else {
        }
    }

    /**
     * temp restriction: Always call after _offerVideoEncoder
     *
     * @param input
     * @param presentationTimeNs
     */
    private fun _offerAudioEncoder(input: ByteArray?, presentationTimeNs: Long) {
        if (audioBytesReceived == 0L) {
            audioStartTime = presentationTimeNs
        }
        totalInputAudioFrameCount++
        audioBytesReceived += input!!.size.toLong()
        if (eosSentToAudioEncoder && stopReceived || input == null) {
            logStatistics()
            if (eosReceived) {
                Log.i(TAG, "EOS received in offerAudioEncoder")
                closeEncoderAndMuxer(mAudioEncoder, mAudioBufferInfo, mAudioTrackIndex)
                eosSentToAudioEncoder = true
                if (!stopReceived) {
                    // swap encoder
                    prepare()
                } else {
                    Log.i(TAG, "Stopping Encoding Service")
                    encodingService!!.shutdown()
                }
            }
            return
        }
        // transfer previously encoded data to muxer
        drainEncoder(mAudioEncoder, mAudioBufferInfo, mAudioTrackIndex, false)
        // send current frame data to encoder
        try {
            val inputBuffers = mAudioEncoder!!.inputBuffers
            val inputBufferIndex = mAudioEncoder!!.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(input)
                val presentationTimeUs = (presentationTimeNs - audioStartTime) / 1000
                if (eosReceived) {
                    Log.i(TAG, "EOS received in offerEncoder")
                    mAudioEncoder!!.queueInputBuffer(inputBufferIndex, 0, input.size, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    closeEncoderAndMuxer(mAudioEncoder, mAudioBufferInfo, mAudioTrackIndex) // always called after video, so safe to close muxer
                    eosSentToAudioEncoder = true
                    if (stopReceived) {
                        Log.i(TAG, "Stopping Encoding Service")
                        encodingService!!.shutdown()
                    }
                } else {
                    mAudioEncoder!!.queueInputBuffer(inputBufferIndex, 0, input.size, presentationTimeUs, 0)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "_offerAudioEncoder exception")
            t.printStackTrace()
        }
    }

    fun wrieteToMuxter() {}

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     *
     *
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     *
     *
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    private fun drainEncoder(encoder: MediaCodec?, bufferInfo: MediaCodec.BufferInfo?, trackIndex: TrackIndex, endOfStream: Boolean) {
        val TIMEOUT_USEC = 100
        if (VERBOSE) Log.d(TAG, "drainEncoder($endOfStream)")
        var encoderOutputBuffers = encoder!!.outputBuffers
        while (true) {
            val encoderStatus = encoder.dequeueOutputBuffer(bufferInfo!!, TIMEOUT_USEC.toLong())
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS")
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.outputBuffers
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once

                //if (mMuxerStarted) {
                //    throw new RuntimeException("format changed after muxer start");
                //}
                val newFormat = encoder.outputFormat
                if (muxableAudioFormat == null) {
                    muxableAudioFormat = newFormat
                }
                closeEncoderAndMuxer(mAudioEncoder, mAudioBufferInfo, mAudioTrackIndex)

                //stop();
                // now that we have the Magic Goodies, start the muxer
                //trackIndex.index = mMuxer.addTrack(newFormat);
                // numTracksAdded++;
                // Log.d(TAG, "encoder output format changed: " + newFormat + ". Added track index: " + trackIndex.index);
                // if (numTracksAdded == TOTAL_NUM_TRACKS) {
                //     mMuxer.start();
                //     mMuxerStarted = true;
                //     Log.i(TAG, "All tracks added. Muxer started");
                // }
            } else if (encoderStatus < 0) {
                Log.w(
                    TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus)
                // let's ignore it
            } else {
                val encodedData = encoderOutputBuffers[encoderStatus]
                        ?: throw RuntimeException("encoderOutputBuffer " + encoderStatus +
                                " was null")
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
                    bufferInfo.size = 0
                }
                if (bufferInfo.size != 0) {
                    //if (!mMuxerStarted) {
                    //    throw new RuntimeException("muxer hasn't started");
                    //}

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    //mMuxer.writeSampleData(trackIndex.index, encodedData, bufferInfo);

                    //wrieteToMuxter(encodedData, bufferInfo);
                }
                encoder.releaseOutputBuffer(encoderStatus, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly")
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached")
                    }
                    break // out of while
                }
            }
        }
        val endTime = System.nanoTime()
    }

    private fun logStatistics() {
        Log.i(TAG + "-Stats", "audio frames input: $totalInputAudioFrameCount output: $totalOutputAudioFrameCount")
    }

    internal enum class EncoderTaskType {
        ENCODE_FRAME,  /*SHIFT_ENCODER,*/
        FINALIZE_ENCODER
    }

    // Can't pass an int by reference in Java...
    inner class TrackIndex {
        var index = 0
    }

    private inner class EncoderTask : Runnable {
        var is_initialized = false
        var presentationTimeNs: Long = 0
        private var encoder: AudioEncoderSearcher? = null
        private var type: EncoderTaskType? = null
        private var audio_data: ByteArray? = null

        constructor(encoder: AudioEncoderSearcher, type: EncoderTaskType?) {
            setEncoder(encoder)
            this.type = type
            when (type) {
                EncoderTaskType.FINALIZE_ENCODER -> setFinalizeEncoderParams()
                else -> {}
            }
        }

        constructor(encoder: AudioEncoderSearcher, audio_data: ByteArray, pts: Long) {
            setEncoder(encoder)
            setEncodeFrameParams(audio_data, pts)
        }

        constructor(encoder: AudioEncoderSearcher) {
            setEncoder(encoder)
            setFinalizeEncoderParams()
        }

        private fun setEncoder(encoder: AudioEncoderSearcher) {
            this.encoder = encoder
        }

        private fun setFinalizeEncoderParams() {
            is_initialized = true
        }

        private fun setEncodeFrameParams(audio_data: ByteArray, pts: Long) {
            this.audio_data = audio_data
            presentationTimeNs = pts
            is_initialized = true
            type = EncoderTaskType.ENCODE_FRAME
        }

        private fun encodeFrame() {
            if (encoder != null && audio_data != null) {
                encoder!!._offerAudioEncoder(audio_data, presentationTimeNs)
                audio_data = null
            }
        }

        private fun finalizeEncoder() {
            encoder!!._stop()
        }

        override fun run() {
            if (is_initialized) {
                when (type) {
                    EncoderTaskType.ENCODE_FRAME -> encodeFrame()
                    EncoderTaskType.FINALIZE_ENCODER -> finalizeEncoder()
                    else -> {}
                }
                // prevent multiple execution of same task
                is_initialized = false
                encodingServiceQueueLength -= 1
                //Log.i(TAG, "EncodingService Queue length: " + encodingServiceQueueLength);
            } else {
                Log.e(TAG, "run() called but EncoderTask not initialized")
            }
        }
    }

    companion object {
        private const val TAG = "AudioEncoder"
        private const val AUDIO_MIME_TYPE = "audio/mp4a-latm"
        private const val VERBOSE = false

        // Muxer state
        private const val TOTAL_NUM_TRACKS = 1

        // Audio state
        private var audioBytesReceived: Long = 0
        private var numTracksAdded = 0
        var muxableAudioFormat: MediaFormat? = null

        fun runSearch(context: Context?) {
            if (muxableAudioFormat == null) {
                val searcher = AudioEncoderSearcher(context)
            }
        }
    }

    init {
        prepare()
    }
}