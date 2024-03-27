/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.diveroid.camera.underfilter;

// Wrapper for native library

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

import com.diveroid.camera.GL2PreviewOption;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class GL2JNILib {


    static Bitmap mBlackBitmap = null;

    static int      mCameraWait  = 0;

    static public boolean USE_RENDERTEXTURE = true;

    public static class Callback
    {

        public void GL2JNIVideoPlayStart(boolean incode,final String savedFile) { }

        public void GL2JNIVideoPlaying(boolean incode, long currentMilisecond, long totalMiliseecond) { }

        public void GL2JNIVideoPlayFinished(boolean incode, final String savedFile) { }

        public void GL2JNIVideoPlayInterrupted(boolean incode,final String savedFile) { }

        public void GL2JNIVideoPlayStopted(boolean incode,final String savedFile) { }

        public void GL2JNIImageConvertFinished(final Bitmap bitmap, final String savedFile) { }

        public void GL2JNIRealtimeInterrupted(boolean incode,final String savedFile) { }

        public void GL2JNIRealtimeStoped(boolean incode,final String savedFile) { }

        public void GL2JNIRealtimeRecordStoped(final String savedFile) { }

    }


    public static Callback mCallback = null;

    public static Activity mContext = null;

    public static AssetManager assetManager;

    static {
        System.loadLibrary("NdkGlue");
    }


    //  static MediaRecorder recorder = null;//new MediaRecorder();


    static GL2JNIVideoWriter writer = null;//new GL2JNIVideoWriter();

    /**
     * @param width the current view width
     * @param height the current view height
     */
    public static native void init(int width, int height);


    static void clearViewMode()
    {

        if( VIEW_REALTIME == mInputMode) {

            ++mCameraWait;
            GL2JNILib.mContext.runOnUiThread(() -> {
                GL2JNICamera2.INSTANCE.stopCamera();//s.startCamera(mContext, width, height, surface, surfaceTexture);
                --mCameraWait;
            });

            GL2JNIVideoReader.INSTANCE.setMSurfaceTexture(null);
            GL2JNIVideoReader.INSTANCE.setMSurface(null);


            if (writer != null) {
                writer.stopEncode();
                writer = null;
                mCallback.GL2JNIRealtimeInterrupted(true, mRecordFile); //정상 적인 상태로  끝난것 아니기에 예외 처리 필요함
            } else {
                mCallback.GL2JNIRealtimeInterrupted(false, mRecordFile); //정상 적인 상태로  끝난것 아니기에 예외 처리 필요함
            }

            mInputMode = VIEW_IMAGE;
        }
        else if( VIEW_MOVIE == mInputMode) {
            GL2JNILib.mContext.runOnUiThread(() -> {
                // GL2JNICamera2.stopCamera();//s.startCamera(mContext, width, height, surface, surfaceTexture);
            });
            GL2JNIVideoReader.INSTANCE.Stop();

            if (writer != null) {
                writer.stopEncodeFilePlay(mInputFile);
                writer = null;
                mCallback.GL2JNIVideoPlayInterrupted(true, mRecordFile); //정상 적인 상태로  끝난것 아니기에 예외 처리 필요함
            } else {
                mCallback.GL2JNIVideoPlayInterrupted(false, "");
            }

            mInputPlaying = false;
            frameChanged = 0;
            oldRealtimeTimestamp = -1;

            GL2JNIVideoReader.INSTANCE.setMSurfaceTexture(null);
            GL2JNIVideoReader.INSTANCE.setMSurface(null);

            mInputMode = VIEW_IMAGE;
        }
    }


    static Thread recordingThread = new Thread(() -> {
        // while (true) {
        //    buf.clear();
        //   int readBytes = GL2JNICamera2.audioRecord.read(buf, SAMPLES_PER_FRAME);
        //    writer.writeToFileAudio(0, buf, SAMPLES_PER_FRAME);
        // }
    });

    public static void beforeStep(
            android.opengl.EGLDisplay mEGLDisplay14 ,
            android.opengl.EGLContext mEGLContext14
    ) {
        if (openGLCommands.size() > 0) {
            OpenGLCommand command = openGLCommands.get(0);
            openGLCommands.remove(0);

            if (command.mode == VIEW_REALTIME) {
                clearViewMode();
                mInputMode = VIEW_REALTIME;

                changeRenderingMode(VIEW_MOVIE, command.textureWidth, command.textureHeight, 0, command.renderTextureWidth, command.renderTextureHeight);

                final int width = command.textureWidth;
                final int height =  command.textureHeight;

                mInputWidth = width;
                mInputHeight = height;
                mRenderTextureWidth = command.renderTextureWidth;
                mRenderTextureHeight = command.renderTextureHeight;

                {
                    int textureId = getMainTextureId();
                    final  SurfaceTexture surfaceTexture = new SurfaceTexture(textureId);
                    final  Surface surface = new Surface(surfaceTexture);
                    //  mediaPlayer.setSurface(surface);
                    //   surface.release();
                    GL2JNIVideoReader.INSTANCE.setMSurfaceTexture(surfaceTexture);
                    GL2JNIVideoReader.INSTANCE.setMSurface(surface);

                    ++mCameraWait;
                    GL2JNILib.mContext.runOnUiThread(() -> {
                        GL2JNICamera2.INSTANCE.startCamera(mContext, command.frameRate, surface, surfaceTexture);
                        mInputModeIsFront = GL2JNICamera2.INSTANCE.getMIsSelfie();
                        --mCameraWait;
                    });
                }
            }
            else if (command.mode == VIEW_MOVIE) {
                clearViewMode();
                //videoRunning =false;
                mRecordMode = command.recordMode;
                mRecordFile = command.recordFile;//outputImage;

                mInputWidth = command.textureWidth;
                mInputHeight = command.textureHeight;
                mRenderTextureWidth = command.textureWidth;
                mRenderTextureHeight = command.textureHeight;

                mInputMode = VIEW_MOVIE;
                firstVideoPlayTimestamp = -1;

                changeRenderingMode(VIEW_MOVIE, command.textureWidth, command.textureHeight, command.textureRotate, command.textureWidth, command.textureHeight);

                {
                    int textureId = getMainTextureId();
                    if( GL2JNIVideoReader.INSTANCE.getMSurfaceTexture() == null) {
                        SurfaceTexture surfaceTexture = new SurfaceTexture(textureId);
                        Surface surface = new Surface(surfaceTexture);
                        //  mediaPlayer.setSurface(surface);
                        //   surface.release();
                        GL2JNIVideoReader.INSTANCE.setMSurfaceTexture(surfaceTexture);
                        GL2JNIVideoReader.INSTANCE.setMSurface(surface);
                    }
                }

                int textureId = getMainTextureId();

                MediaFormat audioFormat = GL2JNIVideoReader.INSTANCE.Start(command.path, textureId);
                mInputPlaying = true;
                oldRealtimeTimestamp = -1;
                GL2JNILib.frameChanged = 0;

                mInputFile = command.path;

                GL2JNIVideoWriter.Companion.setMEGLDisplay(mEGLDisplay14);
                GL2JNIVideoWriter.Companion.setMEGLContext(mEGLContext14);

                if (command.recordFile.length() > 0) {
                    writer = new GL2JNIVideoWriter();
                    writer.startEncode(command.textureWidth, command.textureHeight, command.frameRate, command.recordFile, command.textureRotate, true, audioFormat);//.testEncodeVideoToMp4();
                    mRecordMode = RECORD_MOVIE_RECORDING;
                }
                // mEGLDisplay14, mEGLContext14

                //android.opengl.EGLDisplay mEGLDisplay14 ,
                //android.opengl.EGLContext mEGLContext14
            }
            else if (command.mode == CMD_STOP_MOVIE) {

                GL2JNIVideoReader.INSTANCE.Stop();

                if( writer != null )
                {
                    writer.stopEncodeFilePlay(mInputFile);
                    writer = null;

                    mCallback.GL2JNIVideoPlayStopted(true,"");

                }else
                {
                    mCallback.GL2JNIVideoPlayStopted(false,"");
                }

                mInputPlaying = false;
                frameChanged = 0;
                oldRealtimeTimestamp = -1;
                mInputMode = VIEW_IMAGE;
                mRecordMode = RECORD_NONE;
                // EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);
            } else if (command.mode == CMD_STOP_REALTIME ) {
                // GL2JNIVideoReader.Stop();
                if( writer != null ) {
                    writer.stopEncode();
                    writer = null;
                    mCallback.GL2JNIRealtimeStoped(true,mRecordFile);
                }else
                {
                    mCallback.GL2JNIRealtimeStoped(false,mRecordFile);
                }

                ++mCameraWait;
                GL2JNILib.mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GL2JNICamera2.INSTANCE.stopCamera();//s.startCamera(mContext, width, height, surface, surfaceTexture);
                        --mCameraWait;
                    }
                });

                if( mBlackBitmap == null )
                {
                    mBlackBitmap = createImage(32,32,0);
                }

                if( mBlackBitmap != null )
                {
                    GL2JNILib.setTexture(mBlackBitmap);
                }


                changeRenderingMode(VIEW_IMAGE, 32, 32,0,
                        32, 32);

                mInputMode = VIEW_IMAGE;

            }else if (command.mode == CMD_STOP_SAVE_VIDEO ) {

                // GL2JNIVideoReader.Stop();

                if( writer != null )
                {
                    // if( recorder != null) {
                    //     recorder.stop();
                    //     recorder.reset();
                    //     recorder.release();
                    //     recorder = null;
                    // }

                    writer.stopEncode();
                    writer = null;


                    mCallback.GL2JNIRealtimeRecordStoped(mRecordFile);

                }

                mRecordMode = RECORD_NONE;

            }else {
                //View Image
                clearViewMode();

                if( command.initBitmap != null )
                {
                    GL2JNILib.setTexture(command.initBitmap);
                }


                Log.d("csson", "beforeStep: view Image");
//                mContext.runOnUiThread(() -> {
                    changeRenderingMode(VIEW_IMAGE, command.textureWidth, command.textureHeight,0,
                            command.textureWidth, command.textureHeight);
//                });

                mRecordModeImage = command.recordMode;
                mRecordFile = command.recordFile;//outputImage;
                mInputWidth = command.textureWidth;
                mInputHeight = command.textureHeight;
                mRenderTextureWidth = command.textureWidth;
                mRenderTextureHeight = command.textureHeight;

            }
        }

        //SurfaceTexture surfaceTexture = new SurfaceTexture(textureId);
        //Surface surface = new Surface(surfaceTexture);
        //mediaPlayer.setSurface(surface);
        //surface.release();

        if (VIEW_MOVIE == mInputMode) {
            if (GL2JNIVideoReader.INSTANCE.getRunning()) {
                // if (GL2JNIVideoReader.mSurfaceTexture != null) {
                //       GL2JNIVideoReader.mSurfaceTexture.updateTexImage();
                // }

            } else {
                //if (writer != null) {
                //   writer.stopEncodeFilePlay(mInputFile);//;stopEncode();
                //   writer = null;
                //}
            }
        }

        if( mRecordModeImage == RECORD_IMAGE )
        {
//            if( hasRenderTextureData() )
//            {
//                try
//                {
//                    //Bitmap bitmap = Bitmap.createBitmap(mRenderTextureWidth, mRenderTextuerHeight, Bitmap.Config.ARGB_8888);
//
//                    //Canvas canvas = new Canvas(bitmap);
//                    //bitmap.eraseColor(Color.BLACK);
//
//                    ByteBuffer buffer = mRecordModeImageBuffer;//ByteBuffer.allocateDirect(mRecordModeImageBitmap.getByteCount());
//                    // buffer.order(ByteOrder.LITTLE_ENDIAN);  ByteBuffer buffer = ByteBuffer.allocateDirect(mRecordModeImageBitmap.getByteCount());
//
//                    fillTextureData(mRenderTextureWidth, mRenderTextuerHeight, buffer);
//                    mRecordModeImageBitmap.copyPixelsFromBuffer(buffer);
//
//                    //fillBitmapPixel(bitmap);
//
//
//                    if( mCallback != null) {
//                        mCallback.GL2JNIImageConvertFinished(mRecordModeImageBitmap, mRecordFile );
//
//                    }
//
//                } catch (Exception e) { }
//            }
//
//            mRecordModeImage = RECORD_NONE;

        }else if(  mRecordMode == RECORD_MOVIE)
        {
            mRecordMode = RECORD_MOVIE_RECORDING;

            oldRealtimeTimestampRecordStart = -1;

            GL2JNIVideoWriter.Companion.setMEGLDisplay(mEGLDisplay14);
            GL2JNIVideoWriter.Companion.setMEGLContext(mEGLContext14);

            writer = new GL2JNIVideoWriter();
            writer.startEncode(mRecordWidth, mRecordHeight, mRecordFrame, mRecordFile, 0, false, mMediaAudioFormat);


            String recordFile = mRecordFile + ".audio";

            // try {
//
            //              recorder = new MediaRecorder();
            //            recorder.setAudioSource(MediaRecorder.AudioSource.MIC); //----------- (1)
            //          recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT); // -- (2)
            //        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // ------(3)
            //      recorder.setOutputFile(recordFile);
            //     recorder.prepare(); // -----------------------------------------------(4)
            //     recorder.start();   // Recording is now started ----------------------(5)

            //}catch (Exception e)
            //{

            //}


            //recordingThread.start();;

        }
    }

    public static Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }


    public static native boolean hasRenderTextureData();
    public static native void  fillBitmapPixel(Bitmap bitmap);


    public static native void step();
    public static native void step1(int isFront);
    public static native void step2(int isFront);
    public static native void stepnorendertexture();
    public static native void stepEncode(float dstWidth, float dstHeight);
    public static native void stepEncodenorendertexture(float dstWidth, float dstHeight);
    public static void afterStep()
    {

    }


    //record Audio
    static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel


    static final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);



    static int frameChanged = 0;
    static long     frameChangePresentationTimeUs = 0;


    static long oldRealtimeTimestamp = -1;
    static long oldRealtimeTimestampRecordStart = -1;


    static long firstVideoPlayTimestamp = -1;
    static long firstVideoPlayStartTime = 0;


    static public void onDrawFrame( android.opengl.EGLDisplay mEGLDisplay14 ,
                                    android.opengl.EGLContext mEGLContext14,  EGLSurface mEGLSurface ) {

//        Log.d("GL@JNILib", "onDrawFrame");
        EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);

        beforeStep(mEGLDisplay14, mEGLContext14);


        EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);

        // }

        if (VIEW_IMAGE == mInputMode) {
            GL2JNILib.step();
        } else if (VIEW_MOVIE == mInputMode) {

            if (mInputPlaying) {
                long timestamp = 0;
                while (GL2JNILib.frameChanged != 1) {
                    if (!GL2JNIVideoReader.INSTANCE.getRunning()) //플레이가 종료 된 상태 이다.
                    {
                        if (writer != null) {
                            writer.stopEncodeFilePlay(mInputFile);//.writeToFile(timestamp);
                            writer = null;
                            mInputPlaying = false;
                            mCallback.GL2JNIVideoPlayFinished(true, mRecordFile);
                        } else {
                            mCallback.GL2JNIVideoPlayFinished(false, "");
                        }

                        GL2JNILib.step();

                        EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);
                        return;
                    }

                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                    }

                }

                try {
                    GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().updateTexImage();
                    timestamp = GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().getTimestamp();
                } catch (Exception e) {
                    // GL2JNIVideoReader.running = false;
                }


                GL2JNILib.frameChanged = 0;
                //notify

                // if (GL2JNIVideoReader.mSurfaceTexture != null) {
                // /    GL2JNIVideoReader.mSurfaceTexture.updateTexImage();
                // /    timestamp = GL2JNIVideoReader.mSurfaceTexture.getTimestamp();
                // }

                //try {
                //    Thread.sleep(1000);
                //} catch (Exception e) {

                //}

                GL2JNILib.step();


                if( firstVideoPlayTimestamp  < 0)
                {
                    firstVideoPlayTimestamp =timestamp;
                    firstVideoPlayStartTime = System.currentTimeMillis();
                }

                mCallback.GL2JNIVideoPlaying(writer != null,  (timestamp - firstVideoPlayTimestamp) / 1000/1000,  mTotalMilisecond);


                if (writer != null) {
                    EGL14.eglMakeCurrent(mEGLDisplay14, GL2JNIVideoWriter.Companion.getMEGLSurface(), writer.Companion.getMEGLSurface(), mEGLContext14);

                    GL2JNILib.stepEncode(writer.getDstWidth(), writer.getDstHeight());
                    writer.writeToFile(timestamp);


                    EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);

                }else
                {

                    long playTimeMili = System.currentTimeMillis() - firstVideoPlayStartTime;
                    long movieTimeMili = (timestamp - firstVideoPlayTimestamp)/1000/1000;

                    long diff = movieTimeMili - playTimeMili;
                    if( diff > 0 )
                    {
                        try {
                            Thread.sleep(diff);
                        } catch (Exception e) {

                        }
                    }
                }

                // }else
                //  {

                //  }
            }else
            {

                GL2JNILib.step();

            }

        }else  if( VIEW_REALTIME == mInputMode) {

            EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);

            //if (GL2JNIVideoReader.running) {

            long timestamp = 0;

            if (GL2JNIVideoReader.INSTANCE.getMSurfaceTexture() != null) {

                //GL2JNIVideoReader.mSurfaceTexture.
                GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().updateTexImage();
                timestamp = GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().getTimestamp();


                while( oldRealtimeTimestamp == timestamp)
                {

                    //
                    try
                    {
                        Thread.sleep(1);
                    }catch (Exception e)
                    {

                    }

                    GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().updateTexImage();
                    timestamp = GL2JNIVideoReader.INSTANCE.getMSurfaceTexture().getTimestamp();

                }
            }

            if(USE_RENDERTEXTURE) {

                GL2JNILib.step1(mInputModeIsFront ? 1 : 0);
                GL2JNILib.step2(mInputModeIsFront ? 1 : 0);

            }else {

                GL2JNILib.stepnorendertexture();
            }

            if (writer != null) {
                EGL14.eglMakeCurrent(mEGLDisplay14, GL2JNIVideoWriter.Companion.getMEGLSurface(), writer.Companion.getMEGLSurface(), mEGLContext14);


                if(USE_RENDERTEXTURE) {
                    GL2JNILib.stepEncode(writer.getDstWidth(), writer.getDstHeight());
                }else {
                    GL2JNILib.stepEncodenorendertexture(writer.getDstWidth(), writer.getDstHeight());
                }

                if( oldRealtimeTimestampRecordStart < 0)
                {
                    oldRealtimeTimestampRecordStart = timestamp;
                }
                writer.writeToFile(timestamp - oldRealtimeTimestampRecordStart);


                EGL14.eglMakeCurrent(mEGLDisplay14, mEGLSurface, mEGLSurface, mEGLContext14);

            }
        }

        GL2JNILib.afterStep();
        //   surfaceTexture.updateTexImage();

    }

    public static native void tick();
    public static native void onDestroy();
    public static void onCreate( AssetManager assetManager ){
        GL2JNILib.onCreate(assetManager , GL2PreviewOption.IS_DEFAULT_FILTER_OPTION);
    };
    public static native void onCreate( AssetManager assetManager, boolean defaultFilerOnOff );
    public static native void setTexture( Bitmap bitmap );


    public static native void setValueRfromG( float value );
    public static native void setValueRfromB( float value );
    public static native void setValueBreduceB( float value );
    public static native void setScale( float value );


    //public static native void changeRender

    public static native void changeRenderingMode( int renderingMode, int textureWidth, int textureHeight,int textureRotate,

                                                   int renderTextureWidth, int renderTextureHeight);
    public static native int  getMainTextureId( );

    public static native int  filterProcess( );

    public static native int  getOutTextureId( );

    public static native int  runFilterProcess( );
    public static native int  stopFilterProcess( );


    public static native int  insertUIImage(String uiName, Bitmap bitmap);



