#include "utils.h"


void yuvTorgb(unsigned char* input, int* output, int width, int height) {
    LOGD("yuvTorgb");
    int size = width * height;
    unsigned char r_arr[size];
    unsigned char g_arr[size];
    unsigned char b_arr[size];

    for (int i = 0; i < height; i++) {
         for (int j = 0; j < width; j++) {
             int index = width * i + j;

             int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
             int uindex = vindex + 1;
             int yData = input[index];
             int vData = input[vindex];
             int uData = input[uindex];

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

             output[index] = (255 << 24) | (r << 16) | (g << 8) | b;

             r_arr[index] = r;
             g_arr[index] = g;
             b_arr[index] = b;
         }
    }

}

    int rgb24_to_bmp(int width, int height, const char *bmppath,
            unsigned char* r_arr, unsigned char* g_arr, unsigned char* b_arr) {

            LOGD("rgb24_to_bmp");
        typedef struct
        {
            long imageSize;
            long blank;
            long startPosition;
        } BmpHead;

        typedef struct
        {
            long  Length;
            long  width;
            long  height;
            unsigned short  colorPlane;
            unsigned short  bitColor;
            long  zipFormat;
            long  realSize;
            long  xPels;
            long  yPels;
            long  colorUse;
            long  colorImportant;
        } InfoHead;

        int size = width * height * 3;
        int i=0;
        int j=0;
        BmpHead m_BMPHeader = {0};
        InfoHead  m_BMPInfoHeader = {0};
        char bfType[2] = {'B','M'};
        int header_size = sizeof(bfType) + sizeof(BmpHead) + sizeof(InfoHead);
        unsigned char *rgb24_buffer = NULL;
        FILE *fp_bmp = NULL;

        if ((fp_bmp = fopen(bmppath,"wb"))==NULL) {
            LOGD("Error: Cannot open output BMP file %s", bmppath);
            return -1;
        }

        rgb24_buffer = (unsigned char *) malloc (width * height * 3);

        m_BMPHeader.imageSize = 3 * width * height + header_size;
        m_BMPHeader.startPosition = header_size;

        m_BMPInfoHeader.Length = sizeof(InfoHead);
        m_BMPInfoHeader.width = width;
        //BMP storage pixel data in opposite direction of Y-axis (from bottom to top).
        m_BMPInfoHeader.height = -height;
        m_BMPInfoHeader.colorPlane = 1;
        m_BMPInfoHeader.bitColor = 24;
        m_BMPInfoHeader.realSize = 3 * width * height;

        fwrite(bfType, 1, sizeof(bfType), fp_bmp);
        fwrite(&m_BMPHeader, 1, sizeof(m_BMPHeader), fp_bmp);
        fwrite(&m_BMPInfoHeader, 1, sizeof(m_BMPInfoHeader), fp_bmp);

        int temp = 0;
        int index = 1;
        for (int i = 0; i < size; i++) {
            temp = i % 3;
            index = i / 3;
            if (temp == 0) {
                rgb24_buffer[i] = b_arr[index];
            } else if (temp == 1) {
                rgb24_buffer[i] = g_arr[index];
            } else if (temp == 2) {
                rgb24_buffer[i] = r_arr[index];
            }
        }

        fwrite(rgb24_buffer, 3 * width * height, 1, fp_bmp);
        fclose(fp_bmp);
        free(rgb24_buffer);
        LOGD("down save bmp !!!!!!!");
        return 0;
    }

void yuvTobitmap(unsigned char* input, int* output, int width, int height, const char* path)
{
    LOGD("yuvTobitmap");
    int size = width * height;
    unsigned char r_arr[size];
    unsigned char g_arr[size];
    unsigned char b_arr[size];

    for (int i = 0; i < height; i++) {
         for (int j = 0; j < width; j++) {
             int index = width * i + j;

             int vindex = size + width * (i / 2) + (j % 2 == 0 ? j : j - 1);
             int uindex = vindex + 1;
             int yData = input[index];
             int vData = input[vindex];
             int uData = input[uindex];

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

             output[index] = (255 << 24) | (r << 16) | (g << 8) | b;

             r_arr[index] = r;
             g_arr[index] = g;
             b_arr[index] = b;
         }
    }

    rgb24_to_bmp(width, height, path, r_arr, g_arr, b_arr);
}