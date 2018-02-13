package com.szy.testpreview.decoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by szy on 17/1/25.
 */
public class VideoDecoder {
    private static final String TAG = "video";
    private MediaExtractor extractor;
    private MediaCodec decoder;
    private Surface surface;
    private boolean stopped = false;

    public VideoDecoder(Surface surface) {
        this.surface = surface;
    }

    public void decode(String path) {
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.v(TAG, "mime == " + mime);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                try {
                    decoder = MediaCodec.createDecoderByType(mime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                decoder.configure(format, surface, null, 0);
                break;
            }
        }

        if (decoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        decoder.start();

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long startMs = System.currentTimeMillis();

        while (!stopped) {
            if (!isEOS) {
                int inIndex = decoder.dequeueInputBuffer(10000);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];
                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        // We shouldn't stop the playback at this point,
                        // just pass the EOS
                        // flag to decoder, we will get it again from the
                        // dequeueOutputBuffer
                        Log.d("DecodeActivity",
                                "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                        decoder.queueInputBuffer(inIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        decoder.queueInputBuffer(inIndex, 0, sampleSize,
                                extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }
            }

            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d("DecodeActivity",
                            "New format " + decoder.getOutputFormat());
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    Log.v("DecodeActivity",
                            "We can't use this buffer but render it due to the API limit, "
                                    + buffer);

                    // We use a very simple clock to keep the video FPS, or the
                    // video
                    // playback will be too fast
                    while (info.presentationTimeUs / 1000 > System
                            .currentTimeMillis() - startMs) {
//						try {
//							sleep(10);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//							break;
//						}
                    }
                    decoder.releaseOutputBuffer(outIndex, true);
                    break;
            }

            // All decoded frames have been rendered, we can stop playing
            // now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("DecodeActivity",
                        "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }

        decoder.stop();
        decoder.release();
        extractor.release();
    }

    public void stop() {
        stopped = true;
    }
}