/**
 * used for translate jpeg files into mp4
 */


#ifndef __RECORD_H__
#define __RECORD_H__

#include <stdio.h>
#include <jni.h>
#include "log.h"
#include "libavformat/avformat.h"

AVStream *add_vidio_stream(AVFormatContext *oc, enum AVCodecID codec_id);

void start_record(JNIEnv* env, jobject thiz, jstring output, jobjectArray array, jint size);

void start(char* output, char* array[], int size);

#endif
