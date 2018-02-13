package com.szy.testpreview.decoder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import com.szy.testpreview.R;

public class DecodeActivity extends Activity {

	private static final String PATH = Environment
			.getExternalStorageDirectory() + "/vid.mp4";
	private AudioDecoder audioDecoder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);

		findViewById(R.id.audio).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				new Thread(
	        			new Runnable() {
	        				public void run() {
								audioDecoder = new AudioDecoder();
								audioDecoder.decode(PATH);
	        				}
	        			}
	        			).start();
			}
		});
		
		findViewById(R.id.surface).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DecodeActivity.this, SurfaceActivity.class);
				intent.putExtra("data", PATH);
				startActivity(intent);
			}
		});

		findViewById(R.id.video).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(DecodeActivity.this, VideoActivity.class);
				intent.putExtra("data", PATH);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (audioDecoder != null) {
			audioDecoder.stop();
		}
	}
}
