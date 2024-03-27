package com.diveroid.camera.underfilter

import android.media.*
import android.opengl.*
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

//import static com.ao.diveroid.module.water.BuildConfig.DEBUG;
/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

 Starting with Android 8.0 (API level 26) you can use a MediaMuxer to record multiple simultaneous audio and video streams.
 In earlier versions of Android you can only record one audio track and/or one video track at a time.
 Use the addTrack() method to mix multipe tracks together.
 You can also add one or more metadata tracks with custom information for each frame,
 but only to MP4 containers. Your app defines the format and content of the metadata.
 */
class GL2JNIVideoWriter internal constructor() {
    // RGB color values for generated frames
    //private static final int TEST_R0 = 0;
    //p//rivate static final int TEST_G0 = 136;
    //private static final int TEST_B0 = 0;
    //private static final int TEST_R1 = 236;
    //private static final int TEST_G1 = 50;
    //private static final int TEST_B1 = 186;
    // size of a frame, in pixels
    private var mWidth = -1
    private var mHeight = -1
    private var mRotate = 0

    // bit rate, in bits per second
    private var mBitRate = -1

    // encoder / muxer state
    private var mEncoder: MediaCodec? = null
    private var mInputSurface: CodecInputSurface? = null
    private var mMuxer: MediaMuxer? = null
    private var mTrackIndex = 0
    private var mMuxerStarted = false
    private val mMuxerAudioReady = true
    private var mMuxerVideoReady = false

    // allocate one of these up front so we don't need to do it every time
    private var mBufferInfo: MediaCodec.BufferInfo? = null

    //private MediaCodec mEncoderAutdio;
    private var mTrackIndexAudio = -1
    private val mBufferInfoAudio: MediaCodec.BufferInfo? = null
    private var mOutPath = ""
    var mAudioEncoder: AudioEncoder? = null
    var mAudioSoftwarePoller: AudioSoftwarePoller? = null
    val dstWidth: Float
        get() = mWidth.toFloat()
    val dstHeight: Float
        get() = mHeight.toFloat()

