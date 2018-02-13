package com.szy.testpreview.decoder;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

public class AudioDecoder {
	private final String TAG = "audio";
	/** 用来解码 */
	private MediaCodec mMediaCodec;
	/** 用来读取音频文件 */
	private MediaExtractor extractor;
	private MediaFormat format;
	private int sampleRate = 0, channels = 0, bitrate = 0;
	private long presentationTimeUs = 0, duration = 0;

	private AudioTrackPlayer audioPlayer;

	private boolean stopped = false;

	public void decode(String url) {

		extractor = new MediaExtractor();
		// 根据路径获取源文件
		try {
			extractor.setDataSource(url);
		} catch (Exception e) {
			Log.e(TAG, " 设置文件路径错误" + e.getMessage());
		}
		String mime = "";
		try {
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				format = extractor.getTrackFormat(i);
				mime = format.getString(MediaFormat.KEY_MIME);
				Log.v(TAG, "mime == " + mime);
				if (mime.startsWith("audio")) {
					extractor.selectTrack(i);
					// 音频文件信息
					format = extractor.getTrackFormat(i);
					mime = format.getString(MediaFormat.KEY_MIME);
					sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
					// 声道个数：单声道或双声道
					channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
					// if duration is 0, we are probably playing a live stream
					duration = format.getLong(MediaFormat.KEY_DURATION);
					// System.out.println("歌曲总时间秒:"+duration/1000000);
					bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);

					if (audioPlayer == null) {
						audioPlayer = new AudioTrackPlayer(sampleRate,
								AudioFormat.CHANNEL_OUT_STEREO,
								AudioFormat.ENCODING_PCM_16BIT);
						audioPlayer.init();
					}
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "音频文件信息读取出错：" + e);
			// 不要退出，下面进行判断
		}
		Log.d(TAG, "Track info: mime:" + mime + " 采样率sampleRate:" + sampleRate
				+ " channels:" + channels + " bitrate:" + bitrate
				+ " duration:" + duration);
		// 检查是否为音频文件
		if (format == null || !mime.startsWith("audio/")) {
			Log.e(TAG, "不是音频文件 end !");
			return;
		}
		// 实例化一个指定类型的解码器,提供数据输出
		// Instantiate an encoder supporting output data of the given mime type
		try {
			mMediaCodec = MediaCodec.createDecoderByType(mime);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mMediaCodec == null) {
			Log.e(TAG, "创建解码器失败！");
			return;
		}
		mMediaCodec.configure(format, null, null, 0);

		mMediaCodec.start();
		// 用来存放目标文件的数据
		ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
		// 解码后的数据
		ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
		// 设置声道类型:AudioFormat.CHANNEL_OUT_MONO单声道，AudioFormat.CHANNEL_OUT_STEREO双声道
		int channelConfiguration = channels == 1 ? AudioFormat.CHANNEL_OUT_MONO
				: AudioFormat.CHANNEL_OUT_STEREO;
		Log.i(TAG, "channelConfiguration=" + channelConfiguration);
		extractor.selectTrack(0);
		// ==========开始解码=============
		boolean sawInputEOS = false;
		boolean sawOutputEOS = false;
		final long kTimeOutUs = 10000;
		MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

		while (!sawOutputEOS && !stopped) {
			Log.v("test", "================ +++++++++++");
			try {
				if (!sawInputEOS) {
					Log.v("test", "================");
					int inputBufIndex = mMediaCodec
							.dequeueInputBuffer(kTimeOutUs);
					if (inputBufIndex >= 0) {
						ByteBuffer dstBuf = inputBuffers[inputBufIndex];

						int sampleSize = extractor.readSampleData(dstBuf, 0);
						if (sampleSize < 0) {
							Log.d(TAG, "saw input EOS. Stopping playback");
							sawInputEOS = true;
							sampleSize = 0;
						} else {
							presentationTimeUs = extractor.getSampleTime();
						}

						mMediaCodec
								.queueInputBuffer(
										inputBufIndex,
										0,
										sampleSize,
										presentationTimeUs,
										sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM
												: 0);

						if (!sawInputEOS) {
							extractor.advance();
						}

					} else {
						Log.e(TAG, "inputBufIndex " + inputBufIndex);
					}
				} // !sawInputEOS

				// decode to PCM and push it to the AudioTrack player
				int res = mMediaCodec.dequeueOutputBuffer(info, kTimeOutUs);

				if (res >= 0) {
					int outputBufIndex = res;
					ByteBuffer buf = outputBuffers[outputBufIndex];
					final byte[] chunk = new byte[info.size];

					Log.d(TAG, "output ========= " + info.size);

					buf.get(chunk);
					buf.clear();
					if (chunk.length > 0) {
						audioPlayer.playAudioTrack(chunk, 0, chunk.length);
					}
					mMediaCodec.releaseOutputBuffer(outputBufIndex, false);
					if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
						Log.d(TAG, "saw output EOS.");
						sawOutputEOS = true;
					}

				} else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
					outputBuffers = mMediaCodec.getOutputBuffers();
					Log.w(TAG, "[AudioDecoder]output buffers have changed.");
				} else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					MediaFormat oformat = mMediaCodec.getOutputFormat();
					Log.w(TAG, "[AudioDecoder]output format has changed to "
							+ oformat);
				} else {
					Log.w(TAG, "[AudioDecoder] dequeueOutputBuffer returned "
							+ res);
				}

			} catch (RuntimeException e) {
				Log.e(TAG, "[decodeMP3] error:" + e.getMessage());
			}
		}

		// =================================================================================
		if (mMediaCodec != null) {
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
		if (extractor != null) {
			extractor.release();
			extractor = null;
		}
		// clear source and the other globals
		duration = 0;
		mime = null;
		sampleRate = 0;
		channels = 0;
		bitrate = 0;
		presentationTimeUs = 0;
		duration = 0;
	}

	public void stop() {
		stopped = true;
	}
}
