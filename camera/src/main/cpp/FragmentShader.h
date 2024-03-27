//
// Created by kyt77 on 2019-06-19.
//

#include <GLES3/gl3.h>
#include <string>

inline const std::string fragmentString = R"(
precision mediump float;
varying vec2 vTextureCoords;
varying vec4 vColour;
uniform sampler2D sTexture;


uniform float startR;
uniform float stopR;
uniform float startG;
uniform float stopG;
uniform float startB;
uniform float stopB;
uniform int channelMix;
uniform float scalarBF;
uniform float scalarGF;
uniform float rofb;
uniform float rofg;
uniform float mixRfromR;
uniform float mixRfromG;
uniform float mixRfromB;
uniform float mixBfromR;
uniform float mixBfromG;
uniform float mixBfromB;

void main() {
    vec4 tempColor =  texture2D( sTexture, vTextureCoords );
    tempColor.y = tempColor.y ;//* 255.0;
    tempColor.z = tempColor.z ;//* 255.0;

    float diffX = stopR - startR;
    float scaleX = 1.0/ diffX;
    float shiftX = 0.0 - startR*scaleX;
    tempColor.x = tempColor.x * scaleX;
    tempColor.x = tempColor.x + shiftX;
    tempColor.x =  min(tempColor.x, 1.0);
    tempColor.x = max(tempColor.x, 0.0);

    float diffY = stopG - startG;
    float scaleY = 1.0/ diffY;
    float shiftY = 0.0 - startG*scaleY;
    tempColor.y = tempColor.y * scaleY;
    tempColor.y = tempColor.y + shiftY;
    tempColor.y =  min(tempColor.y, 1.0);
    tempColor.y = max(tempColor.y, 0.0);

    float diffZ = stopB - startB;
    float scaleZ = 1.0/ diffZ;
    float shiftZ = 0.0 - startB*scaleZ;
    tempColor.z = tempColor.z * scaleZ;
    tempColor.z = tempColor.z + shiftZ;
    tempColor.z = min(tempColor.z, 1.0);
    tempColor.z = max(tempColor.z, 0.0);
    tempColor.y = tempColor.y / 1.0;
    tempColor.z = tempColor.z / 1.0;
    float r = mixRfromR * tempColor.x + mixRfromG * tempColor.y  + mixRfromB * tempColor.z;
    r = min(r, 1.0);
    r = max(r, 0.0);
    float b = mixBfromR * r + mixBfromG * tempColor.y  + mixBfromB * tempColor.z;
    b = min(b, 1.0);
    b = max(b, 0.0);
    tempColor.x = r;
    tempColor.z = b;
    gl_FragColor = tempColor;
}

)";


inline const std::string fragmentuvString = R"(
#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 vTextureCoords;
uniform samplerExternalOES sTexture;   //TODO android oes texture

uniform float startR;
uniform float stopR;
uniform float startG;
uniform float stopG;
uniform float startB;
uniform float stopB;
uniform int channelMix;
uniform float scalarBF;
uniform float scalarGF;
uniform float rofb;
uniform float rofg;
uniform float mixRfromR;
uniform float mixRfromG;
uniform float mixRfromB;
uniform float mixBfromR;
uniform float mixBfromG;
uniform float mixBfromB;

