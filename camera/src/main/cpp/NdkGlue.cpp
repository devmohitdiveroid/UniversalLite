/*
 * Copyright (C) 2009 The Android Open Source Project
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

// OpenGL ES 2.0 code
#include <stdint.h>
#include <jni.h>
#include <android/bitmap.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <string>
#include <vector>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <sys/errno.h>
#include <string.h>
#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"


#include "GLES2Lesson.h"
#include "NdkGlue.h"

#include "android_asset_operations.h"

#include "libyuv.h"
#include "libyuv/convert_argb.h"
#include "libyuv/convert_from.h"
#include "FilterPreProcessor.h"
#include "AquaShader.h"
#include "RenderedShader.h"

#include "AnimationShader.h"
#include "FragmentShader.h"
#include <jni.h>
#include <jni.h>


std::string gVertexShader;
std::string gFragmentShader;
GLES2Lesson *gles2Lesson = nullptr;


static unsigned char  qhdBuffer[4096* 4096 * 4 ];
static int *pixels = (int * )&qhdBuffer[0] ;


void loadShaders(JNIEnv *env, jobject &obj) {
    {
        AAssetManager *asset_manager = AAssetManager_fromJava(env, obj);
        FILE *fd;
        fd = android_fopen("vertex.glsl", "r", asset_manager);
        gVertexShader = readToString(fd);
        fclose(fd);

        gFragmentShader = fragmentString;
        std::string uv = fragmentuvString;
        std::string effect = fragmentuv_effectString;

        AquaShader::strVertexShader = gVertexShader;
        AquaShader::strPixelShader  = gFragmentShader;
        AquaShader::strPixelShaderUV  = uv;

        AquaShader::strPixelShaderUV_Effect  = effect;

    }


    {
        AAssetManager *asset_manager = AAssetManager_fromJava(env, obj);
        FILE *fd;
        fd = android_fopen("rendered_vertex.glsl", "r", asset_manager);
        gVertexShader = readToString(fd);
        fclose(fd);
        fd = android_fopen("rendered_fragment.glsl", "r", asset_manager);
        gFragmentShader = readToString(fd);
        fclose(fd);
        fd = android_fopen("rendered_fragmentuv.glsl", "r", asset_manager);
        std::string uv = readToString(fd);
        fclose(fd);

        RenderedShader::strVertexShader = gVertexShader;
        RenderedShader::strPixelShader  = gFragmentShader;
        RenderedShader::strPixelShaderUV  = uv;

    }






    {
        AAssetManager *asset_manager = AAssetManager_fromJava(env, obj);
        FILE *fd;
        fd = android_fopen("animation_vertex.glsl", "r", asset_manager);
        gVertexShader = readToString(fd);
        fclose(fd);
//        fd = android_fopen("animation_fragment.glsl", "r", asset_manager);
        fd = android_fopen("animation_fragmentuv.glsl", "r", asset_manager);
        gFragmentShader = readToString(fd);
        fclose(fd);

        fd = android_fopen("animation_fragmentuv.glsl", "r", asset_manager);
        std::string uv = readToString(fd);
        fclose(fd);


        AnimationShader::strVertexShader = gVertexShader;
        AnimationShader::strPixelShader  = gFragmentShader;
        AnimationShader::strPixelShaderUV = uv;
    }





}

bool setupGraphics(int w, int h) {


    LOGE("setupGraphics %d %d", w,h);
    if( gles2Lesson == 0) {
        gles2Lesson = new GLES2Lesson();
    }

    return gles2Lesson->init(w, h, 0, 0);
    /*

    if( gles2Lesson == 0) {
        gles2Lesson = new GLES2Lesson();
        return gles2Lesson->init(w, h, 0, 0 );
    } else{
        return gles2Lesson->init(GLES2Lesson::mScreenWidth, GLES2Lesson::mScreenHeight, 0, 0);
    }
    */
}

void renderFrame() {
    if (gles2Lesson != nullptr) {
        gles2Lesson->render();
    }
}