    /**
     * Tests encoding of AVC video from a Surface.  The output is saved as an MP4 file.
     */
    var bRunningRecord = false
    var drainThread = Thread {
        while (bRunningRecord) {
            drainEncoder(false)
            if (mFileEncodeMode == false) {
                if (null != mMuxer && mTrackIndexAudio >= 0) {
                    mAudioEncoder!!.setmAudioTrackIndex(mTrackIndexAudio)
                    mAudioEncoder!!.drainToMuxer(false, mMuxer)
                }
            }


            //mMuxer.stop();//.setOrientationHint();
            //mMuxer.start();

            //mMuxer.set
            try {
                Thread.sleep(1)
            } catch (e: Exception) {
            }
        }
        drainEncoder(true)
        ///mAudioEncoder.stop();;
        if (mFileEncodeMode == false) {
            if (mMuxer != null && mTrackIndexAudio >= 0) {
                mAudioEncoder!!.drainToMuxer(true, mMuxer)
            }
        }


        // send end-of-stream to encoder, and drain remaining output
        //drainEncoder(true);
    }
    var mFileEncodeMode = false
    var mAudioFormat: MediaFormat? = null
    fun startEncode(width: Int, height: Int, frameRate: Int, filename: String, rotate: Int, fileEncodeMode: Boolean, audioFormat: MediaFormat?) {
        var width = width
        var height = height
        mFileEncodeMode = fileEncodeMode
        mAudioFormat = audioFormat
        if (rotate == 90 || rotate == 270) {

            //swap
            val temp = height
            height = width
            width = temp

            //    format.setInteger(MediaFormat.KEY_ROTATION, mRotate);
        }
        mOutPath = filename
        bRunning = true
        runningCount = 0


        // QHD at 2Mbps

        //mBitRate =  30 * width * height / 2;//frameRate
        mBitRate = frameRate * width * height / 10 + width * height * 30 / 10 //
        mWidth = width
        mHeight = height
        mRotate = rotate
        FRAME_RATE = frameRate
        try {
            prepareEncoder()
        } catch (e: Exception) {
        }
        if (!mFileEncodeMode) {
            try {
                mAudioSoftwarePoller = AudioSoftwarePoller()
                mAudioEncoder = AudioEncoder()
                mAudioEncoder!!.setAudioSoftwarePoller(mAudioSoftwarePoller)
                mAudioSoftwarePoller!!.setAudioEncoder(mAudioEncoder)
                mAudioSoftwarePoller!!.startPolling()


                // prepareEncoder();
                //mInputSurface.makeCurrent();
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bRunningRecord = true
        drainThread.start()
        // = new Thread(new Runnable() {
    }

    fun MakeCurrent() {}
    fun writeToFile(timeStamp: Long) {
        if (mInputSurface == null) {
            return
        }

        //drainEncoder(false);

        // while( mBufferReady == )
        // mBufferReady = true;
        mInputSurface!!.setPresentationTime(timeStamp) //computePresentationTimeNsec(runningCount));


        // Submit it to the encoder.  The eglSwapBuffers call will block if the input
        // is full, which would be bad if it stayed full until we dequeued an output
        // buffer (which we can't do, since we're stuck here).  So long as we fully drain
        // the encoder before supplying additional input, the system guarantees that we
        // can supply another frame without blocking.
        if (VERBOSE) Log.d(TAG, "sending frame " + runningCount + " to encoder")
        mInputSurface!!.swapBuffers()
        ++runningCount
    }

    //
    //    public void writeToFileAudio(long timeStamp, final ByteBuffer buffer ,final int length) {
    //
    //        //drainEncoder(false);
    //
    //
    //
    //        timeStamp=  getPTSUs();
    //
    //
    //        /***********************************
    //         * writing Audio
    //         ************************************/
    //
    //        {
    //             int TIMEOUT_USEC = 10000;
    //
    //
    //            final ByteBuffer[] inputBuffers = mEncoderAutdio.getInputBuffers();
    //            //while (mIsCapturing)
    //
    //            {
    //                final int inputBufferIndex = mEncoderAutdio.dequeueInputBuffer(TIMEOUT_USEC);
    //                if (inputBufferIndex >= 0) {
    //                    final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
    //                    inputBuffer.clear();
    //                    if (buffer != null) {
    //                        inputBuffer.put(buffer);
    //                    }
    ////	            if (DEBUG) Log.v(TAG, "encode:queueInputBuffer");
    //                    if (length <= 0) {
    //                        // send EOS
    //                       // mIsEOS = true;
    //                        if (DEBUG) Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
    //                        mEncoderAutdio.queueInputBuffer(inputBufferIndex, 0, 0,
    //                                timeStamp, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    //                        return;
    //                    } else {
    //                        mEncoderAutdio.queueInputBuffer(inputBufferIndex, 0, length,
    //                                timeStamp, 0);
    //                    }
    //                    return;
    //                } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
    //                    // wait for MediaCodec encoder is ready to encode
    //                    // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
    //                    // will wait for maximum TIMEOUT_USEC(10msec) on each call
    //                }
    //            }
    //
    //
    //        }
    //
    //
    //
    //        ++runningCount;
    //    }
    fun stopEncode() {
        bRunning = false
        bRunningRecord = false
        if (mFileEncodeMode == false) {
            mAudioSoftwarePoller!!.stopPolling()
            //AudioEncoder.drainToMuxer(true, mMuxer);
            mAudioSoftwarePoller = null
        }
        if (mAudioEncoder != null) {
            // mAudioEncoder
        }
        try {
            drainThread.join()
        } catch (e: Exception) {
        }
        releaseEncoder()


        /*------------------------------------
                    파일 합치기
        --------------------------------------*/
//
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//                String outputFile = "";
//
//                try {
//
//                    //File file = new File(mOutPath);
//                    //file.createNewFile();
//                    outputFile = mOutPath;//file.getAbsolutePath();
//
//                    MediaExtractor videoExtractor = new MediaExtractor();
//                    // AssetFileDescriptor afdd = AssetFileDescriptor. ().openFd("Produce.MP4");
//
//
//                    videoExtractor.setDataSource(mOutPath + ".video");//afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());
//
//                    MediaExtractor audioExtractor = new MediaExtractor();
//                    audioExtractor.setDataSource(mOutPath + ".audio");
//
//                    Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
//                    Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );
//
//                    int trackCount = audioExtractor.getTrackCount();
//
//                    MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//
//                    videoExtractor.selectTrack(0);
//                    MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
//                    int videoTrack = muxer.addTrack(videoFormat);
//
//                    audioExtractor.selectTrack(0);
//                    MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
//                    int audioTrack = muxer.addTrack(audioFormat);
//
//                    Log.d(TAG, "Video Format " + videoFormat.toString() );
//                    Log.d(TAG, "Audio Format " + audioFormat.toString() );
//
//                    boolean sawEOS = false;
//                    int frameCount = 0;
//                    int offset = 100;
//                    int sampleSize = 16 * 1024 * 1024;
//                    ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
//                    ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
//                    MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
//                    MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
//
//
//                    videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                    audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//
//                    muxer.start();
//
//                    while (!sawEOS)
//                    {
//                        videoBufferInfo.offset = offset;
//                        videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);
//
//
//                        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
//                        {
//                            Log.d(TAG, "saw input EOS.");
//                            sawEOS = true;
//                            videoBufferInfo.size = 0;
//
//                        }
//                        else
//                        {
//                            videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
//                            videoBufferInfo.flags = videoExtractor.getSampleFlags();
//                            muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
//                            videoExtractor.advance();
//
//                            frameCount++;
//                            Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
//                            Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
//                        }
//                    }
//
//                    //  Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();
//
//                    boolean sawEOS2 = false;
//                    int frameCount2 =0;
//                    while (!sawEOS2)
//                    {
//                        frameCount2++;
//
//                        audioBufferInfo.offset = offset;
//                        audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);
//
//                        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
//                        {
//                            Log.d(TAG, "saw input EOS.");
//                            sawEOS2 = true;
//                            audioBufferInfo.size = 0;
//                        }else
//                        {
//                            audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
//                            audioBufferInfo.flags = audioExtractor.getSampleFlags();
//                            muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
//                            audioExtractor.advance();
//
//                            Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
//                            Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
//                        }
//
//                    }
//                    // Toast.makeText(getApplicationContext() , "frame:" + frameCount2 , Toast.LENGTH_SHORT).show();
//                    muxer.stop();
//                    muxer.release();
//
//
//                    {
//                        File fdelete = new File(mOutPath + ".audio");
//                        if (fdelete.exists()) {
//                            if (fdelete.delete()) {
//                                System.out.println("file Deleted :" + mOutPath + ".audio");
//                            } else {
//                                System.out.println("file not Deleted :" + mOutPath + ".audio");
//                            }
//                        }
//                    }
//
//                    {
//                        File fdelete = new File(mOutPath + ".video");
//                        if (fdelete.exists()) {
//                            if (fdelete.delete()) {
//                                System.out.println("file Deleted :" + mOutPath + ".video");
//                            } else {
//                                System.out.println("file not Deleted :" +mOutPath + ".video");
//                            }
//                        }
//                    }
//
//
//                } catch (IOException e) {
//                    Log.d(TAG, "Mixer Error 1 " + e.getMessage());
//                } catch (Exception e) {
//                    Log.d(TAG, "Mixer Error 2 " + e.getMessage());
//                }
//
//
//
//
//            }
//        });
//
//        t.start();;
    }

    fun stopEncodeFilePlay(playedFile: String?) {
        bRunning = false
        bRunningRecord = false
        try {
            drainThread.join()
        } catch (e: Exception) {
        }


        /*------------------------------------
                    파일 합치기
        --------------------------------------*/run {
            var outputFile = ""
            try {

                //Thread.sleep(1000);
                //File file = new File(mOutPath);
                //file.createNewFile();
                outputFile = mOutPath //file.getAbsolutePath();

                //MediaExtractor videoExtractor = new MediaExtractor();
                // AssetFileDescriptor afdd = AssetFileDescriptor. ().openFd("Produce.MP4");
                //videoExtractor.setDataSource(mOutPath + ".video");//afdd.getFileDescriptor() ,afdd.getStartOffset(),afdd.getLength());
                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(playedFile!!)

                //Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
                Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.trackCount)
                val trackCount = audioExtractor.trackCount

                //MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                // videoExtractor.selectTrack(0);
                // MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                //int videoTrack = muxer.addTrack(videoFormat);
                var selectedTrack = -1
                for (i in 0 until audioExtractor.trackCount) {
                    val format = audioExtractor.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (mime!!.startsWith("audio/")) {//leess 타겟30변경 에러수정(!!붙임)
                        audioExtractor.selectTrack(i)
                        selectedTrack = i
                        break
                    }
                }
                val audioTrack = mTrackIndexAudio


                // Log.d(TAG, "Video Format " + videoFormat.toString() );
                //  Log.d(TAG, "Audio Format " + audioFormat.toString() );
                val sawEOS = false
                val frameCount = 0
                val offset = 100
                val sampleSize = 16 * 1024 * 1024
                val videoBuf = ByteBuffer.allocate(sampleSize)
                val audioBuf = ByteBuffer.allocate(sampleSize)
                val videoBufferInfo = MediaCodec.BufferInfo()
                val audioBufferInfo = MediaCodec.BufferInfo()


                // videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

                // muxer.start();

                // while (!sawEOS)
                // {
                //    videoBufferInfo.offset = offset;
                //    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                //   if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                //   {
                //       Log.d(TAG, "saw input EOS.");
                //       sawEOS = true;
                //       videoBufferInfo.size = 0;

                //   }
                //  else
                // {
                //     videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                //     videoBufferInfo.flags = videoExtractor.getSampleFlags();
                //     muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                //     videoExtractor.advance();
//
                //                          frameCount++;
                //                        Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                //                      Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);
                //                }
                //          }

                //  Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();
                if (selectedTrack >= 0) {
                    var sawEOS2 = false
                    var frameCount2 = 0
                    while (!sawEOS2) {
                        frameCount2++
                        audioBufferInfo.offset = offset
                        audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset)
                        if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                            Log.d(TAG, "saw input EOS.")
                            sawEOS2 = true
                            audioBufferInfo.size = 0
                        } else {
                            audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                            audioBufferInfo.flags = audioExtractor.sampleFlags
                            mMuxer!!.writeSampleData(audioTrack, audioBuf, audioBufferInfo)
                            audioExtractor.advance()
                            Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024)
                            Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024)
                        }
                    }
                }
                else {

                }

