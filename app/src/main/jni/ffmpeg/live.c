#include "live.h"

extern JavaVM* mGlbVm;

typedef struct
{
	int num;
	char** param;
} LiveNode;

static jmethodID method;
static pthread_mutex_t mutex;
static jobject jObject;

//Output FFmpeg's av_log()
void local_log(void *ptr, int level, const char* fmt, va_list vl) {

	//To TXT file

	FILE *fp=fopen("/storage/emulated/0/live_log.txt","a+");
	if(fp){
		vfprintf(fp,fmt,vl);
		fflush(fp);
		fclose(fp);
	}


	//To Logcat
	//LOGE(fmt, vl);
}

int run_live(void* args)
{
	JNIEnv* env = NULL;
	if (0 == (*mGlbVm)->AttachCurrentThread(mGlbVm, &env, NULL))
	{
		if (0 != pthread_mutex_lock(&mutex))
		{
		    LOGE("mutex error!");
		}

		int i = 0;
		LiveNode* node = (LiveNode*) args;
		int argc = node->num;
		char** argv = node->param;

		start_live_exec(argc,argv);

	    for(i=0;i<argc;i++)
	    {
			free(argv[i]);
		}

	    free(argv);
	    free(node);

	    (*env)->CallVoidMethod(env, jObject, method);

	    if (0 != pthread_mutex_unlock(&mutex))
	    {
	    	LOGE("unmutex error!");
	    }

	    (*mGlbVm)->DetachCurrentThread(mGlbVm);
	} else {
		LOGE("live thread error!");
	}

	return 0;
}


int start_live( JNIEnv * env, jobject thiz, jint cmdnum, jobjectArray cmdline, jmethodID id)
{

  //FFmpeg av_log() callback
  av_log_set_callback(local_log);
  int argc=cmdnum;

  char** argv=(char**)malloc(sizeof(char*)*argc);

  int i=0;
  for(i=0;i<argc;i++)
  {
    jstring string=(*env)->GetObjectArrayElement(env,cmdline,i);
    const char* tmp=(*env)->GetStringUTFChars(env,string,0);
    argv[i]=(char*)malloc(sizeof(char)*1024);
    strcpy(argv[i],tmp);
  }

  LiveNode* node = (LiveNode*) malloc(sizeof(LiveNode));
  node->num = argc;
  node->param = argv;

  method = id;

  if (jObject == 0)
  {
      jObject = (*env)->NewGlobalRef(env, thiz);
  }

  pthread_t thread;
  int result = pthread_create(
  						&thread,
  						NULL,
  						run_live,
  						(void*) node);

  return 0;

}
