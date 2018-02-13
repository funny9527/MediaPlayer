# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

# ffmpeg begin
include $(CLEAR_VARS)
LOCAL_MODULE := avcodec
LOCAL_SRC_FILES := prebuild/libavcodec-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avdevice
LOCAL_SRC_FILES := prebuild/libavdevice-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avfilter
LOCAL_SRC_FILES := prebuild/libavfilter-5.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avformat
LOCAL_SRC_FILES := prebuild/libavformat-56.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := avutil
LOCAL_SRC_FILES := prebuild/libavutil-54.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := postproc
LOCAL_SRC_FILES := prebuild/libpostproc-53.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swresample
LOCAL_SRC_FILES := prebuild/libswresample-1.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := swscale
LOCAL_SRC_FILES := prebuild/libswscale-3.so
include $(PREBUILT_SHARED_LIBRARY)
# ffmpeg end

include $(CLEAR_VARS)

SRC_PATH = ./

LOCAL_LDLIBS := -llog -lz
LOCAL_MODULE    := streamer
LOCAL_SRC_FILES := $(SRC_PATH)/onload.c \
                   $(SRC_PATH)/vod.c \
                   $(SRC_PATH)/live.c \
                   $(SRC_PATH)/ffmpeg_mod.c \
                   $(SRC_PATH)/ffmpeg_opt.c \
                   $(SRC_PATH)/ffmpeg_filter.c \
                   $(SRC_PATH)/cmdutils.c \
                   $(SRC_PATH)/record.c \
                   $(SRC_PATH)/yuvrecorder.c \
                   $(SRC_PATH)/ffmpeg_encoder.c \
                   $(SRC_PATH)/encoder.c \
                   $(SRC_PATH)/decode.c

LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_SHARED_LIBRARIES := avcodec avdevice avfilter avformat avutil postproc swresample swscale

include $(BUILD_SHARED_LIBRARY)