                // Toast.makeText(getApplicationContext() , "frame:" + frameCount2 , Toast.LENGTH_SHORT).show();
                //muxer.stop();
                //muxer.release();


                //{//
                //    File fdelete = new File(mOutPath + ".video");
                //   if (fdelete.exists()) {
                //       if (fdelete.delete()) {
                //          System.out.println("file Deleted :" + mOutPath + ".video");
                //      } else {
                //          System.out.println("file not Deleted :" + mOutPath + ".video");
                //      }
                //  }
                //}
            } catch (e: IOException) {
                Log.d(TAG, "Mixer Error 1 " + e.message)
            } catch (e: Exception) {
                Log.d(TAG, "Mixer Error 2 " + e.message)
            }
        }
        releaseEncoder()
        try {
            val fsrc = File("$mOutPath.video")
            val fdst = File(mOutPath)
            fsrc.renameTo(fdst)
        } catch (e: Exception) {
            //Log.e("TAG", e.message)
        }
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    private fun prepareEncoder() {


        /*--------------------------------------------------------
            Setup Video
         --------------------------------------------------------*/
        run {
            mBufferInfo = MediaCodec.BufferInfo()
            val format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight)

            // Set some properties.  Failing to specify some of these can cause the MediaCodec
            // configure() call to throw an unhelpful exception.
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)

            //if( mRotate > 0) {
            //    format.setInteger(MediaFormat.KEY_ROTATION, mRotate);
            //}
            if (VERBOSE) Log.d(TAG, "format: $format")

            // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
            // we can use for input and wrap it with a class that handles the EGL work.
            //
            // If you want to have two EGL contexts -- one for display, one for recording --
            // you will likely want to defer instantiation of CodecInputSurface until after the
            // "display" EGL context is created, then modify the eglCreateContext call to
            // take eglGetCurrentContext() as the share_context argument.
            try {
                mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mEncoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mInputSurface = CodecInputSurface(mEncoder!!.createInputSurface(), mFileEncodeMode == false /*실시간 녹화할때만 antialising를 적용 하면 된다.*/)
            mEncoder!!.start()
        }

        /*--------------------------------------------------------
             Setup Audio
          --------------------------------------------------------*/

