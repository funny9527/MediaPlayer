//
// Created by 朝阳 on 16/11/18.
//
#include <string.h>
#include <jni.h>
#include "log.h"
#include "utils.h"



#ifdef __cplusplus
extern "C" {
#endif


static const char* className = "com/szy/testpreview/yuv/NativeDecoder";

void native_yuvTorgb(JNIEnv *env, jobject thiz, jbyteArray array, jintArray data, jint width, jint height) {
    //int size = (*env)->GetArrayLength(env, array);
    jbyte* arr = (*env)->GetByteArrayElements(env, array, NULL);

    //int d_size =  (*env)->GetArrayLength(env, data);
    jint* d_arr = (*env)->GetIntArrayElements(env, data, NULL);

    //LOGD("native_yuvTorgb == %d %d", size, d_size);

   /* int i = 0;
    for (; i < size; i++) {
        LOGD("get value = %d", arr[i]);
    }*/

    yuvTorgb(arr, d_arr, width, height);

    (*env)->ReleaseByteArrayElements(env, array, arr, NULL);
    (*env)->ReleaseByteArrayElements(env, data, d_arr, NULL);
}

void native_yuvTobitmap(JNIEnv *env, jobject thiz, jbyteArray array, jintArray data, jint width, jint height, jstring path) {
    jbyte* arr = (*env)->GetByteArrayElements(env, array, NULL);
    jint* d_arr = (*env)->GetIntArrayElements(env, data, NULL);

    char* file = (*env)->GetStringUTFChars(env, path, NULL);
    yuvTobitmap(arr, d_arr, width, height, file);

    (*env)->ReleaseByteArrayElements(env, array, arr, NULL);
    (*env)->ReleaseByteArrayElements(env, data, d_arr, NULL);
    (*env)->ReleaseStringUTFChars(env, path, file);
}




static JNINativeMethod mMethods[] =
{
    {"yuvTorgb", "([B[III)V", (void *) native_yuvTorgb},
    {"yuvTobitmap", "([B[IIILjava/lang/String;)V", (void *) native_yuvTobitmap}
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

jint JNI_OnLoad(JavaVM* vm, void *reserved)
{
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

    return JNI_VERSION_1_4;
}



void JNI_OnUnload(JavaVM* vm, void *reserved)
{
    return;
}

#ifdef __cplusplus
}
#endif