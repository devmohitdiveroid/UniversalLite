#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 vTextureCoords;
uniform samplerExternalOES sTexture;



uniform float animationProgress;

uniform int orientation; // 0( 다이빙중 촬영본 ), 90(상태표시줄이 상단일때) , 180, 270 값이 들어오고 모든 orientation은 오른쪽에서 왼쪽으로 애니메이션이 된다.

void main() {


    float endLine = 1.0 - animationProgress;

    int isFilterTarger = 0;
    if( (orientation == 0   && 1.0 - vTextureCoords.x   < endLine ) ||
        (orientation == 90  && vTextureCoords.y         < endLine ) ||
        (orientation == 180 && vTextureCoords.x         < endLine)  ||
        (orientation == 270 && 1.0 - vTextureCoords.y   < endLine) ){
        isFilterTarger = 1;
    }


    if( isFilterTarger == 1) //vTextureCoords.x < endLine
    {
        gl_FragColor = texture2D(sTexture, vTextureCoords);



    }else
    {
        gl_FragColor.r = 1.0;
        gl_FragColor.g = 1.0;
        gl_FragColor.b = 1.0;
        gl_FragColor.a = 0.0;

    }
}