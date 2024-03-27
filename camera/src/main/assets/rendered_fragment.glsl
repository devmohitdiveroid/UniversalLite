precision mediump float;
varying vec2 vTextureCoords;
varying vec4 vColour;
uniform sampler2D sTexture;


void main() {

    vec4 tempColor =  texture2D( sTexture, vTextureCoords );


    gl_FragColor = tempColor;


}
