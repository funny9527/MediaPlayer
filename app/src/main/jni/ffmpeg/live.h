#ifndef __CORE_H__
#define __CORE_H__

#include <string.h>
#include <jni.h>
#include "ffmpeg.h"
#include <jni.h>
#include "log.h"



int start_live_exec(int argc, char **argv);

void local_log(void *ptr, int level, const char* fmt, va_list vl);

int start_live(JNIEnv * env, jobject thiz, jint cmdnum, jobjectArray cmdline, jmethodID id);

int stop_live();
int is_live_stopped();


#endif
