package com.szy.testpreview.yuv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.szy.testpreview.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class YuvActivity extends Activity implements SurfaceHolder.Callback2,
        Camera.PreviewCallback, View.OnClickListener {
    static final String TAG = "data";
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;

    private Camera camera;
    private Camera.Parameters parameters;

    int width = 1280;
    int height = 720;
    private byte[] mFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        surfaceview = (SurfaceView)findViewById(R.id.surfaceview);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
        findViewById(R.id.btn6).setOnClickListener(this);
        findViewById(R.id.btn7).setOnClickListener(this);
        findViewById(R.id.btn8).setOnClickListener(this);
        findViewById(R.id.btn9).setOnClickListener(this);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = getBackCamera();
        startcamera(camera);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mFrame = data;
    }

    private String getString(byte[] d) {
        int len = d.length;
        Log.v(TAG, "len =========== " + len);
        StringBuilder sb = new StringBuilder();
        for (byte temp : d) {
            sb.append(temp).append(" ");
        }
        String s = sb.toString();
        Log.v(TAG, s);
        return s;
    }

    public void saveYuv(byte[] data) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++ " + path);
        File file = new File(path + File.separator + "yuv.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream f = null;
        try {
            f = new FileOutputStream(file);
            String s = getString(data);
            f.write(s.getBytes());
            Log.v(TAG, "write down");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v(TAG, e + "");
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, e + "");
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.v(TAG, e + "");
                }
            }
        }
    }

    private Camera getBackCamera() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private void startcamera(Camera mCamera) {
        if(mCamera != null) {
            try {
                mCamera.setPreviewCallback(this);
                mCamera.setDisplayOrientation(90);
                if(parameters == null){
                    parameters = mCamera.getParameters();
                }
                parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createBmp(final byte[] data) {
        Log.v(TAG, "create bmp");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int size = width * height;
        int[] pixels = new int[size];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = width * i + j;

                int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = data[index] & 0xFF;
                int vData = data[vindex] & 0xFF;
                int uData = data[uindex] & 0xFF;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_java_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void testNative(byte[] arr) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int size = width * height;
        int[] pixels = new int[size];
        NativeDecoder d = new NativeDecoder();
        d.yuvTorgb(arr, pixels, width, height);

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_native_java.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void testBmp(byte[] arr) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_native.bmp");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        int size = width * height;
        int[] pixels = new int[size];
        NativeDecoder d = new NativeDecoder();
        d.yuvTobitmap(arr, pixels, width, height, file.getAbsolutePath());
    }

    public void createYBmp(final byte[] data) {
        Log.v(TAG, "create bmp");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int size = width * height;
        int[] pixels = new int[size];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = width * i + j;

                int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = data[index] & 0xFF;
                int vData = 128;
                int uData = 128;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_y_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void createUBmp(final byte[] data) {
        Log.v(TAG, "create bmp");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int size = width * height;
        int[] pixels = new int[size];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = width * i + j;

                int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = 16;//data[index] & 0xFF;
                int vData = 128;//data[vindex] & 0xFF;
                int uData = data[uindex] & 0xFF;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_u_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void createVBmp(final byte[] data) {
        Log.v(TAG, "create bmp");

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int size = width * height;
        int[] pixels = new int[size];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = width * i + j;

                int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = 16;//data[index] & 0xFF;
                int vData = data[vindex] & 0xFF;
                int uData = 128;//data[uindex] & 0xFF;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_v_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void createRotateBmp(final byte[] arr) {
        Log.v(TAG, "create bmp");
        int size = width * height;
        byte[] data = new byte[size * 3 / 2];
        int[] pixels = new int[size];

        rotate(arr, data, width, height);

        Bitmap bmp = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = height * i + j;

                int vindex = size + height * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = data[index] & 0xFF;
                int vData = data[vindex] & 0xFF;
                int uData = data[uindex] & 0xFF;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, height, 0, 0, height, width);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_rotate_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    public void rotate(byte[] src, byte[] dest, int w, int h) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < w * h * 3 / 2; i++) {
//            if (i % w == 0) {
//                sb.append("\n");
//            }
//            sb.append(src[i] + "  ");
//        }
//        Log.d(TAG, sb.toString());



        int m = 0;
        for (int i = 0; i < w; i++)
            for (int j = h - 1; j >= 0; j--) {
                dest[m++] = src[j * w + i];
            }

        Log.d(TAG, "m=========== " + m);

        for (int i = 0; i < w; i += 2)
            for ( int j = h * 3 / 2 - 1; j >= h; j--) {
                int index = j * w + i;
                if (index < w * h * 3 / 2) {
                    dest[m++] = src[index];
                }

                if (index + 1 < w * h * 3 / 2) {
                    dest[m++] = src[index + 1];
                }
            }



//        sb.setLength(0);
//        for (int i = 0; i < w * h * 3 / 2; i++) {
//            if (i % h == 0) {
//                sb.append("\n");
//            }
//            sb.append(dest[i] + "  ");
//        }
//
//        Log.d(TAG, sb.toString());
    }

    public void createRotateMaskBmp(final byte[] arr) {
        Log.v(TAG, "create bmp");
        int size = width * height;
        byte[] data = new byte[size * 3 / 2];
        int[] pixels = new int[size];

        rotate(arr, data, width, height);

        Bitmap bmp = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = height * i + j;

                int vindex = size + height * (i / 2) + (j % 2 == 0 ? j : j - 1);
                int uindex = vindex + 1;
                int yData = data[index] & 0xFF;
                int vData = data[vindex] & 0xFF;
                int uData = data[uindex] & 0xFF;

                int r = (1164 * (yData - 16) + 1596 * (vData - 128)) / 1000;
                int g = (1164 * (yData - 16) - 813 * (vData - 128) - 392 * (uData - 128)) / 1000;
                int b = (1164 * (yData - 16) + 2017 * (uData - 128)) / 1000;

                //mask
                if (i >= width / 2 - 40 && i <= width / 2 + 160 && j >= height / 2 - 40 && j <=  height / 2 + 160) {
                    int k =  i / 40;
                    int d = j / 40;

                    int v = (k + d) % 2 == 0 ? 255 : 0;
                    r = v;
                    g = v;
                    b = v;
                }

                if (r < 0) {
                    r = 0;
                }
                if (r > 255) {
                    r = 255;
                }
                if (g < 0) {
                    g = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b > 255) {
                    b = 255;
                }

                pixels[index] = (255 << 24) | (r << 16) | (g << 8) | b;//Color.argb(255, r, g, b);
            }
        }

        bmp.setPixels(pixels, 0, height, 0, 0, height, width);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.v(TAG, "+++++++++++----- " + path);
        File file = new File(path + File.separator + "z_rotate_mask_bmp.jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "down!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "out put ++++ " +e);
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "out put ++++ " +e);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mFrame == null) {
            return ;
        }

        switch (v.getId()) {
            case R.id.btn1:
                createBmp(mFrame);
                break;
            case R.id.btn2:
                saveYuv(mFrame);
                break;
            case R.id.btn3:
                testNative(mFrame);
                break;
            case R.id.btn4:
                testBmp(mFrame);
                break;
            case R.id.btn5:
                createYBmp(mFrame);
                break;
            case R.id.btn6:
                createUBmp(mFrame);
                break;
            case R.id.btn7:
                createVBmp(mFrame);
                break;
            case R.id.btn8:
                createRotateBmp(mFrame);

//                byte[] src = new byte[] {
//                        11, 12, 13, 14, 15,16,17,18,
//                        21, 22, 23, 24,25,26,27,28,
//                        31,32,33,34,35,36,37,38,
//                        41,42,43,44,45,46,47,48,
//                        51,52,53,54,55,56,57,58,
//                        61,62,63,64,65,66,67,68
//                };
//
//                byte[] dest = new byte[48];
//                rotate(src, dest, 8, 4);
                break;
            case R.id.btn9:
                createRotateMaskBmp(mFrame);
                break;
        }
    }
}
