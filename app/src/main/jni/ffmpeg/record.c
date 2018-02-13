#include "record.h"
#include "yuvrecord.h"


#define DATASIZE 1024*1024

AVStream *add_vidio_stream(AVFormatContext *oc, enum AVCodecID codec_id)//用以初始化一个用于输出的AVFormatContext结构体
{
	AVStream *st;
	AVCodec *codec;

	st = avformat_new_stream(oc, NULL);
	if (!st)
	{
		printf("Could not alloc stream\n");
		exit(1);
	}
	codec = avcodec_find_encoder(codec_id);//查找mjpeg解码器
	if (!codec)
	{
		printf("codec not found\n");
		exit(1);
	}
	avcodec_get_context_defaults3(st->codec, codec);//申请AVStream->codec(AVCodecContext对象)空间并设置默认值(由avcodec_get_context_defaults3()设置

	st->codec->bit_rate = 400000;//设置采样参数，即比特率
	st->codec->width = 320;//设置视频宽高，这里跟图片的宽高保存一致即可
	st->codec->height = 240;
	st->codec->time_base.den = 10;//设置帧率
	st->codec->time_base.num = 1;

	st->codec->pix_fmt = PIX_FMT_YUV420P;//设置像素格式
	st->codec->codec_tag = 0;
	if (oc->oformat->flags & AVFMT_GLOBALHEADER)//一些格式需要视频流数据头分开
		st->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
	return st;
}

void start_record(JNIEnv* env, jobject thiz, jstring output, jobjectArray array, jint size)
{
    char* tmp = (*env)->GetStringUTFChars(env, output, 0);
    char* output_file = (char*) malloc(sizeof(char) * (strlen(tmp) + 1));
    strcpy(output_file, tmp);

    LOGD("get output path=%s", output_file);

    int i;
    char** arr = (char**) malloc(sizeof(char*) * size);
    for (i = 0; i < size; i++)
    {
    	jstring file = (*env)->GetObjectArrayElement(env, array, i);
    	char* name = (*env)->GetStringUTFChars(env, file, 0);

    	arr[i] = (char*) malloc(sizeof(char) * (strlen(name) + 1));
    	strcpy(arr[i], name);

    	(*env)->DeleteLocalRef(env, file);
//    	LOGD("get file path=%s", name);
    }

    start(output_file, arr, size);
}

void start(char* output, char* array[], int size)
{
	int i = 0;
//	for (i = 0; i < size; i++) {
//		LOGD("## %s", array[i]);
//	}

	AVFormatContext *ofmt_ctx = NULL;//其包含码流参数较多，是一个贯穿始终的数据结构，很多函数都要用到它作为参数
	const char *out_filename = output;//"out.mkv";//输出文件路径，在这里也可以将mkv改成别的ffmpeg支持的格式，如mp4，flv，avi之类的
	int ret;//返回标志

	av_register_all();//初始化解码器和复用器
	avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, out_filename);//初始化一个用于输出的AVFormatContext结构体，视频帧率和宽高在此函数里面设置
	if (!ofmt_ctx)
	{
		LOGD("Could not create output context\n");
		return;
	}

	AVStream *out_stream = add_vidio_stream(ofmt_ctx, AV_CODEC_ID_MJPEG);//创造输出视频流
	av_dump_format(ofmt_ctx, 0, out_filename, 1);//该函数会打印出视频流的信息，如果看着不开心可以不要

	LOGD("++++ %d %d", out_stream->r_frame_rate.num, out_stream->r_frame_rate.den);

	if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE))//打开输出视频文件
	{
		ret = avio_open(&ofmt_ctx->pb, out_filename, AVIO_FLAG_WRITE);
		if (ret < 0) {
			LOGD("Could not open output file '%s'", out_filename);
			return;
		}
	}

//	AVDictionary* opt = NULL;
//
//	av_dict_set_int(&opt, "video_track_timescale", 25, 0);

	if (avformat_write_header(ofmt_ctx, NULL) < 0)//写文件头（Write file header）
	{
		LOGD("Error occurred when opening output file\n");
		return;
	}

	int frame_index = 0;//放入视频的图像计数
	unsigned char *mydata = (unsigned char*) malloc(sizeof(unsigned char) * DATASIZE);
	AVPacket pkt;
	av_init_packet(&pkt);
	pkt.flags |= AV_PKT_FLAG_KEY;
	pkt.stream_index = out_stream->index;//获取视频信息，为压入帧图像做准备
	while (frame_index < size)//将图像压入视频中
	{
		FILE *file;//打开一张jpeg图像并读取其数据，在这里图像最大为1M,如果超过1M，则需要修改1024*1024这里
		file = fopen(array[frame_index], "rb");
		pkt.size = fread(mydata, 1, DATASIZE, file);
		pkt.data = mydata;
		fclose(file);
		if (av_interleaved_write_frame(ofmt_ctx, &pkt) < 0) //写入图像到视频
		{
			LOGD("Error muxing packet\n");
			break;
		}
		LOGD("Write %8d frames to output file\n", frame_index);//打印出当前压入的帧数

		free(array[frame_index]);

		frame_index++;
	}
	av_free_packet(&pkt);//释放掉帧数据包对象
	av_write_trailer(ofmt_ctx);//写文件尾（Write file trailer）
	free(mydata);//释放数据对象
	free(output);
	free(array);
	if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
		avio_close(ofmt_ctx->pb);//关闭视频文件
	avformat_free_context(ofmt_ctx);//释放输出视频相关数据结构
	LOGD("finished");
	//trans();
	return;
}
