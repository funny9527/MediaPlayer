#include "vod.h"


extern JavaVM* mGlbVm;

int stopped = 1;
static jmethodID method;
static pthread_mutex_t mutex;
static jobject jObject;


typedef struct
{
	jstring input;
	jstring output;
} VideoUrl;

void save_log(void *ptr, int level, const char* fmt, va_list vl) {

	//To TXT file
	FILE *fp=fopen("/storage/emulated/0/vod_log.txt","a+");
	if(fp){
		vfprintf(fp,fmt,vl);
		fflush(fp);
		fclose(fp);
	}

	//To Logcat
	//LOGE(fmt, vl);
}


//int vod_to_rtmp(JNIEnv *env, jobject obj, jstring input_jstr, jstring output_jstr)
int run_vod(void* args)
{
	VideoUrl* url = (VideoUrl*) args;
	jstring input_jstr = url->input;
	jstring output_jstr = url->output;

	JNIEnv* env = NULL;
	if (0 == (*mGlbVm)->AttachCurrentThread(mGlbVm, &env, NULL))
	{
		 if (0 != pthread_mutex_lock(&mutex))
	        {
			    	LOGE("mutex error!");
			}

		    AVOutputFormat *ofmt = NULL;
			AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
			AVPacket pkt;

			int ret, i;
			char input_str[500]={0};
			char output_str[500]={0};
			char info[1000]={0};
			sprintf(input_str,"%s",(*env)->GetStringUTFChars(env,input_jstr, NULL));
			sprintf(output_str,"%s",(*env)->GetStringUTFChars(env,output_jstr, NULL));

			LOGD("input:%s", input_str);
			LOGD("output:%s", output_str);

			//input_str  = "cuc_ieschool.flv";
			//output_str = "rtmp://localhost/publishlive/livestream";
			//output_str = "rtp://233.233.233.233:6666";

			//FFmpeg av_log() callback
			av_log_set_callback(save_log);

			av_register_all();
			//Network
			avformat_network_init();

			//Input
			if ((ret = avformat_open_input(&ifmt_ctx, input_str, 0, 0)) < 0) {
				LOGE( "Could not open input file.");
				goto end;
			}
			if ((ret = avformat_find_stream_info(ifmt_ctx, 0)) < 0) {
				LOGE( "Failed to retrieve input stream information");
				goto end;
			}

			int videoindex=-1;
			for(i=0; i<ifmt_ctx->nb_streams; i++)
				if(ifmt_ctx->streams[i]->codec->codec_type==AVMEDIA_TYPE_VIDEO){
					videoindex=i;
					break;
				}
			//Output
			avformat_alloc_output_context2(&ofmt_ctx, NULL, "flv",output_str); //RTMP
			//avformat_alloc_output_context2(&ofmt_ctx, NULL, "mpegts", output_str);//UDP

			if (!ofmt_ctx) {
				LOGE( "Could not create output context\n");
				ret = AVERROR_UNKNOWN;
				goto end;
			}
			ofmt = ofmt_ctx->oformat;
			for (i = 0; i < ifmt_ctx->nb_streams; i++) {
				//Create output AVStream according to input AVStream
				AVStream *in_stream = ifmt_ctx->streams[i];
				AVStream *out_stream = avformat_new_stream(ofmt_ctx, in_stream->codec->codec);
				if (!out_stream) {
					LOGE( "Failed allocating output stream\n");
					ret = AVERROR_UNKNOWN;
					goto end;
				}
				//Copy the settings of AVCodecContext
				ret = avcodec_copy_context(out_stream->codec, in_stream->codec);
				if (ret < 0) {
					LOGE( "Failed to copy context from input to output stream codec context\n");
					goto end;
				}
				out_stream->codec->codec_tag = 0;
				if (ofmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
					out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
			}

			//Open output URL
			if (!(ofmt->flags & AVFMT_NOFILE)) {
				ret = avio_open(&ofmt_ctx->pb, output_str, AVIO_FLAG_WRITE);
				if (ret < 0) {
					LOGE( "Could not open output URL '%s'", output_str);
					goto end;
				}
			}
			//Write file header
			ret = avformat_write_header(ofmt_ctx, NULL);
			if (ret < 0) {
				LOGE( "Error occurred when opening output URL\n");
				goto end;
			}

			int frame_index=0;

			int64_t start_time=av_gettime();
			while (!stopped) {
				AVStream *in_stream, *out_stream;
				//Get an AVPacket
				ret = av_read_frame(ifmt_ctx, &pkt);
				if (ret < 0)
					break;
				//FIX：No PTS (Example: Raw H.264)
				//Simple Write PTS
				if(pkt.pts==AV_NOPTS_VALUE){
					//Write PTS
					AVRational time_base1=ifmt_ctx->streams[videoindex]->time_base;
					//Duration between 2 frames (us)
					int64_t calc_duration=(double)AV_TIME_BASE/av_q2d(ifmt_ctx->streams[videoindex]->r_frame_rate);
					//Parameters
					pkt.pts=(double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE);
					pkt.dts=pkt.pts;
					pkt.duration=(double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE);
				}
				//Important:Delay
				if(pkt.stream_index==videoindex){
					AVRational time_base=ifmt_ctx->streams[videoindex]->time_base;
					AVRational time_base_q={1,AV_TIME_BASE};
					int64_t pts_time = av_rescale_q(pkt.dts, time_base, time_base_q);
					int64_t now_time = av_gettime() - start_time;
					if (pts_time > now_time)
						av_usleep(pts_time - now_time);

				}

				in_stream  = ifmt_ctx->streams[pkt.stream_index];
				out_stream = ofmt_ctx->streams[pkt.stream_index];
				/* copy packet */
				//Convert PTS/DTS
				pkt.pts = av_rescale_q_rnd(pkt.pts, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
				pkt.dts = av_rescale_q_rnd(pkt.dts, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX);
				pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
				pkt.pos = -1;
				//Print to Screen
				if(pkt.stream_index==videoindex){
					LOGE("Send %8d video frames to output URL\n",frame_index);
					frame_index++;
				}
				//ret = av_write_frame(ofmt_ctx, &pkt);
				ret = av_interleaved_write_frame(ofmt_ctx, &pkt);

				if (ret < 0) {
					LOGE( "Error muxing packet\n");
					break;
				}
				av_free_packet(&pkt);

			}
			//Write file trailer
			av_write_trailer(ofmt_ctx);
		end:
			avformat_close_input(&ifmt_ctx);
			/* close output */
			if (ofmt_ctx && !(ofmt->flags & AVFMT_NOFILE))
				avio_close(ofmt_ctx->pb);
			avformat_free_context(ofmt_ctx);
			if (ret < 0 && ret != AVERROR_EOF) {
				LOGE( "Error occurred.\n");
				return -1;
			}

			(*env)->DeleteGlobalRef(env, input_jstr);
			(*env)->DeleteGlobalRef(env, output_jstr);

			free(url);

			(*env)->CallVoidMethod(env, jObject, method);

			if (0 != pthread_mutex_unlock(&mutex))
			{
				 LOGE("unmutex error!");
		    }

			(*mGlbVm)->DetachCurrentThread(mGlbVm);
	} else {
		LOGE("vod thread error!");
	}

	return 0;
}


int vod_to_rtmp(JNIEnv *env, jobject obj, jstring input_jstr, jstring output_jstr, jmethodID id)
{
	VideoUrl* url = (VideoUrl*) malloc(sizeof(VideoUrl));
	url->input = (*env)->NewGlobalRef(env, input_jstr);
	url->output = (*env)->NewGlobalRef(env, output_jstr);

	method = id;
	if (jObject == 0)
		{
		    jObject = (*env)->NewGlobalRef(env, obj);
		}

	stopped = 0;

	pthread_t thread;
	int result = pthread_create(
						&thread,
						NULL,
						run_vod,
						(void*) url);
}


int stop_vod()
{
	stopped = 1;
	LOGD("stop vod!");
}

int is_vod_stopped()
{
	return stopped;
}

int vod_release() {

	if (0 != pthread_mutex_destroy(&mutex))
	{
		LOGE("destroy mutex error!");
	}
}

