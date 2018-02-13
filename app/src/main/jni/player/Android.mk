LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libmain

SDL_PATH := ../SDL
FFMPEG_PATH := ../ffmpeg

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(SDL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(FFMPEG_PATH)/include


# Add your application source files here...
LOCAL_SRC_FILES := ../sdl/src/main/android/SDL_android_main.c \
                    main.c

LOCAL_SHARED_LIBRARIES := SDL2
LOCAL_SHARED_LIBRARIES += avcodec avdevice avfilter avformat avutil postproc swresample swscale

LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog

include $(BUILD_SHARED_LIBRARY)
