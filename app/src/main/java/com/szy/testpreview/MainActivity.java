package com.szy.testpreview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.szy.testpreview.decoder.DecodeActivity;
import com.szy.testpreview.encoder.EncodeActivity;
import com.szy.testpreview.yuv.YuvActivity;

import org.libsdl.app.SDLActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        findViewById(R.id.yuv).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,
                                YuvActivity.class));
                    }
                }
        );

        findViewById(R.id.play).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,
                                DecodeActivity.class));
                    }
                }
        );

        findViewById(R.id.encode).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,
                                EncodeActivity.class));
                    }
                }
        );

        findViewById(R.id.sdl).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MainActivity.this,
                                SDLActivity.class));
                    }
                }
        );
    }
}
