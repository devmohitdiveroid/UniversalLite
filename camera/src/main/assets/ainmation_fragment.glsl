precision mediump float;
varying vec2 vTextureCoords;
varying vec4 vColour;
uniform sampler2D sTexture;


uniform float animationProgress;

void main() {

    vec4 tempColor =  texture2D( sTexture, vTextureCoords );


    float endLine = 1.0 - animationProgress;

    if( vTextureCoords.x < endLine)
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
