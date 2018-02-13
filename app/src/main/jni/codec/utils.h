#ifndef __UTILS__H__
#define __UTILS__H__

#include <string.h>
#include <stdio.h>
#include "log.h"

int rgb24_to_bmp(int width, int height, const char *bmppath,
    unsigned char* r_arr, unsigned char* g_arr, unsigned char* b_arr);
void yuvTorgb(unsigned char* input, int* output, int width, int height);
void yuvTobitmap(unsigned char* input, int* output, int width, int height, const char* path);

#endif