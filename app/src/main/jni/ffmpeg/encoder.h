#ifndef __ENCODER_H__
#define __ENCODER_H__

#include <string.h>
#include <jni.h>
#include "ffmpeg.h"
#include <jni.h>
#include "log.h"



int encoder_exec(int argc, char **argv);

void encoder_local_log(void *ptr, int level, const char* fmt, va_list vl);

int start_encoder(JNIEnv * env, jobject thiz, jint cmdnum, jobjectArray cmdline, jmethodID id);

int stop_encoder();
int is_encoder_stopped();


#endif