//    /GLuint getMainTextureId();

    // void changeRenderingMode(int renderingMode );
    // GLuint getMainTextureId();


    // public static native <ButeBuffer> void uploadTextureData(int width, int height, ButeBuffer data);

    public static native  void uploadTextureBitmapData(int width, int height, Bitmap bitmap);


    public static native  void fillTextureData(int width, int height, ByteBuffer data);

    static final int FILTER_OFF     = 0;
    static final int FILTER_ON      = 1;

    static final int VIEW_IMAGE         = 0;
    static final int VIEW_MOVIE         = 1;
    static final int VIEW_REALTIME      = 3;



    static  final int           CMD_START_IMAGE = 0;
    static  final int           CMD_START_MOVIE = 1;
    static final int            CMD_START_REALTIME= 3;

    //static  final int           CMD_STOP_IMAGE = 0;
    static  final int           CMD_STOP_MOVIE = 11;
    static final int            CMD_STOP_REALTIME= 13;


    static final int            CMD_STOP_SAVE_VIDEO = 14;


    static final int    RECORD_NONE  = 0;
    static final int    RECORD_IMAGE = 1;
    static final int    RECORD_MOVIE = 2;
    static final int    RECORD_MOVIE_RECORDING = 3;


    static int      mInputMode  = VIEW_IMAGE;
    static int      mInputWidth = 0;
    static int      mInputHeight = 0;
    static boolean mInputPlaying = false;
    static String   mInputFile = "";
    static boolean mInputModeIsFront = false;


    static int      mRenderTextureWidth = 0;
    static int      mRenderTextureHeight = 0;//,

    static int      mRecordMode = RECORD_NONE;
    static int      mRecordWidth = 0;
    static int      mRecordHeight = 0;
    static int      mRecordFrame = 0;


    static String   mRecordFile = "";
    static  MediaFormat mMediaAudioFormat;

    static int      mRecordModeImage = RECORD_NONE;
    static Bitmap   mRecordModeImageBitmap = null;
    static ByteBuffer      mRecordModeImageBuffer = null;//


    public static long mTotalMilisecond = 100;


    //public static boolean videoRunning = false;
    public static void startVideo(String path, String outputImage)
    {
        //if( videoRunning == true )
        //{
        //   return;
        //} int textureWidth, int textureHeigh

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

        int rot2 = 0 ;
        try {
            rot2 = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
//            DLog.INSTANCE.d( "VideoOrientation", "orientation="+rot2);
            GL2JNILib.setVideoOrientation(rot2);
        }catch (Exception e)
        {
        }


        //int frame = 0 ;
        //try {
        //    rot2 = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY.METADATA_KEY_VIDEO_ROTATION));
        //}catch (Exception e)
        //{
        //}

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time );
        mTotalMilisecond = timeInMillisec;

        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OpenGLCommand command = new OpenGLCommand();
        command.mode = VIEW_MOVIE;
        command.path = path;
        command.textureWidth = width;
        command.textureHeight = height;
        command.frameRate = 30;
        command.textureRotate = rot2;

        if( outputImage.length() > 0) {
            command.recordMode = RECORD_MOVIE;
            command.recordFile = outputImage;
        }
        openGLCommands.add(command);
    }


    public static void startRealtimeView( int width , int height, int renderTextureWidth, int renderTextuerHeight, int frame)
    {

        OpenGLCommand command = new OpenGLCommand();
        command.mode = VIEW_REALTIME;


        command.textureWidth = width;
        command.textureHeight = height;
        command.renderTextureWidth = renderTextureWidth;
        command.renderTextureHeight = renderTextuerHeight;
        command.frameRate = frame;

        openGLCommands.add(command);
        // TODO: 디버그 로그 관련 처리 추가 필요.
//        DLog.INSTANCE.dd("startRealtimeView");
        GL2JNILib.setVideoOrientation(0);

    }

    public static void stopRealtimeView() {
        OpenGLCommand command = new OpenGLCommand();
        command.mode = CMD_STOP_REALTIME;

        //command.textureWidth = width;
        //c//ommand.textureHeight = height;
        command.frameRate = 30;

        openGLCommands.add(command);

    }


    public static void startImage(Bitmap bitmap,  int w, int h,  String outputImage) {
        //videoRunning = false;
        OpenGLCommand command = new OpenGLCommand();
        command.mode = VIEW_IMAGE;

        command.initBitmap = bitmap;
        command.textureWidth = w;
        command.textureHeight = h;
        command.renderTextureWidth = w;
        command.renderTextureHeight = h;

        if( outputImage.length() > 0) {
            command.recordMode = RECORD_IMAGE;
            command.recordFile = outputImage;
        }

        openGLCommands.add(command);

    }


    static class OpenGLCommand
    {

        int mode = 0;
        String path = "";
        Bitmap initBitmap = null;
        int textureWidth = 0;
        int textureHeight = 0;
        int textureRotate = 0;
        int renderTextureWidth = 0;
        int renderTextureHeight = 0;

        int frameRate = 0;


        int     recordMode = RECORD_NONE;
        String  recordFile = "";
    };

    static ArrayList<OpenGLCommand> openGLCommands = new ArrayList<OpenGLCommand>();


    public static void stopVideo()
    {
        OpenGLCommand command = new OpenGLCommand();
        command.mode = CMD_STOP_MOVIE;

        openGLCommands.add(command);
    }

    public static void saveImage()
    {
        GL2JNICamera2.INSTANCE.takePicture();
    }

    public static void saveMovie(String recordingFile, MediaFormat mediaAudioFormat, int recordWidth, int recordHeight, int frame )
    {
        mRecordFile = recordingFile;
        mRecordMode = RECORD_MOVIE;
        mRecordWidth = recordWidth;
        mRecordHeight = recordHeight;
        mRecordFrame = frame;

        mMediaAudioFormat = mediaAudioFormat;
    }


    public static void saveMovieStop()
    {
        OpenGLCommand command = new OpenGLCommand();
        command.mode = CMD_STOP_SAVE_VIDEO;

        openGLCommands.add(command);
    }

    public static native void debugShowOriginMini( int bEnable);
    public static native void debugShowHistogram( int bEnable);

    /**
     * 아래 마진조정 시 애니메이션 외에는 정상작동한다. 아래 조건일 경우에 애니메이션에 문제 있을 수 있음
     * 조건 : texture생산자의 width/height 비율와 결과물의 width/height 비율이 다를 경우
     * ex) 실시간보정에서 Camera로 받는 texture와 저장되는 texture의 비율이 다름
     *
     * 마진조정을 아래 메소드로 사용하지 말고 부모ViewGroup의 layoutParam을 조정할것
     */
    public static native void bottomMargin(float margin);
    public static native void topMargin(float margin);
    public static native void leftMargin(float margin);
    public static native void rightMargin(float margin);
    public static native void setFilterOn(int bOn);
    public static native int getFilterOn();


    public static native int getFilterOdddn();

    public static native int setClearColor(float r, float g, float b);


    public static native void startAnimation();
    public static native void setVideoOrientation(int videoOrientation);

}
