/*
 *  Copyright 2012 The LibYuv Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS. All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

#ifndef INCLUDE_LIBYUV_CONVERT_ARGB_H_
#define INCLUDE_LIBYUV_CONVERT_ARGB_H_

#include "basic_types.h"

#include "rotate.h"  // For enum RotationMode.

// TODO(fbarchard): This set of functions should exactly match convert.h
// TODO(fbarchard): Add tests. Create random content of right size and convert
// with C vs Opt and or to I420 and compare.
// TODO(fbarchard): Some of these functions lack parameter setting.

#ifdef __cplusplus
namespace libyuv {
extern "C" {
#endif

// Alias.
#define ARGBToARGB ARGBCopy

// Copy ARGB to ARGB.
LIBYUV_API
int ARGBCopy(const uint8_t* src_argb,
             int src_stride_argb,
             uint8_t* dst_argb,
             int dst_stride_argb,
             int width,
             int height);

// Convert I420 to ARGB.
LIBYUV_API
int I420ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Duplicate prototype for function in convert_from.h for remoting.
LIBYUV_API
int I420ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert I010 to ARGB.
LIBYUV_API
int I010ToARGB(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert I010 to ARGB.
LIBYUV_API
int I010ToARGB(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert I010 to ABGR.
LIBYUV_API
int I010ToABGR(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert H010 to ARGB.
LIBYUV_API
int H010ToARGB(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert H010 to ABGR.
LIBYUV_API
int H010ToABGR(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert I422 to ARGB.
LIBYUV_API
int I422ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert I444 to ARGB.
LIBYUV_API
int I444ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert J444 to ARGB.
LIBYUV_API
int J444ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert I444 to ABGR.
LIBYUV_API
int I444ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert I420 with Alpha to preattenuated ARGB.
LIBYUV_API
int I420AlphaToARGB(const uint8_t* src_y,
                    int src_stride_y,
                    const uint8_t* src_u,
                    int src_stride_u,
                    const uint8_t* src_v,
                    int src_stride_v,
                    const uint8_t* src_a,
                    int src_stride_a,
                    uint8_t* dst_argb,
                    int dst_stride_argb,
                    int width,
                    int height,
                    int attenuate);

// Convert I420 with Alpha to preattenuated ABGR.
LIBYUV_API
int I420AlphaToABGR(const uint8_t* src_y,
                    int src_stride_y,
                    const uint8_t* src_u,
                    int src_stride_u,
                    const uint8_t* src_v,
                    int src_stride_v,
                    const uint8_t* src_a,
                    int src_stride_a,
                    uint8_t* dst_abgr,
                    int dst_stride_abgr,
                    int width,
                    int height,
                    int attenuate);

// Convert I400 (grey) to ARGB.  Reverse of ARGBToI400.
LIBYUV_API
int I400ToARGB(const uint8_t* src_y,
               int src_stride_y,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert J400 (jpeg grey) to ARGB.
LIBYUV_API
int J400ToARGB(const uint8_t* src_y,
               int src_stride_y,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Alias.
#define YToARGB I400ToARGB

// Convert NV12 to ARGB.
LIBYUV_API
int NV12ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_uv,
               int src_stride_uv,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert NV21 to ARGB.
LIBYUV_API
int NV21ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_vu,
               int src_stride_vu,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert NV12 to ABGR.
LIBYUV_API
int NV12ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_uv,
               int src_stride_uv,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert NV21 to ABGR.
LIBYUV_API
int NV21ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_vu,
               int src_stride_vu,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert NV12 to RGB24.
LIBYUV_API
int NV12ToRGB24(const uint8_t* src_y,
                int src_stride_y,
                const uint8_t* src_uv,
                int src_stride_uv,
                uint8_t* dst_rgb24,
                int dst_stride_rgb24,
                int width,
                int height);

// Convert NV21 to RGB24.
LIBYUV_API
int NV21ToRGB24(const uint8_t* src_y,
                int src_stride_y,
                const uint8_t* src_vu,
                int src_stride_vu,
                uint8_t* dst_rgb24,
                int dst_stride_rgb24,
                int width,
                int height);

// Convert NV21 to YUV24.
LIBYUV_API
int NV21ToYUV24(const uint8_t* src_y,
                int src_stride_y,
                const uint8_t* src_vu,
                int src_stride_vu,
                uint8_t* dst_yuv24,
                int dst_stride_yuv24,
                int width,
                int height);

// Convert NV12 to RAW.
LIBYUV_API
int NV12ToRAW(const uint8_t* src_y,
              int src_stride_y,
              const uint8_t* src_uv,
              int src_stride_uv,
              uint8_t* dst_raw,
              int dst_stride_raw,
              int width,
              int height);

// Convert NV21 to RAW.
LIBYUV_API
int NV21ToRAW(const uint8_t* src_y,
              int src_stride_y,
              const uint8_t* src_vu,
              int src_stride_vu,
              uint8_t* dst_raw,
              int dst_stride_raw,
              int width,
              int height);

// Convert M420 to ARGB.
LIBYUV_API
int M420ToARGB(const uint8_t* src_m420,
               int src_stride_m420,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert YUY2 to ARGB.
LIBYUV_API
int YUY2ToARGB(const uint8_t* src_yuy2,
               int src_stride_yuy2,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert UYVY to ARGB.
LIBYUV_API
int UYVYToARGB(const uint8_t* src_uyvy,
               int src_stride_uyvy,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert J420 to ARGB.
LIBYUV_API
int J420ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert J422 to ARGB.
LIBYUV_API
int J422ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert J420 to ABGR.
LIBYUV_API
int J420ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert J422 to ABGR.
LIBYUV_API
int J422ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert H420 to ARGB.
LIBYUV_API
int H420ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert H422 to ARGB.
LIBYUV_API
int H422ToARGB(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert H420 to ABGR.
LIBYUV_API
int H420ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert H422 to ABGR.
LIBYUV_API
int H422ToABGR(const uint8_t* src_y,
               int src_stride_y,
               const uint8_t* src_u,
               int src_stride_u,
               const uint8_t* src_v,
               int src_stride_v,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert H010 to ARGB.
LIBYUV_API
int H010ToARGB(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert I010 to AR30.
LIBYUV_API
int I010ToAR30(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_ar30,
               int dst_stride_ar30,
               int width,
               int height);

// Convert H010 to AR30.
LIBYUV_API
int H010ToAR30(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_ar30,
               int dst_stride_ar30,
               int width,
               int height);

// Convert I010 to AB30.
LIBYUV_API
int I010ToAB30(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_ab30,
               int dst_stride_ab30,
               int width,
               int height);

// Convert H010 to AB30.
LIBYUV_API
int H010ToAB30(const uint16_t* src_y,
               int src_stride_y,
               const uint16_t* src_u,
               int src_stride_u,
               const uint16_t* src_v,
               int src_stride_v,
               uint8_t* dst_ab30,
               int dst_stride_ab30,
               int width,
               int height);

// BGRA little endian (argb in memory) to ARGB.
LIBYUV_API
int BGRAToARGB(const uint8_t* src_bgra,
               int src_stride_bgra,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// ABGR little endian (rgba in memory) to ARGB.
LIBYUV_API
int ABGRToARGB(const uint8_t* src_abgr,
               int src_stride_abgr,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// RGBA little endian (abgr in memory) to ARGB.
LIBYUV_API
int RGBAToARGB(const uint8_t* src_rgba,
               int src_stride_rgba,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Deprecated function name.
#define BG24ToARGB RGB24ToARGB

// RGB little endian (bgr in memory) to ARGB.
LIBYUV_API
int RGB24ToARGB(const uint8_t* src_rgb24,
                int src_stride_rgb24,
                uint8_t* dst_argb,
                int dst_stride_argb,
                int width,
                int height);

// RGB big endian (rgb in memory) to ARGB.
LIBYUV_API
int RAWToARGB(const uint8_t* src_raw,
              int src_stride_raw,
              uint8_t* dst_argb,
              int dst_stride_argb,
              int width,
              int height);

// RGB16 (RGBP fourcc) little endian to ARGB.
LIBYUV_API
int RGB565ToARGB(const uint8_t* src_rgb565,
                 int src_stride_rgb565,
                 uint8_t* dst_argb,
                 int dst_stride_argb,
                 int width,
                 int height);

// RGB15 (RGBO fourcc) little endian to ARGB.
LIBYUV_API
int ARGB1555ToARGB(const uint8_t* src_argb1555,
                   int src_stride_argb1555,
                   uint8_t* dst_argb,
                   int dst_stride_argb,
                   int width,
                   int height);

// RGB12 (R444 fourcc) little endian to ARGB.
LIBYUV_API
int ARGB4444ToARGB(const uint8_t* src_argb4444,
                   int src_stride_argb4444,
                   uint8_t* dst_argb,
                   int dst_stride_argb,
                   int width,
                   int height);

// Aliases
#define AB30ToARGB AR30ToABGR
#define AB30ToABGR AR30ToARGB
#define AB30ToAR30 AR30ToAB30

// Convert AR30 To ARGB.
LIBYUV_API
int AR30ToARGB(const uint8_t* src_ar30,
               int src_stride_ar30,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int width,
               int height);

// Convert AR30 To ABGR.
LIBYUV_API
int AR30ToABGR(const uint8_t* src_ar30,
               int src_stride_ar30,
               uint8_t* dst_abgr,
               int dst_stride_abgr,
               int width,
               int height);

// Convert AR30 To AB30.
LIBYUV_API
int AR30ToAB30(const uint8_t* src_ar30,
               int src_stride_ar30,
               uint8_t* dst_ab30,
               int dst_stride_ab30,
               int width,
               int height);

#ifdef HAVE_JPEG
// src_width/height provided by capture
// dst_width/height for clipping determine final size.
LIBYUV_API
int MJPGToARGB(const uint8_t* sample,
               size_t sample_size,
               uint8_t* dst_argb,
               int dst_stride_argb,
               int src_width,
               int src_height,
               int dst_width,
               int dst_height);
#endif

// Convert Android420 to ARGB.
LIBYUV_API
int Android420ToARGB(const uint8_t* src_y,
                     int src_stride_y,
                     const uint8_t* src_u,
                     int src_stride_u,
                     const uint8_t* src_v,
                     int src_stride_v,
                     int src_pixel_stride_uv,
                     uint8_t* dst_argb,
                     int dst_stride_argb,
                     int width,
                     int height);

// Convert Android420 to ABGR.
LIBYUV_API
int Android420ToABGR(const uint8_t* src_y,
                     int src_stride_y,
                     const uint8_t* src_u,
                     int src_stride_u,
                     const uint8_t* src_v,
                     int src_stride_v,
                     int src_pixel_stride_uv,
                     uint8_t* dst_abgr,
                     int dst_stride_abgr,
                     int width,
                     int height);

// Convert camera sample to ARGB with cropping, rotation and vertical flip.
// "sample_size" is needed to parse MJPG.
// "dst_stride_argb" number of bytes in a row of the dst_argb plane.
//   Normally this would be the same as dst_width, with recommended alignment
//   to 16 bytes for better efficiency.
//   If rotation of 90 or 270 is used, stride is affected. The caller should
//   allocate the I420 buffer according to rotation.
// "dst_stride_u" number of bytes in a row of the dst_u plane.
//   Normally this would be the same as (dst_width + 1) / 2, with
//   recommended alignment to 16 bytes for better efficiency.
//   If rotation of 90 or 270 is used, stride is affected.
// "crop_x" and "crop_y" are starting position for cropping.
//   To center, crop_x = (src_width - dst_width) / 2
//              crop_y = (src_height - dst_height) / 2
// "src_width" / "src_height" is size of src_frame in pixels.
//   "src_height" can be negative indicating a vertically flipped image source.
// "crop_width" / "crop_height" is the size to crop the src to.
//    Must be less than or equal to src_width/src_height
//    Cropping parameters are pre-rotation.
// "rotation" can be 0, 90, 180 or 270.
// "fourcc" is a fourcc. ie 'I420', 'YUY2'
// Returns 0 for successful; -1 for invalid parameter. Non-zero for failure.
LIBYUV_API
int ConvertToARGB(const uint8_t* sample,
                  size_t sample_size,
                  uint8_t* dst_argb,
                  int dst_stride_argb,
                  int crop_x,
                  int crop_y,
                  int src_width,
                  int src_height,
                  int crop_width,
                  int crop_height,
                  enum RotationMode rotation,
                  uint32_t fourcc);

#ifdef __cplusplus
}  // extern "C"
}  // namespace libyuv
#endif

#endif  // INCLUDE_LIBYUV_CONVERT_ARGB_H_
