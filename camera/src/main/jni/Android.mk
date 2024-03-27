LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_ROOT:=$(LOCAL_PATH)/../../../../opencv/native/jni
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=on
OPENCV_LIB_TYPE := STATIC

include $(OPENCV_ROOT)/OpenCV.mk

LOCAL_MODULE := ImageFilter
LOCAL_SRC_FILES := com_ao_diveroid_util_CvUtil.cpp
LOCAL_C_INCLUDES := $(OPENCV_ROOT)/include
LOCAL_LDLIBS += -lm -llog

include $(BUILD_SHARED_LIBRARY)