void main() {
  vec4 tempColor =  texture2D( sTexture, vTextureCoords );
  tempColor.y = tempColor.y ;//* 255.0;
  tempColor.z = tempColor.z ;//* 255.0;
    float diffX = stopR - startR;
    float scaleX = 1.0/ diffX;
    float shiftX = 0.0 - startR*scaleX;
    tempColor.x = tempColor.x * scaleX;
    tempColor.x = tempColor.x + shiftX;
    tempColor.x =  min(tempColor.x, 1.0);
    tempColor.x = max(tempColor.x, 0.0);
  float diffY = stopG - startG;
  float scaleY = 1.0/ diffY;
  float shiftY = 0.0 - startG*scaleY;
  tempColor.y = tempColor.y * scaleY;
  tempColor.y = tempColor.y + shiftY;
  tempColor.y =  min(tempColor.y, 1.0);
  tempColor.y = max(tempColor.y, 0.0);
  float diffZ = stopB - startB;
  float scaleZ = 1.0/ diffZ;
  float shiftZ = 0.0 - startB*scaleZ;
  tempColor.z = tempColor.z * scaleZ;
  tempColor.z = tempColor.z + shiftZ;
  tempColor.z = min(tempColor.z, 1.0);
  tempColor.z = max(tempColor.z, 0.0);
  tempColor.y = tempColor.y / 1.0;
  tempColor.z = tempColor.z / 1.0;
  float r = mixRfromR * tempColor.x + mixRfromG * tempColor.y  + mixRfromB * tempColor.z;
  r = min(r, 1.0);
  r = max(r, 0.0);
  float b = mixBfromR * r + mixBfromG * tempColor.y  + mixBfromB * tempColor.z;
  b = min(b, 1.0);
  b = max(b, 0.0);
  tempColor.x = r;
  tempColor.z = b;
  gl_FragColor = tempColor;
}
)";


inline const std::string fragmentuv_effectString = R"(
#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 vTextureCoords;
uniform samplerExternalOES sTexture;   //TODO android oes texture

uniform float startR;
uniform float stopR;
uniform float startG;
uniform float stopG;
uniform float startB;
uniform float stopB;
uniform int channelMix;
uniform float scalarBF;
uniform float scalarGF;
uniform float rofb;
uniform float rofg;
uniform float mixRfromR;
uniform float mixRfromG;
uniform float mixRfromB;
uniform float mixBfromR;
uniform float mixBfromG;
uniform float mixBfromB;
uniform float effectLimit;

void main() {
  vec4 tempColor =  texture2D( sTexture, vTextureCoords );
  tempColor.y = tempColor.y ;//* 255.0;
  tempColor.z = tempColor.z ;//* 255.0;
    float diffX = stopR - startR;
    float scaleX = 1.0/ diffX;
    float shiftX = 0.0 - startR*scaleX;
    tempColor.x = tempColor.x * scaleX;
    tempColor.x = tempColor.x + shiftX;
    tempColor.x =  min(tempColor.x, 1.0);
    tempColor.x = max(tempColor.x, 0.0);
  float diffY = stopG - startG;
  float scaleY = 1.0/ diffY;
  float shiftY = 0.0 - startG*scaleY;
  tempColor.y = tempColor.y * scaleY;
  tempColor.y = tempColor.y + shiftY;
  tempColor.y =  min(tempColor.y, 1.0);
  tempColor.y = max(tempColor.y, 0.0);
  float diffZ = stopB - startB;
  float scaleZ = 1.0/ diffZ;
  float shiftZ = 0.0 - startB*scaleZ;
  tempColor.z = tempColor.z * scaleZ;
  tempColor.z = tempColor.z + shiftZ;
  tempColor.z = min(tempColor.z, 1.0);
  tempColor.z = max(tempColor.z, 0.0);
  tempColor.y = tempColor.y / 1.0;
  tempColor.z = tempColor.z / 1.0;
  float r = mixRfromR * tempColor.x + mixRfromG * tempColor.y  + mixRfromB * tempColor.z;
  r = min(r, 1.0);
  r = max(r, 0.0);
  float b = mixBfromR * r + mixBfromG * tempColor.y  + mixBfromB * tempColor.z;
  b = min(b, 1.0);
  b = max(b, 0.0);
  tempColor.x = r;
  tempColor.z = b;
  gl_FragColor = tempColor;
  if(  gl_FragCoord.x > effectLimit )
  {
    gl_FragColor = texture2D( sTexture, vTextureCoords );
  }
}
)";
