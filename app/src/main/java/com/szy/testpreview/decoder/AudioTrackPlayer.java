package com.szy.testpreview.decoder;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioTrackPlayer {
	int mFrequency;// 采样率
	int mChannel;// 声道
	int mSampBit;// 采样精度
	AudioTrack mAudioTrack;

	public AudioTrackPlayer(int frequency, int channel, int sampbit) {
		mFrequency = frequency;
		mChannel = channel;
		mSampBit = sampbit;
	}

	public void init() {
		if (mAudioTrack != null) {
			release();
		}

		// 获得构建对象的最小缓冲区大小
		int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel,
				mSampBit);
		Log.v("test", "size = " + minBufSize + "  " + mFrequency + "  "
				+ mChannel + "  " + mSampBit);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency,
				mChannel, mSampBit, minBufSize, AudioTrack.MODE_STREAM);
		// AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
		// STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。
		// 这个和我们在socket中发送数据一样，应用层从某个地方获取数据，
		// 例如通过编解码得到PCM数据，然后write到audiotrack。
		// 这种方式的坏处就是总是在JAVA层和Native层交互，效率损失较大。
		// 而STATIC的意思是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
		// 后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
		// 这种方法对于铃声等内存占用较小，延时要求较高的声音来说很适用。
		mAudioTrack.play();
	}

	public void release() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
		}
	}

	public void playAudioTrack(byte[] data, int offset, int length) {
		if (data == null || data.length == 0) {
			return;
		}
		try {
			mAudioTrack.write(data, offset, length);
		} catch (Exception e) {
			Log.i("MyAudioTrack", "catch exception...");
		}
	}

	public int getPrimePlaySize() {
		int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel,
				mSampBit);
		return minBufSize * 2;
	}
}
