package com.diveroid.camera.underfilter

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.util.concurrent.ArrayBlockingQueue

/*
* This class polls audio from the microphone and feeds an
* AudioEncoder. Audio buffers are recycled between this class and the AudioEncoder
*
* Usage:
*
* 1. AudioSoftwarePoller recorder = new AudioSoftwarePoller();
* 1a (optional): recorder.setSamplesPerFrame(NUM_SAMPLES_PER_CODEC_FRAME)
* 2. recorder.setAudioEncoder(myAudioEncoder)
* 2. recorder.startPolling();
* 3. recorder.stopPolling();
*/
class AudioSoftwarePoller {
    val VERBOSE = false
    var recorderTask: RecorderTask = RecorderTask()
    var mAudioEncoder: AudioEncoder? = null
    fun setAudioEncoder(avcEncoder: AudioEncoder?) {
        mAudioEncoder = avcEncoder
    }

    /**
     * Set the number of samples per frame (Default is 1024). Call this before startPolling().
     * The output of emptyBuffer() will be equal to, or a multiple of, this value.
     *
     * @param samples_per_frame The desired audio frame size in samples.
     */
    fun setSamplesPerFrame(samples_per_frame: Int) {
        if (!is_recording) recorderTask.samples_per_frame = samples_per_frame
    }

    /**
     * Return the number of microseconds represented by each audio frame
     * calculated with the sampling rate and samples per frame
     * @return
     */
    val microSecondsPerFrame: Long
        get() {
            if (US_PER_FRAME == 0L) {
                US_PER_FRAME = (SAMPLE_RATE / recorderTask.samples_per_frame * 1000000).toLong()
            }
            return US_PER_FRAME
        }

    fun recycleInputBuffer(buffer: ByteArray) {
        recorderTask.data_buffer.offer(buffer)
    }

    /**
     * Begin polling audio and transferring it to the buffer. Call this before emptyBuffer().
     */
    fun startPolling() {
        Thread(recorderTask).start()
    }

    /**
     * Stop polling audio.
     */
    fun stopPolling() {
        is_recording = false // will stop recording after next sample received
        // by recorderTask
    }

    inner class RecorderTask : Runnable {
        var buffer_size = 0

        //public int samples_per_frame = 1024;    // codec-specific
        var samples_per_frame = 2048 // codec-specific
        var buffer_write_index = 0 // last buffer index written to

        //public byte[] data_buffer;
        var total_frames_written = 0
        var data_buffer = ArrayBlockingQueue<ByteArray>(50)
        var read_result = 0
        override fun run() {
            val min_buffer_size = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            buffer_size = samples_per_frame * FRAMES_PER_BUFFER

            // Ensure buffer is adequately sized for the AudioRecord
            // object to initialize
            if (buffer_size < min_buffer_size) buffer_size = (min_buffer_size / samples_per_frame + 1) * samples_per_frame * 2

            //data_buffer = new byte[samples_per_frame]; // filled directly by hardware
            for (x in 0..24) data_buffer.add(ByteArray(samples_per_frame))
            var audio_recorder: AudioRecord?
            audio_recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,  // source
                    SAMPLE_RATE,  // sample rate, hz
                    CHANNEL_CONFIG,  // channels
                    AUDIO_FORMAT,  // audio format
                    buffer_size) // buffer size (bytes)
            audio_recorder.startRecording()
            is_recording = true
            Log.i("AudioSoftwarePoller", "SW recording begin")
            var audioPresentationTimeNs: Long
            while (is_recording) {
                //read_result = audio_recorder.read(data_buffer, buffer_write_index, samples_per_frame);
                audioPresentationTimeNs = System.nanoTime()
                var this_buffer: ByteArray?
                this_buffer = if (data_buffer.isEmpty()) {
                    ByteArray(samples_per_frame)
                    //Log.i(TAG, "Audio buffer empty. added new buffer");
                } else {
                    data_buffer.poll()
                }
                read_result = audio_recorder.read(this_buffer!!, 0, samples_per_frame)
                if (VERBOSE) Log.i("RecordAudioLog", buffer_write_index.toString() + " - " + (buffer_write_index + samples_per_frame - 1).toString())
                if (read_result == AudioRecord.ERROR_BAD_VALUE || read_result == AudioRecord.ERROR_INVALID_OPERATION) Log.e("AudioSoftwarePoller", "Read error")
                //buffer_write_index = (buffer_write_index + samples_per_frame) % buffer_size;
                total_frames_written++
                if (mAudioEncoder != null) {
                    mAudioEncoder!!.offerAudioEncoder(this_buffer, audioPresentationTimeNs)
                }
            }
            if (audio_recorder != null) {
                audio_recorder.setRecordPositionUpdateListener(null)
                audio_recorder.release()
                audio_recorder = null
                Log.i("AudioSoftwarePoller", "stopped")
            }
        }
    }

    companion object {
        const val TAG = "AudioSoftwarePoller"
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val FRAMES_PER_BUFFER = 24 // 1 sec @ 1024 samples/frame (aac)
        var US_PER_FRAME: Long = 0
        var is_recording = false
    }
}