void shutdown() {
    GLES2Lesson *local = gles2Lesson;
    gles2Lesson = nullptr;
    local->shutdown();
    delete local;
}




extern "C" {
    
    
    
JNIEXPORT void JNICALL Java_com_diveroid_camera_underfilter_GL2JNILib_onCreate(JNIEnv *env, jclass type,
                                                                                  jobject assetManager, jboolean defaultFilerOnOff) {
    LOGI("error %d", 1);
    loadShaders(env, assetManager);

    LOGI("error %d", 2);
    FilterPreProcessor::bEnableFilter = defaultFilerOnOff;
    FilterPreProcessor::inputValueScaleAll = 1.0f;
    GLES2Lesson::msClearColorR = 0.0f;
    GLES2Lesson::msClearColorG = 0.0f;
    GLES2Lesson::msClearColorB = 0.0f;

    GLES2Lesson::mTopMargin = 0.0f;
    GLES2Lesson::mBottomMargin = 0.0f;
    GLES2Lesson::mLeftMargin = 0.0f;
    GLES2Lesson::mRightMargin = 0.0f;

    GLES2Lesson::mAnimationUse = 0;
    GLES2Lesson::mMAnimationTime = 0;
}

JNIEXPORT void JNICALL Java_com_diveroid_camera_underfilter_GL2JNILib_init(JNIEnv *env, jclass type,
                                                                              jint width, jint height) {
    setupGraphics(width, height);
}

JNIEXPORT void JNICALL Java_com_diveroid_camera_underfilter_GL2JNILib_step(JNIEnv *env, jclass type) {
    renderFrame();
}

JNIEXPORT void JNICALL Java_com_diveroid_camera_underfilter_GL2JNILib_tick(JNIEnv *env, jclass type) {


}

JNIEXPORT void JNICALL Java_com_diveroid_camera_underfilter_GL2JNILib_onDestroy(JNIEnv *env, jclass type) {
    shutdown();
}

JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setTexture(JNIEnv *env, jclass type, jobject bitmap) {

    void *addr;
    AndroidBitmapInfo info;
    int errorCode;

    if ((errorCode = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0) {
        LOGI("error %d", errorCode);
    }

    if ((errorCode = AndroidBitmap_getInfo(env, bitmap, &info)) != 0) {
        LOGI("error %d", errorCode);
    }

    LOGI("bitmap info: %d wide, %d tall, %d ints per pixel", info.width, info.height, info.format);


    long size = info.width * info.height * info.format;
    pixels = (int*)&qhdBuffer[0];
    memcpy(pixels, addr, size * sizeof(int));

    GLES2Lesson::updateTextureDate( info.width, info.height, (char*)pixels);

    if ((errorCode = AndroidBitmap_unlockPixels(env, bitmap)) != 0) {
        LOGI("error %d", errorCode);
    }
}

};

extern "C"
JNIEXPORT jstring JNICALL
Java_com_diveroid_camera_underfilter_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO

    return env->NewStringUTF("dfsdfsdfsd");
}
extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setValueRfromG(JNIEnv *env, jclass type, jfloat value) {


    GLES2Lesson::setValueRfromG(value);

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setValueRfromB(JNIEnv *env, jclass type, jfloat value) {

    GLES2Lesson::setValueRfromB(value);


}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setValueBreduceB(JNIEnv *env, jclass type, jfloat value) {

    GLES2Lesson::setValueBreduceB(value);



}


extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_uploadTextureData(JNIEnv *env, jclass type, jint width, jint height,
                                                  jobject data_) {

    // TODO

 //   const char* str = (char*) env->GetDirectBufferAddress(data_);
//    printf(str); // Hello World!




}


extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_uploadTextureBitmapData(JNIEnv *env, jclass type, jint width,
                                                                          jint height, jobject bitmap) {



    void *addr;
    AndroidBitmapInfo info;
    int errorCode;

    if ((errorCode = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0) {
        LOGI("error %d", errorCode);
    }

    if ((errorCode = AndroidBitmap_getInfo(env, bitmap, &info)) != 0) {
        LOGI("error %d", errorCode);
    }

    LOGI("bitmap info: %d wide, %d tall, %d ints per pixel", info.width, info.height, info.format);


    //long size = info.width * info.height * info.format;
    //pixels = new int[size];
    //memcpy(pixels, addr, size * sizeof(int));


    long size = info.width * info.height * info.format;

    char *temp = (char*)addr;

    //pixels = new int[size];
    //int temmp0 = (int)temp[0];
    //int temmp1= (int)temp[1];
    //int temmp2 = (int)temp[2];


    memcpy(pixels, addr, size * sizeof(int));




    if ((errorCode = AndroidBitmap_unlockPixels(env, bitmap)) != 0) {
        LOGI("error %d", errorCode);
    }






    GLES2Lesson::updateTextureDate(info.width, info.height, (char*)pixels);



}






//LIBYUV_API
//int NV21ToABGR(const uint8_t* src_y,
//               int src_stride_y,
 //              const uint8_t* src_vu,
 //              int src_stride_vu,
 //              uint8_t* dst_abgr,
 //              int dst_stride_abgr,
 //              int width,
 //              int height) {
 //   return NV12ToARGBMatrix(src_y, src_stride_y, src_vu, src_stride_vu, dst_abgr,
 //                           dst_stride_abgr, &kYvuI601Constants, width, height);
//}

extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_uploadTextureYUVData(JNIEnv *env, jclass type, jint width,
                                                     jint height, jobject data_) {

    // TODO

    const unsigned char* pData = (unsigned char*) env->GetDirectBufferAddress(data_);
    //printf(str); // Hello World!

    //MediaCodec을 이용하여 H.264를 인코딩 디코딩 할 수 있습니다. 이 H.264 인코딩에 사용되는 색공간은 YUV 입니다.
    // YUV 색공간 중 안드로이드에서 인코딩/디코딩 가능한 색공간은 NV12와 I420 을 사용합니다.
    // Surface를 사용할 경우 RGB 데이터를 사용하면 되지만 RGB가 아닌 경우에는 YUV를 직접 변환하는 작업이 필요합니다.


   // src = sample + (src_width * crop_y + crop_x);
   // src_uv = sample + aligned_src_width * (src_height + crop_y / 2) + crop_x;
    // Call NV12 but with u and v parameters swapped.
    //r = NV21ToARGB(src, src_width,
    //               src_uv, aligned_src_width,
    //               crop_argb, argb_stride,
    //               crop_width, inv_crop_height);



    const unsigned  char *  src_uv =pData  + width * height;

    //libyuv::NV21ToARGB(pData, width,
    //        src_uv, width,
    //       qhdBuffer, width << 2,
    //       width, height);

   // GLES2Lesson::updateTextureDate( width, height,(char*) qhdBuffer);





}


extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getMainTextureId(JNIEnv *env, jclass type) {

    // TODO

    return GLES2Lesson::getMainTextureId();

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_changeRenderingMode(JNIEnv *env, jclass type, jint renderingMode,
                                                                      jint textureWidth, jint textureHeight, jint textureRotate,

                                                                      jint renderTextureWidth, jint renderTextureHeight


) {

    // TODO



    if( gles2Lesson != 0)
    {
        gles2Lesson->changeRenderingMode(renderingMode, textureWidth, textureHeight , textureRotate, renderTextureWidth, renderTextureHeight );// TODO
    }

    FilterPreProcessor::inputFlag = 0;
    FilterPreProcessor::outputFlag = 0;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getOutTextureId(JNIEnv *env, jclass type) {

    // TODO


    return GLES2Lesson::m_renderedTexture;
}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setEncodingSurface(JNIEnv *env, jclass type, jobject mSurface) {

    // TODO




}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_stepEncode(JNIEnv *env, jclass type, jfloat dstWidth, jfloat dstHeight) {

    // TODO



        if (gles2Lesson != nullptr) {
            gles2Lesson->renderEncode( dstWidth, dstHeight);
        }



}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_runFilterProcess(JNIEnv *env, jclass type) {


  FilterPreProcessor::run();

  return 0;




}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_filterProcess(JNIEnv *env, jclass type) {

    // TODO

    return 0;

}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_insertUIImage(JNIEnv *env, jclass type, jstring uiName_,
                                                                jobject bitmap) {
    const char *uiName = env->GetStringUTFChars(uiName_, 0);

    // TODO


    void *addr;
    AndroidBitmapInfo info;
    int errorCode;

    if ((errorCode = AndroidBitmap_lockPixels(env, bitmap, &addr)) != 0) {
        LOGI("error %d", errorCode);
    }

    if ((errorCode = AndroidBitmap_getInfo(env, bitmap, &info)) != 0) {
        LOGI("error %d", errorCode);
    }

    LOGI("bitmap info: %d wide, %d tall, %d ints per pixel", info.width, info.height, info.format);


    long size = info.width * info.height * info.format;
    //pixels = (int*)&qhdBuffer[0];
    //memcpy(pixels, addr, size * sizeof(int));


    std::vector<char> data(size * 4);

    memcpy(&data[0], addr, size * sizeof(int));

    GLES2Lesson::m_UIImages[uiName].width = info.width;
    GLES2Lesson::m_UIImages[uiName].height = info.height;
    GLES2Lesson::m_UIImages[uiName].buf =data;



    if ((errorCode = AndroidBitmap_unlockPixels(env, bitmap)) != 0) {
        LOGI("error %d", errorCode);
    }



    env->ReleaseStringUTFChars(uiName_, uiName);
}extern "C"
JNIEXPORT jobject JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getRenderTextureData(JNIEnv *env, jclass type) {

    // TODO

}extern "C"
JNIEXPORT jobject JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getRenderTextureBitmap(JNIEnv *env, jclass type) {

    // TODO





}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_fillBitmapPixel(JNIEnv *env, jclass type, jobject bitmap) {

    // TODO


    //const char *uiName = env->GetStringUTFChars(uiName_, 0);

    // TODO


    unsigned char *addr;
    AndroidBitmapInfo info;
    int errorCode;

    if ((errorCode = AndroidBitmap_lockPixels(env, bitmap, (void**)&addr)) != 0) {
        LOGI("error %d", errorCode);
    }

    if ((errorCode = AndroidBitmap_getInfo(env, bitmap, &info)) != 0) {
        LOGI("error %d", errorCode);
    }

    LOGI("bitmap info: %d wide, %d tall, %d ints per pixel", info.width, info.height, info.format);


    long size = info.width * info.height * info.format;
    //pixels = (int*)&qhdBuffer[0];
    //memcpy(pixels, addr, size * sizeof(int));


    //std::vector<char> data(size * 4);

    //memcpy(&data[0], addr, size * sizeof(int));

  //  GLES2Lesson::m_UIImages[uiName] =data;

    //gles2Lesson->m_renderedTexture

    glBindTexture(GL_TEXTURE_2D, GLES2Lesson::m_renderedTexture);

   // glReadPixels(0, 0, info.width , info.height, GL_RGBA, GL_UNSIGNED_BYTE, addr);




    glBindTexture(GL_TEXTURE_2D, 0);


    for( int i = 0; i < info.width * info.height; ++i)
    {
        addr[i] = 128;
    }



    if ((errorCode = AndroidBitmap_unlockPixels(env, bitmap)) != 0) {
        LOGI("error %d", errorCode);
    }



   // env->ReleaseStringUTFChars(uiName_, uiName);


}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_hasRenderTextureData(JNIEnv *env, jclass type) {

    // TODO


    if (gles2Lesson != nullptr) {
        return gles2Lesson->mFrameCount > 1;
    }


    return false;
}



/**restore java bitmap (from JNI data)*/  //




extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_fillTextureData(JNIEnv *env, jclass type, jint width, jint height,
                                                                  jobject data) {

    // TODO

    // TODO

    unsigned  char* addr = (unsigned char*) env->GetDirectBufferAddress (data);



    //r( int i = 0; i <width * height; ++i)
   //
    //  addr[i] = 128;
    //



    glBindFramebuffer(GL_FRAMEBUFFER, GLES2Lesson::m_FramebufferName);


     glReadPixels(0, 0, width , height, GL_RGBA, GL_UNSIGNED_BYTE, addr);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);


    //glBindTexture(GL_TEXTURE_2D, 0);




}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_stopFilterProcess(JNIEnv *env, jclass type) {

    // TODO



    return 0;

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_debugShowOriginMini(JNIEnv *env, jclass type, jint bEnable) {

    // TODO

    GLES2Lesson::mDebugShowOriginMini = bEnable;

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_debugShowHistogram(JNIEnv *env, jclass type, jint bEnable) {

    // TODO
    GLES2Lesson::mDebugShowHistogram = bEnable;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_bottomMargin(JNIEnv *env, jclass type, jfloat margin) {
    GLES2Lesson::mBottomMargin = margin;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_topMargin(JNIEnv *env, jclass type, jfloat margin) {
    GLES2Lesson::mTopMargin = margin;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_leftMargin(JNIEnv *env, jclass type, jfloat margin) {
    GLES2Lesson::mLeftMargin = margin;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_rightMargin(JNIEnv *env, jclass type, jfloat margin) {
    GLES2Lesson::mRightMargin = margin;
}




extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setScale(JNIEnv *env, jclass type, jfloat value) {

    FilterPreProcessor::inputValueScaleAll = value;
}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setFilterOn(JNIEnv *env, jclass type, jint bOn) {

    // TODO

    FilterPreProcessor::bEnableFilter = bOn;

}extern "C"


JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getFilterOn(JNIEnv *env, jclass type) {

    // TODO
    return  FilterPreProcessor::bEnableFilter;
}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_getFilterOdddn(JNIEnv *env, jclass type) {

    // TODO

}extern "C"
JNIEXPORT jint JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setClearColor(JNIEnv *env, jclass type, jfloat r, jfloat g, jfloat b) {

    GLES2Lesson::msClearColorR = r;
    GLES2Lesson::msClearColorG = g;
    GLES2Lesson::msClearColorB = b;

    return 0;
}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_startAnimation(JNIEnv *env, jclass type) {


    GLES2Lesson::mMAnimationTime = current_timestamp();
    GLES2Lesson::mAnimationUse = 1;

    return;

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_step1(JNIEnv *env, jclass type, jint isFront) {
    // TODO

    if (gles2Lesson != nullptr) {
        gles2Lesson->render1(isFront);
    }

}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_step2(JNIEnv *env, jclass type, jint isFront) {

    // TODO

    if (gles2Lesson != nullptr) {
        gles2Lesson->render2(isFront);
    }
}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_stepnorendertexture(JNIEnv *env, jclass type) {

    // TODO

    if (gles2Lesson != nullptr) {
        gles2Lesson->render_norendertexture();
    }


}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_stepEncodenorendertexture(JNIEnv *env, jclass type, jfloat dstWidth,
                                                                            jfloat dstHeight) {

    // TODO



    if (gles2Lesson != nullptr) {
        gles2Lesson->renderEncodenorendertexture( dstWidth, dstHeight);
    }



}extern "C"
JNIEXPORT void JNICALL
Java_com_diveroid_camera_underfilter_GL2JNILib_setVideoOrientation(JNIEnv *env, jclass clazz,
                                                                      jint video_orientation) {
    LOGI("VideoOrientation %d", video_orientation);
    GLES2Lesson::videoOrientation = video_orientation;
}
