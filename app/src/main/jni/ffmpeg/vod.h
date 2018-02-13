#ifndef __VOD_H__
#define __VOD_H__


#include <stdio.h>
#include <time.h>

#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/log.h"
#include <jni.h>
#include <pthread.h>
#include "log.h"


void save_log(void *ptr, int level, const char* fmt, va_list vl);

int vod_to_rtmp(JNIEnv *env, jobject obj, jstring input_jstr, jstring output_jstr, jmethodID id);
int stop_vod();
int is_vod_stopped();

#endif
