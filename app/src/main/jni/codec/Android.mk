LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS :=-llog

LOCAL_MODULE    := decoder
LOCAL_SRC_FILES := image.c \
                             utils.c

include $(BUILD_SHARED_LIBRARY)