//        {
//
//            final String MIME_TYPE_AUDIO = "audio/mp4a-latm";
//            final int SAMPLE_RATE = 44100;	// 44.1[KHz] is only setting guaranteed to be available on all devices.
//            final int BIT_RATE = 64000;
//            final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
//            final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec
//
//
//            mBufferInfoAudio = new MediaCodec.BufferInfo();
//
//
//            final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE_AUDIO);
//            if (audioCodecInfo == null) {
//                Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE_AUDIO);
//                return;
//            }
//
//
//            final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE_AUDIO, SAMPLE_RATE, 1);
//            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
//            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
//            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
////		audioFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, inputFile.length());
////      audioFormat.setLong(MediaFormat.KEY_DURATION, (long)durationInMs );
//            if (DEBUG) Log.i(TAG, "format: " + audioFormat);
//
//
//            try {
//                mEncoderAutdio = MediaCodec.createEncoderByType(MIME_TYPE_AUDIO);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            mEncoderAutdio.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//            mEncoderAutdio.start();
//        }


        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        val outputPath = mOutPath //new File(OUTPUT_DIR,
        //"test." + mWidth + "x" + mHeight + ".mp4").toString();
        Log.d(TAG, "output file is $outputPath")


        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        mMuxer = try {
            if (mFileEncodeMode) {
                MediaMuxer("$outputPath.video", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            } else {
                MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            }
        } catch (ioe: IOException) {
            throw RuntimeException("MediaMuxer creation failed", ioe)
        }
        mTrackIndex = -1
        mMuxerStarted = false
    }

    var mBufferReady = true

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private fun releaseEncoder() {
        if (VERBOSE) Log.d(TAG, "releasing encoder objects")
        if (mEncoder != null) {
            mEncoder!!.stop()
            mEncoder!!.release()
            mEncoder = null
        }

//
//        if (mEncoderAutdio != null) {
//            mEncoderAutdio.stop();
//            mEncoderAutdio.release();
//            mEncoderAutdio = null;
//        }
//
        if (!mFileEncodeMode) {
            mAudioEncoder!!.stop()
            //Muxer = new MediaMuxer(outputPath + ".video", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } else {
        }
        if (mInputSurface != null) {
            mInputSurface!!.release()
            mInputSurface = null
        }
        try {
            if (mMuxer != null) {
                mMuxer!!.stop()
                mMuxer!!.release()
                mMuxer = null
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Extracts all pending data from the encoder.
     *
     *
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    private fun drainEncoder(endOfStream: Boolean) {
        val TIMEOUT_USEC = 10000
        if (VERBOSE) Log.d(TAG, "drainEncoder($endOfStream)")
        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder")
            try {
                mEncoder!!.signalEndOfInputStream()
            } catch (e: Exception) {
            }

//            try
//            {
//                mEncoderAutdio.signalEndOfInputStream();
//            }catch(Exception e)
//            {
//
//            }
        }


        /*-------------------------------------
        //video Encode
        ---------------------------------------*/


        //  if( false)
        try {
            var encoderOutputBuffers = mEncoder!!.outputBuffers
            while (mMuxerStarted || mMuxerVideoReady == false) {
                val encoderStatus = mEncoder!!.dequeueOutputBuffer(mBufferInfo!!, TIMEOUT_USEC.toLong())
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (!endOfStream) {
                        break // out of while
                    } else {
                        if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS")
                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    encoderOutputBuffers = mEncoder!!.outputBuffers
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once
                    if (mMuxerStarted) {
                        throw RuntimeException("format changed twice")
                    }
                    val newFormat = mEncoder!!.outputFormat
                    Log.d(TAG, "encoder output format changed: $newFormat")

                    // now that we have the Magic Goodies, start the muxer
                    mTrackIndex = mMuxer!!.addTrack(newFormat)

                    //mFileEncodeMode &&
                    mTrackIndexAudio = if (mAudioFormat != null) {
                        try {
                            mMuxer!!.addTrack(mAudioFormat!!)
                        } catch (e: Exception) {
                            -1
                        }
                    } else {
                        -1
                    }
                    mMuxerVideoReady = true
                    if (mMuxerAudioReady && mMuxerVideoReady) {
                        mMuxer!!.start()
                        mMuxerStarted = true
                    }
                } else if (encoderStatus < 0) {
                    Log.w(
                        TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                            encoderStatus)
                    // let's ignore it
                } else {
                    mBufferReady = true
                    val encodedData = encoderOutputBuffers[encoderStatus]
                            ?: throw RuntimeException("encoderOutputBuffer " + encoderStatus +
                                    " was null")
                    if (mBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG")
                        mBufferInfo!!.size = 0
                    }
                    if (mBufferInfo!!.size != 0) {
                        if (!mMuxerStarted) {
                            throw RuntimeException("muxer hasn't started")
                        }

                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encodedData.position(mBufferInfo!!.offset)
                        encodedData.limit(mBufferInfo!!.offset + mBufferInfo!!.size)
                        mMuxer!!.writeSampleData(mTrackIndex, encodedData, mBufferInfo!!)
                        if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo!!.size + " bytes to muxer")
                    }
                    mEncoder!!.releaseOutputBuffer(encoderStatus, false)
                    if (mBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        if (!endOfStream) {
                            Log.w(TAG, "reached end of stream unexpectedly")
                        } else {
                            if (VERBOSE) Log.d(TAG, "end of stream reached")
                        }
                        break // out of while
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /*-------------------------------------
        //audio Encode
        ---------------------------------------*/


        //'//private MediaCodec mEncoderAutdio;
        //private int mTrackIndexAudio;
        //private MediaCodec.BufferInfo mBufferInfoAudio;

//        if(false)
//        {
//            ByteBuffer[] encoderOutputBuffers = mEncoderAutdio.getOutputBuffers();
//            while (mMuxerStarted || mMuxerAudioReady== false) {
//                int encoderStatus = mEncoderAutdio.dequeueOutputBuffer(mBufferInfoAudio, TIMEOUT_USEC);
//                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    // no output available yet
//                    if (!endOfStream) {
//                        break;      // out of while
//                    } else {
//                        if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
//                    }
//                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                    // not expected for an encoder
//                    encoderOutputBuffers = mEncoderAutdio.getOutputBuffers();
//                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    // should happen before receiving buffers, and should only happen once
//                    if (mMuxerStarted) {
//                        throw new RuntimeException("format changed twice");
//                    }
//                    MediaFormat newFormat = mEncoderAutdio.getOutputFormat();
//                    Log.d(TAG, "encoder output format changed: " + newFormat);
//
//
//
//                            // now that we have the Magic Goodies, start the muxer
//                    mTrackIndexAudio = mMuxer.addTrack(newFormat);
//
//                    mMuxerAudioReady= true;
//
//                    if( mMuxerAudioReady && mMuxerVideoReady) {
//                        mMuxer.start();
//                        mMuxerStarted = true;
//                    }
//
//                } else if (encoderStatus < 0) {
//                    Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
//                            encoderStatus);
//                    // let's ignore it
//                } else {
//
//
//
//
//                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
//                    if (encodedData == null) {
//                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
//                                " was null");
//                    }
//
//                    //if ((mBufferInfoAudio.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
//                        // The codec config data was pulled out and fed to the muxer when we got
//                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
//                   //     if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                   //     mBufferInfoAudio.size = 0;
//                   // }
//
//               //     mBufferInfoAudio.presentationTimeUs = computePresentationTimeNsec(runningCount);
//
//                   // if (mBufferInfoAudio.size != 0) {
//                   //     if (!mMuxerStarted) {
//                   //         throw new RuntimeException("muxer hasn't started");
//                   //     }
//
//                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
//                    //    encodedData.position(mBufferInfoAudio.offset);
//               //         encodedData.limit(mBufferInfoAudio.offset + mBufferInfoAudio.size);
////
//               //         mMuxer.writeSampleData(mTrackIndexAudio, encodedData, mBufferInfoAudio);
//                //        if (VERBOSE) Log.d(TAG, "sent " + mBufferInfoAudio.size + " bytes to muxer");
//                 //   }
//
//                    mBufferInfo.presentationTimeUs = getPTSUs();
//                    mMuxer.writeSampleData(mTrackIndexAudio, encodedData, mBufferInfoAudio);
//
//                    prevOutputPTSUs = mBufferInfo.presentationTimeUs;
//
//
//                    mEncoderAutdio.releaseOutputBuffer(encoderStatus, false);
//
//                    if ((mBufferInfoAudio.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                        if (!endOfStream) {
//                            Log.w(TAG, "reached end of stream unexpectedly");
//                        } else {
//                            if (VERBOSE) Log.d(TAG, "end of stream reached");
//                        }
//                        break;      // out of while
//                    }
//                }
//
//            }
//        }
    }

    private val prevOutputPTSUs: Long = 0
    // presentationTimeUs should be monotonic

    // otherwise muxer fail to write
    protected val pTSUs: Long
        protected get() {
            var result = System.nanoTime() / 1000L

            // presentationTimeUs should be monotonic

            // otherwise muxer fail to write
            if (result < prevOutputPTSUs) result = prevOutputPTSUs - result + result
            return result
        }
    /**
     * Generates a frame of data using GL commands.  We have an 8-frame animation
     * sequence that wraps around.  It looks like this:
     * <pre>
     * 0 1 2 3
     * 7 6 5 4
    </pre> *
     * We draw one of the eight rectangles and leave the rest set to the clear color.
     */
    /**
     * Generates the presentation time for frame N, in nanoseconds.
     *
     * private static long computePresentationTimeNsec(int frameIndex) {
     * final long ONE_BILLION = 1000000000;
     * return frameIndex * ONE_BILLION / FRAME_RATE;
     * }
     */
    /**
     * Holds state associated with a Surface used for MediaCodec encoder input.
     *
     *
     * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses that
     * to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to be sent
     * to the video encoder.
     *
     *
     * This object owns the Surface -- releasing this will release the Surface too.
     */
    private class CodecInputSurface(surface: Surface?, bUseAntialise: Boolean) {
        //private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        //private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private var mSurface: Surface?

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
         */
        private fun eglSetup(bUseAntialise: Boolean) {
            //mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            //if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            //   throw new RuntimeException("unable to get EGL14 display");
            //}/
            //int[] version = new int[2];
            //if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            //    throw new RuntimeException("unable to initialize EGL14");
            //}
            //int [] mValue = new int[1];

            // Configure EGL for recording and OpenGL ES 2.0.
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (bUseAntialise) {
                val attribList = intArrayOf(
                        EGL14.EGL_RED_SIZE, 8,
                        EGL14.EGL_GREEN_SIZE, 8,
                        EGL14.EGL_BLUE_SIZE, 8,
                        EGL14.EGL_ALPHA_SIZE, 8,
                        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                        EGL_RECORDABLE_ANDROID, 1,  //   EGL14.EGL_SAMPLE_BUFFERS, 1,
                        //   EGL14.EGL_SAMPLES, 4,  // This is for 4x MSAA.
                        EGL14.EGL_NONE
                )
                EGL14.eglChooseConfig(
                    mEGLDisplay, attribList, 0, configs, 0, configs.size,
                        numConfigs, 0)
                val numCo = numConfigs[0]
                if (numCo <= 0) {
                    val attribList2 = intArrayOf(
                            EGL14.EGL_RED_SIZE, 8,
                            EGL14.EGL_GREEN_SIZE, 8,
                            EGL14.EGL_BLUE_SIZE, 8,
                            EGL14.EGL_ALPHA_SIZE, 8,
                            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                            EGL_RECORDABLE_ANDROID, 1,
                            EGL14.EGL_NONE
                    )
                    EGL14.eglChooseConfig(
                        mEGLDisplay, attribList2, 0, configs, 0, configs.size,
                            numConfigs, 0)
                    checkEglError("eglCreateContext RGB888+recordable ES2")
                }
            } else {
                val attribList = intArrayOf(
                        EGL14.EGL_RED_SIZE, 8,
                        EGL14.EGL_GREEN_SIZE, 8,
                        EGL14.EGL_BLUE_SIZE, 8,
                        EGL14.EGL_ALPHA_SIZE, 8,
                        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                        EGL_RECORDABLE_ANDROID, 1,
                        EGL14.EGL_NONE
                )
                EGL14.eglChooseConfig(
                    mEGLDisplay, attribList, 0, configs, 0, configs.size,
                        numConfigs, 0)
                val numCo = numConfigs[0]
                checkEglError("eglCreateContext RGB888+recordable ES2")
            }


            // Configure context for OpenGL ES 2.0.
            val attrib_list = intArrayOf(
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            )
            //mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
            //        attrib_list, 0);
            // checkEglError("eglCreateContext");

            // Create a window surface, and attach it to the Surface we received.
            val surfaceAttribs = intArrayOf(
                    EGL14.EGL_NONE
            )
            mEGLSurface = EGL14.eglCreateWindowSurface(
                mEGLDisplay, configs[0], mSurface,
                    surfaceAttribs, 0)
            checkEglError("eglCreateWindowSurface")
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  Also releases the
         * Surface that was passed to our constructor.
         */
        fun release() {
            if (mEGLDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface)
                //EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                //EGL14.eglReleaseThread();
                //EGL14.eglTerminate(mEGLDisplay);
            }
            mSurface!!.release()
            mEGLDisplay = EGL14.EGL_NO_DISPLAY
            mEGLContext = EGL14.EGL_NO_CONTEXT
            mEGLSurface = EGL14.EGL_NO_SURFACE
            mSurface = null
        }

        /**
         * Makes our EGL context and surface current.
         */
        fun makeCurrent() {
            if (mEGLSurface == null) {
                return
            }
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)
            checkEglError("eglMakeCurrent")
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         */
        fun swapBuffers(): Boolean {
            if (mEGLDisplay == null || mEGLSurface == null) {
                return false
            }
            val result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)
            checkEglError("eglSwapBuffers")
            return result
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         */
        fun setPresentationTime(nsecs: Long) {
            if (mEGLDisplay == null || mEGLSurface == null) {
                return
            }
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs)
            checkEglError("eglPresentationTimeANDROID")
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         */
        private fun checkEglError(msg: String) {
            var error: Int
            if (EGL14.eglGetError().also { error = it } != EGL14.EGL_SUCCESS) {
                throw RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
            }
        }

        companion object {
            private const val EGL_RECORDABLE_ANDROID = 0x3142
        }

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        init {
            if (surface == null) {
                throw NullPointerException()
            }
            mSurface = surface
            eglSetup(bUseAntialise)
        }
    }

    companion object {
        var mEGLDisplay: EGLDisplay? = null
        var mEGLContext: EGLContext? = null
        var mEGLSurface = EGL14.EGL_NO_SURFACE
        var bRunning = false
        var runningCount = 0
        const val DEBUG = false
        private const val TAG = "EncodeAndMuxTest"
        private const val VERBOSE = false // lots of logging

        // where to put the output file (note: /sdcard requires WRITE_EXTERNAL_STORAGE permission)
        //  private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();
        // parameters for the encoder
        private const val MIME_TYPE = "video/avc" // H.264 Advanced Video Coding
        private var FRAME_RATE = 15 // 15fps
        private const val IFRAME_INTERVAL = 1 // every 1 seconds
        private fun selectAudioCodec(mimeType: String): MediaCodecInfo? {
            if (DEBUG) Log.v(TAG, "selectAudioCodec:")
            var result: MediaCodecInfo? = null
            // get the list of available codecs
            val numCodecs = MediaCodecList.getCodecCount()
            LOOP@ for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) {    // skipp decoder
                    continue
                }
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (DEBUG) Log.i(TAG, "supportedType:" + codecInfo.name + ",MIME=" + types[j])
                    if (types[j].equals(mimeType, ignoreCase = true)) {
                        if (result == null) {
                            result = codecInfo
                            break@LOOP
                        }
                    }
                }
            }
            return result
        }
    }
}