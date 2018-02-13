package com.szy.testpreview.decoder;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceActivity extends Activity implements SurfaceHolder.Callback {
	private String mPath;
	private static final String TAG = "video";
	private VideoDecoder videoDecoder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPath = getIntent().getStringExtra("data");
		SurfaceView sv = new SurfaceView(this);
		sv.getHolder().addCallback(this);
		setContentView(sv);
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, int format, int width,
							   int height) {
		Thread playThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
						videoDecoder = new VideoDecoder(holder.getSurface());
						videoDecoder.decode(mPath);
					}
				}
		);
		playThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (videoDecoder != null) {
			videoDecoder.stop();
		}
	}
}