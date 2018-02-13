package com.szy.testpreview.yuv;

/**
 * Created by szy on 17/1/24.
 */
public class NativeDecoder {
    /**
     *
     * @param arr : yuv420p
     * @param data : rgb24
     * @param width : camera width
     * @param height : camera height
     */
    public native void yuvTorgb(byte[] arr, int[] data, int width, int height);

    /**
     *
     * @param arr : yuv420p
     * @param data : rgb24
     * @param width : camera width
     * @param height : camera height
     * @param path : 保存bitmap路径
     */
    public native void yuvTobitmap(byte[] arr, int[] data, int width, int height, String path);

    static {
        System.loadLibrary("decoder");
    }
}
