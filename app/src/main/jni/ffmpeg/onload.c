/**
 *
 */


#include <string.h>
#include <jni.h>
#include "log.h"
#include "vod.h"
#include "live.h"
#include "record.h"
#include "encoder.h"
#include "decode.h"



#ifdef __cplusplus
extern "C" {
#endif

static const char* className = "com/bandq/stream/StreamDecoder";
static jmethodID jmethod_on_vod_stopped;
static jmethodID jmethod_on_live_stopped;
static jmethodID jmethod_on_record_stopped;
JavaVM* mGlbVm;


/**
 * play vod
 */
void _native_stream_vod_to_rtmp(JNIEnv* env, jobject thiz, jstring input, jstring output)
{
	vod_to_rtmp(env, thiz, input, output, jmethod_on_vod_stopped);
}

/**
 * stop vod
 */
int _native_stop_vod(JNIEnv* env, jobject thiz)
{
	return stop_vod();
}

/**
 * if vod stopped
 */
jint _native_vod_stopped(JNIEnv* env, jobject thiz)
{
	return is_vod_stopped();
}


/**
 * play live
 */
void _native_stream_mjpeg_to_rtmp(JNIEnv* env, jobject thiz, jobjectArray opt, jint size)
{
	start_live(env, thiz, size, opt, jmethod_on_live_stopped);
}

/**
 * stop live
 */
int _native_stop_live(JNIEnv* env, jobject thiz)
{
	return stop_live();
}

/**
 * if live stopped
 */
jint _native_live_stopped(JNIEnv* env, jobject thiz)
{
	return is_live_stopped();
}

/**
 * pack jpeg files to mp4
 */
void _native_start_record(JNIEnv* env, jobject thiz, jstring output, jobjectArray paths, jint size)
{
	start_record(env, thiz, output, paths, size);
}

/**
 * start record
 */
void _native_pack_h264(JNIEnv* env, jobject thiz, jobjectArray opt, jint size)
{
	start_encoder(env, thiz, size, opt, jmethod_on_record_stopped);
}

/**
 * stop record
 */
int _native_stop_Record(JNIEnv* env, jobject thiz)
{
	return stop_encoder();
}

/**
 * if record stopped
 */
jint _native_record_stopped(JNIEnv* env, jobject thiz)
{
	return is_encoder_stopped();
}

void _native_to_yuv(JNIEnv* env, jobject thiz) {
    open_file();
}


static JNINativeMethod mMethods[] =
{
	//for vod
    {"native_stream_vod_to_rtmp", "(Ljava/lang/String;Ljava/lang/String;)I", (void *) _native_stream_vod_to_rtmp},
    {"native_stop_vod", "()I", (int *) _native_stop_vod},
    {"native_vod_stopped", "()I", (int *) _native_vod_stopped},

    //for live
    {"native_stream_mjpeg_to_rtmp", "([Ljava/lang/String;I)I", (void *) _native_stream_mjpeg_to_rtmp},
    {"native_stop_live", "()I", (int *) _native_stop_live},
    {"native_live_stopped", "()I", (int *) _native_live_stopped},

    //for record
    {"native_pack_h264", "([Ljava/lang/String;I)I", (void *) _native_pack_h264},
    {"native_stop_Record", "()I", (int *) _native_stop_Record},
    {"native_record_stopped", "()I", (int *) _native_record_stopped},

    {"native_start_record", "(Ljava/lang/String;[Ljava/lang/String;I)I", (void *) _native_start_record},
    {"native_to_yuv", "()I", (void *) _native_to_yuv}
};


int register_methods(JNIEnv *env)
{
    jclass clazz;

    clazz = (*env)->FindClass(env, className );

    if (clazz == NULL)
    {
        LOGE("Can't find class %s\n", className);
        return -1;
    }

    LOGD("register native methods");

    if ((*env)->RegisterNatives(env, clazz, mMethods, sizeof(mMethods) / sizeof(mMethods[0])) != JNI_OK)
    {
        LOGE("Failed registering methods for %s\n", className);
        return -1;
    }

    return 0;
}

void registerMethod(JNIEnv *env)
{
	jclass clazz = (*env)->FindClass(env, className);
	jmethod_on_vod_stopped = (*env)->GetMethodID(env, clazz, "onVodStopped", "()V");
	jmethod_on_live_stopped = (*env)->GetMethodID(env, clazz, "onLiveStopped", "()V");
	jmethod_on_record_stopped = (*env)->GetMethodID(env, clazz, "onRecordStopped", "()V");
}


jint JNI_OnLoad(JavaVM* vm, void *reserved)
{
	mGlbVm = vm;
    JNIEnv* env = NULL;
    jint result = -1;

    LOGD("%s: +", __FUNCTION__);

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        LOGE("ERROR: GetEnv failed.\n");
        return result;
    }

    if (register_methods(env) < 0)
    {
        LOGE("ERROR: register methods failed.\n");
        return result;
    }

    registerMethod(env);

    return JNI_VERSION_1_4;
}



void JNI_OnUnload(JavaVM* vm, void *reserved)
{
    return;
}

#ifdef __cplusplus
}
#endif
