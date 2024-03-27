//
// Created by monty on 23/11/15.
//


#include <EGL/egl.h>
#include <GLES3/gl3.h>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"

#include <string>

#include <android/log.h>
#include <GLES2/gl2ext.h>
#include <unistd.h>

#include "GLES2Lesson.h"
#include "NdkGlue.h"
#include "AnimationShader.h"


int GLES2Lesson::mDebugShowOriginMini = 0;
int GLES2Lesson::mDebugShowHistogram = 0;

float GLES2Lesson::mBottomMargin    = 0.0f;
float GLES2Lesson::mTopMargin       = 0.0f;
float GLES2Lesson::mLeftMargin      = 0.0f;
float GLES2Lesson::mRightMargin     = 0.0f;



int GLES2Lesson::videoOrientation = 0;
int GLES2Lesson::mAnimationUse = 0;
long long GLES2Lesson::mMAnimationTime = 0;


//Counter Clockwise
const float GLES2Lesson::cubeVertices[]{
//    4________5
//    /|       /|
//   / |      / |
// 0/__|___1_/  |
//  | 7|____|___|6
//  |  /    |  /
//  | /     | /
// 3|/______|/2
//x, y, z, r, g, b, u, v
        -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,    //0
        1.0f, 1.0f, 0.0f, 1.0f, 0.0f,     //1
        1.0f, -1.0f, 0.0f, 1.0f, 1.0f,   //2
        -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,   //3


        -1.0f, 1.0f, -1.0f, 0.0f, 0.0f,   //4
        1.0f, 1.0f, -1.0f, 1.0f, 0.0f,    //5
        1.0f, -1.0f, -1.0f, 1.0f, 1.0f,   //6
        -1.0f, -1.0f, -1.0f, 0.0f, 1.0f,   //7

        -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,    //0
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f,     //1
        1.0f, -1.0f, 1.0f, 1.0f, 0.0f,   //2
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,   //3
        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,   //4
        1.0f, 1.0f, -1.0f, 0.0f, 1.0f,    //5
        1.0f, -1.0f, -1.0f, 0.0f, 0.0f,   //6
        -1.0f, -1.0f, -1.0f, 1.0f, 0.0f   //7
};



//Counter Clockwise
const float GLES2Lesson::cubeVerticesReverse[]{
//    4________5
//    /|       /|
//   / |      / |
// 0/__|___1_/  |
//  | 7|____|___|6
//  |  /    |  /
//  | /     | /
// 3|/______|/2
//x, y, z, r, g, b, u, v
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,    //0
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f,     //1
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f,   //2
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,   //3


        -1.0f, 1.0f, -1.0f, 0.0f, 1.0f,   //4
        1.0f, 1.0f, -1.0f, 1.0f, 1.0f,    //5
        1.0f, -1.0f, -1.0f, 1.0f, 0.0f,   //6
        -1.0f, -1.0f, -1.0f, 0.0f, 0.0f,   //7

        -1.0f, 1.0f, 1.0f, 0.0f, 1.0f,    //0
        1.0f, 1.0f, 1.0f, 1.0f, 1.0f,     //1
        1.0f, -1.0f, 1.0f, 1.0f, 0.0f,   //2
        -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,   //3
        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f,   //4
        1.0f, 1.0f, -1.0f, 0.0f, 1.0f,    //5
        1.0f, -1.0f, -1.0f, 0.0f, 0.0f,   //6
        -1.0f, -1.0f, -1.0f, 1.0f, 0.0f   //7
};



const unsigned short GLES2Lesson::cubeIndices[]{
        0, 1, 2,
        0, 2, 3,

        5, 4, 7,
        5, 7, 6,

        4, 5, 1,
        0, 4, 1,

        6, 7, 2,
        2, 7, 3,

        9, 13, 14,
        9, 14, 10,

        12, 8, 15,
        8, 11, 15
};



/*

GLuint uploadTextureData(int *textureData, int width, int height) {
    // Texture object handle
    GLuint textureId = 0;

    //Generate texture storage
    glGenTextures(1, &textureId);

    //specify what we want for that texture
    glBindTexture(GL_TEXTURE_2D, textureId);

    //upload the data
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData );

    // Set the filtering mode - surprisingly, this is needed.
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    return textureId;
}
 */


extern void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

extern void checkGlError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}



float GLES2Lesson::valueRfromG = 0.8f ;
float GLES2Lesson::valueRfromB = 0.8f;
float GLES2Lesson::valueBreduceB= 0.8f;


int *GLES2Lesson::textureData = 0;
int GLES2Lesson::mTextureWidth = 128;
int GLES2Lesson::mTextureHeight= 128;
int GLES2Lesson::mTextureRotate =0;

GLuint GLES2Lesson::mTextureId = 0;

GLuint GLES2Lesson::mMiniTextureId = 0;
GLuint GLES2Lesson::mFrameBufferMiniTexture = 0;
GLuint GLES2Lesson::mHistogramTextureId = 0;


float GLES2Lesson::msClearColorR = 0.0f;
float GLES2Lesson::msClearColorG = 0.0f;
float GLES2Lesson::msClearColorB = 0.0f;



void GLES2Lesson::setValueRfromG( float value )
{
    valueRfromG = value;
}
void GLES2Lesson::setValueRfromB( float value )
{
    valueRfromB = value;
}

void GLES2Lesson::setValueBreduceB( float value )
{
    valueBreduceB= value;
}


int GLES2Lesson::mRenderingMode = VIEW_IMAGE;
void GLES2Lesson::changeRenderingMode(int renderingMode , int textureWidth, int textureHeight,int textureRotate, int renderTextureWidth, int renderTextureHeight)
{

    mFrameCount =0;

    resultValue.clear();//.push_back( preProcessorValue);
    averageResultValue = PreProcessorValue();



    mRenderingMode = renderingMode;


    mTextureWidth = textureWidth;
    mTextureHeight = textureHeight;
    mTextureRotate = textureRotate;

    mRenderTextureWidth = renderTextureWidth;
    mRenderTextureHeight = renderTextureHeight;


  //  mTextureId = uploadTextureData(textureData, textureWidth, textureHeight);

    /*

    enum {
        VIEW_IMAGE = 0,
        VIEW_MOVIE = 1,
        ENCODE_MOVIE=2,
        VIEW_REALTIME= 3,
        ENCODE_REALTIME=4,
    };
     */

    for(  std::map<std::string, GLuint >::iterator iter = m_UIImagesTexture.begin(); iter != m_UIImagesTexture.end(); ++iter)
    {
        GLuint temp = iter->second;
        glDeleteTextures( 1, &temp );
    }
    m_UIImagesTexture.clear();


    if( mRenderingMode == VIEW_IMAGE)
    {


       // mTextureId
        if( mTextureId > 0)
        {
            glDeleteTextures( 1, &mTextureId );

            mTextureId = 0;
        }

        // Texture object handle
        //GLuint textureId = 0;

        //Generate texture storage
        glGenTextures(1, &mTextureId);

        //specify what we want for that texture
        glBindTexture(GL_TEXTURE_2D, mTextureId);

        //upload the data
        // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData );

        //upload the data

        if( textureData )
        {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,(unsigned char*) textureData );
        }


        // Set the filtering mode - surprisingly, this is needed.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);



    } else   if( mRenderingMode == VIEW_MOVIE)
    {



        if( mTextureId > 0)
        {
            glDeleteTextures( 1, &mTextureId );

            mTextureId = 0;
        }

        // Texture object handle
        GLuint textureId = 0;

        //Generate texture storage
        glGenTextures(1, &textureId);

        //specify what we want for that texture

        glActiveTexture(textureId);

        /**
         * GL2JNILib#beforeStep 을 보면
         * GL2JNICamera2에서 surface와 이와 연결된 surfaceTextrue를 생성해두고
         * Camera의 Session을 생성할때 생성자로 surface를 넘긴다.
         * 그래서 surfaceTexture가 OPENGL의 consumer로 세팅된다.
         *
         * GL2JNICamera2의 startCamera에서 surface와 surfaceTexture를 생성하고
         * surfaceTextrue에 frame리스너를 등록한다.
         */
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);

        //upload the data
       // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData );

        // Set the filtering mode - surprisingly, this is needed.
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_LINEAR);//GL_NEAREST
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_LINEAR);//GL_NEAREST


       mTextureId =  textureId;

    }



    initRenderTextue(renderTextureWidth, renderTextureHeight);
    initMiniTextue(MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT);
    initHistogramTextue(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);



    for( std::map<std::string, TextureInfo >::iterator iter = m_UIImages.begin(); iter != m_UIImages.end(); ++iter)
    {
        continue;

        /*
        GLuint tempTexture = 0;


        glGenTextures(1, &tempTexture);

        //specify what we want for that texture
        glBindTexture(GL_TEXTURE_2D, tempTexture);

        //upload the data
        // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData );

        //upload the data


        //(*iter).second

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, iter->second.width, iter->second.height, 0, GL_RGBA, GL_UNSIGNED_BYTE,(unsigned char*) &iter->second.buf.operator[](0) );



        // Set the filtering mode - surprisingly, this is needed.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);


        m_UIImagesTexture[iter->first] = tempTexture;
*/

    }
   // m_UIImagesTexture.clear();





}


GLuint GLES2Lesson::m_FramebufferName = 0;
GLuint GLES2Lesson::m_renderedTexture = 0;

GLuint GLES2Lesson::getMainTextureId()
{
    return mTextureId;
}




GLuint GLES2Lesson::loadShader(GLenum shaderType, const char *pSource) {
    auto shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char *buf = (char *) malloc(infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
//                    LOGE("Could not compile shader %d:\n%s\n", shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint GLES2Lesson::createProgram(const char *pVertexSource, const char *pFragmentSource) {
    auto vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    auto pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    auto program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char *buf = (char *) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
//                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

void GLES2Lesson::printVerboseDriverInformation() {
    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);
}

GLES2Lesson::GLES2Lesson() {
//start off as identity - late we will init it with proper values.
    cubeTransformMatrix = glm::mat4(1.0f);
    projectionMatrix = glm::mat4(1.0f);
    //textureData = nullptr;
    //vertexAttributePosition = 0;
    //modelMatrixAttributePosition = 0;
    //projectionMatrixAttributePosition = 0;
    //gProgram = 0;
    cubeRotationAngleYZ = 0.0f;
    cubeRotationAngleXZ = 0.0f;
}

GLES2Lesson::~GLES2Lesson() {
    deleteVBOs();
    deleteTextures();
    //glDeleteTextures( 1, &mTextureId );



    delete mpAquaShader  ;       mpAquaShader = 0;
    delete mpAquaShaderUV;       mpAquaShaderUV= 0;
    delete mpAquaShaderUV_Effect;       mpAquaShaderUV_Effect= 0;
    delete mpRenderedShader;     mpRenderedShader = 0;
    delete mpRenderedShaderUV;   mpRenderedShaderUV = 0;

    delete mpAnimationShader ;mpAnimationShader = 0;
    delete mpAnimationShaderUV ;mpAnimationShader = 0;



}

void GLES2Lesson::deleteTextures()
{

    if( mTextureId > 0)    {
        glDeleteTextures( 1, &mTextureId );
        mTextureId = 0;
    }

    if( mMiniTextureId > 0)    {
        glDeleteTextures( 1, &mMiniTextureId );
        mMiniTextureId = 0;
    }

    if( mFrameBufferMiniTexture > 0)   {
        glDeleteTextures( 1, &mFrameBufferMiniTexture );
        mFrameBufferMiniTexture = 0;
    }


    if( m_FramebufferName > 0)   {
        glDeleteTextures( 1, &m_FramebufferName );
        m_FramebufferName = 0;
    }

    if( m_renderedTexture > 0)    {
        glDeleteTextures( 1, &m_renderedTexture );
        m_renderedTexture = 0;
    }




    if( mHistogramTextureId > 0)   {
        glDeleteTextures( 1, &mHistogramTextureId );
        mHistogramTextureId = 0;
    }






}


bool GLES2Lesson::init(float w, float h,int textureWidth____, int textureHeight____) {

    //FilterPreProcessor::checkRunThread();

//    LOGI("init () - begin");


    mFrameCount =0;

    resultValue.clear();//.push_back( preProcessorValue);
    averageResultValue = PreProcessorValue();



    printVerboseDriverInformation();

    LOGI("init () - deleteShader");

    delete mpAquaShader  ;       mpAquaShader = 0;
    delete mpAquaShaderUV;       mpAquaShaderUV= 0;
    delete mpRenderedShader;     mpRenderedShader = 0;
    delete mpRenderedShaderUV;   mpRenderedShaderUV = 0;
    delete mpAnimationShader ;mpAnimationShader = 0;
    delete mpAnimationShaderUV ;mpAnimationShaderUV = 0;


    LOGI("init () - createShader");

    mpAquaShader         = new AquaShader (0);
    mpAquaShaderUV    = new AquaShader (1);
    mpAquaShaderUV_Effect    = new AquaShader (2);
    mpRenderedShader     = new RenderedShader(false);
    mpRenderedShaderUV     = new RenderedShader(true);
    mpAnimationShader = new AnimationShader(false);
    mpAnimationShaderUV = new AnimationShader(true);

  //  gProgram = createProgram(vertexShader.c_str(), fragmentShader.c_str());

  //  if (!gProgram) {
  //      LOGE("Could not create program.");
  //      return false;
   // }

   // fetchShaderLocations();

//    LOGI("init () - glViewport");

    glViewport(0, 0, w, h);
    checkGlError("glViewport");


   // GLES2Lesson::triangleTransformMatrix = glm::translate( glm::mat4(1.0f), glm::vec3( -1.5f, 0.0f, -6.0f ) );

   // GLES2Lesson::squareTransformMatrix = glm::translate( glm::mat4(1.0f), glm::vec3( 1.5f, 0.0f, -6.0f ) );

    //projectionMatrix = glm::perspective(45.0f, w / h, 0.1f, 100.0f);
  //  projectionMatrix = glm::perspective(90.0f, 1.0f, 0.0f, 100.0f);


//    LOGI("init () - projectionMatrix");

    projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(w),
                                  static_cast<GLfloat>( h ), 0.0f, -1.0f, 1.0f);




    GLES2Lesson::mScreenWidth = w;
    GLES2Lesson::mScreenHeight = h;
    //initRenderTextue(textureWidth, textureHeight);

    LOGI("init () - deleteTextures w=%f h=%f",w,h);

    deleteTextures();

    LOGI("init () - deleteVBOs");
    deleteVBOs();

    LOGI("init () - createVBOs");
    createVBOs();


    //clearUI Texture


    LOGI("init () - changeRenderingMode");
    changeRenderingMode(mRenderingMode, mTextureWidth, mTextureHeight,mTextureRotate, mRenderTextureWidth, mRenderTextureHeight);




    //generate uiTexture




    LOGI("init () - etc Setting");

    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);
    glFrontFace(GL_CW);
    glDepthMask(true);

    LOGI("init () - end");

    return true;
}






//bool GLES2Lesson::dirted = false;
//char * GLES2Lesson::dirtedTextureData = 0;
//int GLES2Lesson::dirtedWidth = 0 ;
//int GLES2Lesson::dirtedHeight = 0 ;


void GLES2Lesson::updateTextureDate(int width, int height, char * textureDataIn)
{



    //dirted = true;
    textureData = (int*)textureDataIn;

    mTextureWidth = width;
    mTextureHeight= height;














}



float GLES2Lesson::mScreenWidth = 0.0f;
float GLES2Lesson::mScreenHeight = 0.0f;


//float GLES2Lesson::mTextureWidthInScreen  = 0.0f;
//float GLES2Lesson::mTextureHeightInScreen = 0.0f;


std::vector<PreProcessorValue> GLES2Lesson::resultValue;
PreProcessorValue GLES2Lesson::averageResultValue;


std::map<std::string, GLES2Lesson::TextureInfo > GLES2Lesson::m_UIImages;
std::map<std::string, GLuint > GLES2Lesson::m_UIImagesTexture;



void GLES2Lesson::resetTransformMatrices(int srctWidth, int srcHeight , int dstWidth, int dstHeight ) {
    glm::mat4 identity = glm::mat4(1.0f);




    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;




    //identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), 0.5f * size.y, 0.0f));

    identity = glm::translate(identity, glm::vec3( 0.5f * dstWidth , 0.5f * dstHeight , 0.0f));


    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
    // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;




    /*

    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), 0.5f * size.y, 0.0f));

    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
   // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;

     */



    //glm::vec3 translate = glm::vec3(0.0f, 0.0f, -2.0f );// * GLES2Lesson::mScreenHeight / GLES2Lesson::mScreenWidth);
    //glm::vec3 xAxis = glm::vec3(1.0f, 0.0f, 0.0f);
    //glm::vec3 yAxis = glm::vec3(0.0f, 1.0f, 0.0f);
    //glm::mat4 translated = glm::translate(identity, translate);
   // glm::mat4 rotatedAroundXAxis = glm::rotate(translated, cubeRotationAngleYZ, xAxis);
   // glm::mat4 rotatedAroundYAxis = glm::rotate(rotatedAroundXAxis, cubeRotationAngleXZ, yAxis);
    //cubeTransformMatrix =translated;// rotatedAroundYAxis;
}





/*
void GLES2Lesson::fetchShaderLocations() {

    vertexAttributePosition = glGetAttribLocation(gProgram, "aPosition");
    modelMatrixAttributePosition = glGetUniformLocation(gProgram, "uModel");
    projectionMatrixAttributePosition = glGetUniformLocation(gProgram, "uProjection");
    samplerUniformPosition = glGetUniformLocation(gProgram, "sTexture");
    textureCoordinatesAttributePosition = glGetAttribLocation(gProgram, "aTexCoord");




    fChannelMixFromGreenPosition = glGetUniformLocation(gProgram, "fChannelMixFromGreen");
    fChannelMixFromBluePosition = glGetUniformLocation(gProgram, "fChannelMixFromBlue");
    fReduceBluePosition = glGetUniformLocation(gProgram, "fReduceBlue");





    startR_Position  = glGetUniformLocation(gProgram, "startR");;
    stopR_Position = glGetUniformLocation(gProgram, "stopR");;

    startG_Position= glGetUniformLocation(gProgram, "startG");;
    stopG_Position= glGetUniformLocation(gProgram, "stopG");;

    startB_Position= glGetUniformLocation(gProgram, "startB");;
    stopB_Position= glGetUniformLocation(gProgram, "stopB");;

    channelMix_Position= glGetUniformLocation(gProgram, "channelMix");;


    scalarBF_Position= glGetUniformLocation(gProgram, "scalarBF");;
    scalarGF_Position= glGetUniformLocation(gProgram, "scalarGF");;



    rofg_Position= glGetUniformLocation(gProgram, "rofg");
    rofb_Position= glGetUniformLocation(gProgram, "rofb");










}
*/



void GLES2Lesson::resetTransformMatrices2(float srctWidth, float srcHeight , float dstWidth, float dstHeight ) {
    glm::mat4 identity = glm::mat4(1.0f);




//    LOGE("OJKTEST : resetTransformMatrices2");
    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * dstWidth , 0.5f * dstHeight, 0));

    identity = glm::scale(identity, glm::vec3(dstWidth * 0.5,dstHeight * 0.5,  1.0f));
    // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;




    /*

    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), 0.5f * size.y, 0.0f));

    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
   // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;

     */



    //glm::vec3 translate = glm::vec3(0.0f, 0.0f, -2.0f );// * GLES2Lesson::mScreenHeight / GLES2Lesson::mScreenWidth);
    //glm::vec3 xAxis = glm::vec3(1.0f, 0.0f, 0.0f);
    //glm::vec3 yAxis = glm::vec3(0.0f, 1.0f, 0.0f);
    //glm::mat4 translated = glm::translate(identity, translate);
    // glm::mat4 rotatedAroundXAxis = glm::rotate(translated, cubeRotationAngleYZ, xAxis);
    // glm::mat4 rotatedAroundYAxis = glm::rotate(rotatedAroundXAxis, cubeRotationAngleXZ, yAxis);
    //cubeTransformMatrix =translated;// rotatedAroundYAxis;
}
/*
void GLES2Lesson::fetchShaderLocations() {

    vertexAttributePosition = glGetAttribLocation(gProgram, "aPosition");
    modelMatrixAttributePosition = glGetUniformLocation(gProgram, "uModel");
    projectionMatrixAttributePosition = glGetUniformLocation(gProgram, "uProjection");
    samplerUniformPosition = glGetUniformLocation(gProgram, "sTexture");
    textureCoordinatesAttributePosition = glGetAttribLocation(gProgram, "aTexCoord");




    fChannelMixFromGreenPosition = glGetUniformLocation(gProgram, "fChannelMixFromGreen");
    fChannelMixFromBluePosition = glGetUniformLocation(gProgram, "fChannelMixFromBlue");
    fReduceBluePosition = glGetUniformLocation(gProgram, "fReduceBlue");





    startR_Position  = glGetUniformLocation(gProgram, "startR");;
    stopR_Position = glGetUniformLocation(gProgram, "stopR");;

    startG_Position= glGetUniformLocation(gProgram, "startG");;
    stopG_Position= glGetUniformLocation(gProgram, "stopG");;

    startB_Position= glGetUniformLocation(gProgram, "startB");;
    stopB_Position= glGetUniformLocation(gProgram, "stopB");;

    channelMix_Position= glGetUniformLocation(gProgram, "channelMix");;


    scalarBF_Position= glGetUniformLocation(gProgram, "scalarBF");;
    scalarGF_Position= glGetUniformLocation(gProgram, "scalarGF");;



    rofg_Position= glGetUniformLocation(gProgram, "rofg");
    rofb_Position= glGetUniformLocation(gProgram, "rofb");










}
*/




void GLES2Lesson::resetTransformMatrices3(float srctWidth, float srcHeight, float dstWidth,
                                          float dstHeight) {

    //if( mDebugShowOriginMini == 1) {
    //호출안됨
//    LOGE("OJKTEST : resetTransformMatrices3");
    glm::mat4 identity = glm::mat4(1.0f);




    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), size.y + size.y , 0.0f));

    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
    // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;




    /*

    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), 0.5f * size.y, 0.0f));

    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
   // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;

     */



    //glm::vec3 translate = glm::vec3(0.0f, 0.0f, -2.0f );// * GLES2Lesson::mScreenHeight / GLES2Lesson::mScreenWidth);
    //glm::vec3 xAxis = glm::vec3(1.0f, 0.0f, 0.0f);
    //glm::vec3 yAxis = glm::vec3(0.0f, 1.0f, 0.0f);
    //glm::mat4 translated = glm::translate(identity, translate);
    // glm::mat4 rotatedAroundXAxis = glm::rotate(translated, cubeRotationAngleYZ, xAxis);
    // glm::mat4 rotatedAroundYAxis = glm::rotate(rotatedAroundXAxis, cubeRotationAngleXZ, yAxis);
    //cubeTransformMatrix =translated;// rotatedAroundYAxis;
}


void GLES2Lesson::resetTransformMatrices4(float srctWidth, float srcHeight, float dstWidth,
                                          float dstHeight) {

    //*  Render HISTORAM
    glm::mat4 identity = glm::mat4(1.0f);




    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3( dstWidth + dstWidth + dstWidth, size.y +size.y +size.y + size.y  + size.y , 0.0f));

    identity = glm::scale(identity, glm::vec3(size , 1.0f));
    // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;




    /*

    glm::vec2 size;
    size.x = srctWidth;
    size.y = srcHeight;


    identity = glm::translate(identity, glm::vec3(0.5f * size.x + 0.5f * (dstWidth - srctWidth), 0.5f * size.y, 0.0f));

    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
   // identity = glm::scale(identity, glm::vec3(size, 1.0f));

    cubeTransformMatrix =identity;

     */



    //glm::vec3 translate = glm::vec3(0.0f, 0.0f, -2.0f );// * GLES2Lesson::mScreenHeight / GLES2Lesson::mScreenWidth);
    //glm::vec3 xAxis = glm::vec3(1.0f, 0.0f, 0.0f);
    //glm::vec3 yAxis = glm::vec3(0.0f, 1.0f, 0.0f);
    //glm::mat4 translated = glm::translate(identity, translate);
    // glm::mat4 rotatedAroundXAxis = glm::rotate(translated, cubeRotationAngleYZ, xAxis);
    // glm::mat4 rotatedAroundYAxis = glm::rotate(rotatedAroundXAxis, cubeRotationAngleXZ, yAxis);
    //cubeTransformMatrix =translated;// rotatedAroundYAxis;
}


/**
 * RenderShader를 세팅하는 곳이다
 * GL세팅이 drawGeometry아래 2개 뺴고 모두 동일하다.
 * scalarBF_Position : Blue의 스케일을 조정한다.
 * scalarGF_Position : Green 스케일을 조정한다.
 */
void GLES2Lesson::drawGeometryRendered(const int vertexVbo, const int indexVbo, int vertexCount,
                               const glm::mat4 &transform, RenderedShader * pRenderedShader) {

    glBindBuffer(GL_ARRAY_BUFFER, vertexVbo);
    glEnableVertexAttribArray(pRenderedShader->vertexAttributePosition);
    glEnableVertexAttribArray(pRenderedShader->textureCoordinatesAttributePosition);





    glUniform1f(pRenderedShader->startR_Position ,float(averageResultValue.startR) / float(1.0f) );
    glUniform1f(pRenderedShader->stopR_Position ,float(averageResultValue.endR) / float(1.0f));

    glUniform1f(pRenderedShader->startG_Position ,float(averageResultValue.startG) / float(1.0f) );
    glUniform1f(pRenderedShader->stopG_Position ,float(averageResultValue.endG) / float(1.0f));

    glUniform1f(pRenderedShader->startB_Position ,float(averageResultValue.startB) / float(1.0f) );
    glUniform1f(pRenderedShader->stopB_Position ,float(averageResultValue.endB) / float(1.0f));



    //glUniform1i(pRenderedShader->channelMix_Position ,resultValue.isChannelMix );



    glUniform1f(pRenderedShader->scalarBF_Position ,GLES2Lesson::valueRfromB);
    glUniform1f(pRenderedShader->scalarGF_Position ,GLES2Lesson::valueRfromG);




    //glUniform1f(pRenderedShader->rofg_Position ,resultValue.rofg );
    //glUniform1f(pRenderedShader->rofb_Position ,resultValue.rofb );



    glUniform1f(pRenderedShader->mixRfromR_Position ,averageResultValue.mixRfromR );
    glUniform1f(pRenderedShader->mixRfromG_Position ,averageResultValue.mixRfromG );
    glUniform1f(pRenderedShader->mixRfromB_Position ,averageResultValue.mixRfromB );


    glUniform1f(pRenderedShader->mixBfromR_Position ,averageResultValue.mixBfromR );
    glUniform1f(pRenderedShader->mixBfromG_Position ,averageResultValue.mixBfromG );
    glUniform1f(pRenderedShader->mixBfromB_Position ,averageResultValue.mixBfromB );




    /**
     * 분석결과 아래내용은 draw메소드들에서 모두 동일하다
     */
    //0 is for texturing unit 0 (since we never changed it)
    glUniform1i(pRenderedShader->samplerUniformPosition, 0);
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureId);


    glUniformMatrix4fv(pRenderedShader->modelMatrixAttributePosition, 1, false, &transform[0][0]);
    glVertexAttribPointer(pRenderedShader->vertexAttributePosition, 3, GL_FLOAT, GL_FALSE, sizeof(float) * 5, 0);
    glVertexAttribPointer(pRenderedShader->textureCoordinatesAttributePosition, 2, GL_FLOAT, GL_TRUE,
                          sizeof(float) * 5, (void *) (sizeof(float) * 3));

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVbo);
    glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_SHORT, 0);

    glDisableVertexAttribArray(pRenderedShader->vertexAttributePosition);
    glDisableVertexAttribArray(pRenderedShader->textureCoordinatesAttributePosition);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}







/**
 * AcuaShader에 세팅을 하는 곳, 아래 쪽에 사용된다.
 * Scale변수가 빠져있는 렌더링 세팅부분이다.
 * render1() :
 * render_norendertexture : 테스트용도로 사용되었음
 * renderEncodenorendertexture : 인코딩 부분에 사용되었음
 */
void GLES2Lesson::drawGeometry(const int vertexVbo, const int indexVbo, int vertexCount,
                               const glm::mat4 &transform, AquaShader * pAquaShader) {

    glBindBuffer(GL_ARRAY_BUFFER, vertexVbo);
    glEnableVertexAttribArray(pAquaShader->vertexAttributePosition);
    glEnableVertexAttribArray(pAquaShader->textureCoordinatesAttributePosition);




    glUniform1f(pAquaShader->startR_Position ,float(averageResultValue.startR) / float(1.0f) );
    glUniform1f(pAquaShader->stopR_Position ,float(averageResultValue.endR) / float(1.0f));
    glUniform1f(pAquaShader->startG_Position ,float(averageResultValue.startG) / float(1.0f) );
    glUniform1f(pAquaShader->stopG_Position ,float(averageResultValue.endG) / float(1.0f));
    glUniform1f(pAquaShader->startB_Position ,float(averageResultValue.startB) / float(1.0f) );
    glUniform1f(pAquaShader->stopB_Position ,float(averageResultValue.endB) / float(1.0f));




    glUniform1f(pAquaShader->mixRfromR_Position ,averageResultValue.mixRfromR );
    glUniform1f(pAquaShader->mixRfromG_Position ,averageResultValue.mixRfromG );
    glUniform1f(pAquaShader->mixRfromB_Position ,averageResultValue.mixRfromB );


    glUniform1f(pAquaShader->mixBfromR_Position ,averageResultValue.mixBfromR );
    glUniform1f(pAquaShader->mixBfromG_Position ,averageResultValue.mixBfromG );
    glUniform1f(pAquaShader->mixBfromB_Position ,averageResultValue.mixBfromB );





    /**
     * 분석결과 아래내용은 draw메소드들에서 모두 동일하다
     */
    //0 is for texturing unit 0 (since we never changed it)
    glUniform1i(pAquaShader->samplerUniformPosition, 0);
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureId);


    glUniformMatrix4fv(pAquaShader->modelMatrixAttributePosition, 1, false, &transform[0][0]);
    glVertexAttribPointer(pAquaShader->vertexAttributePosition, 3, GL_FLOAT, GL_FALSE, sizeof(float) * 5, 0);
    glVertexAttribPointer(pAquaShader->textureCoordinatesAttributePosition, 2, GL_FLOAT, GL_TRUE,
                          sizeof(float) * 5, (void *) (sizeof(float) * 3));

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVbo);
    glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_SHORT, 0);

    glDisableVertexAttribArray(pAquaShader->vertexAttributePosition);
    glDisableVertexAttribArray(pAquaShader->textureCoordinatesAttributePosition);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}





void GLES2Lesson::drawAnimationGeometry(const int vertexVbo, const int indexVbo, int vertexCount,
                               const glm::mat4 &transform, AnimationShader * animationShader, float progress ) {
    drawAnimationGeometry(vertexVbo, indexVbo, vertexCount, transform, animationShader, progress, GLES2Lesson::videoOrientation );
}
void GLES2Lesson::drawAnimationGeometry(const int vertexVbo, const int indexVbo, int vertexCount,
                               const glm::mat4 &transform, AnimationShader * animationShader, float progress, int orientation ) {
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


    glBindBuffer(GL_ARRAY_BUFFER, vertexVbo);
    glEnableVertexAttribArray(animationShader->vertexAttributePosition);
    glEnableVertexAttribArray(animationShader->textureCoordinatesAttributePosition);



    glUniform1f(animationShader->animationProgressPosition,progress );
    glUniform1i(animationShader->orientationPosition,   orientation );
//    LOGI("VideoOrientation drawAnimationGeometry %d",   orientation );



    /**
     * 분석결과 아래내용은 drawGeometryRendered 와 동일
     */
    //0 is for texturing unit 0 (since we never changed it)
    glUniform1i(animationShader->samplerUniformPosition, 0);
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureId);


    glUniformMatrix4fv(animationShader->modelMatrixAttributePosition, 1, false, &transform[0][0]);
    glVertexAttribPointer(animationShader->vertexAttributePosition, 3, GL_FLOAT, GL_FALSE, sizeof(float) * 5, 0);
    glVertexAttribPointer(animationShader->textureCoordinatesAttributePosition, 2, GL_FLOAT, GL_TRUE,
                          sizeof(float) * 5, (void *) (sizeof(float) * 3));

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVbo);
    glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_SHORT, 0);

    glDisableVertexAttribArray(animationShader->vertexAttributePosition);
    glDisableVertexAttribArray(animationShader->textureCoordinatesAttributePosition);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


 //   glDisable(GL_BLEND);
}


void GLES2Lesson::deleteVBOs() {

    if( vboCubeVertexDataIndex > 0){
        glDeleteBuffers(1, &vboCubeVertexDataIndex);
        vboCubeVertexDataIndex = 0;
    }

    if( vboCubeVertexDataIndexReverse > 0){
        glDeleteBuffers(1, &vboCubeVertexDataIndexReverse);
        vboCubeVertexDataIndexReverse = 0;
    }

    if( vboCubeVertexIndicesIndex > 0){
        glDeleteBuffers(1, &vboCubeVertexIndicesIndex);
        vboCubeVertexIndicesIndex = 0;
    }

}

void GLES2Lesson::createVBOs() {
    glGenBuffers(1, &vboCubeVertexDataIndex);
    glBindBuffer(GL_ARRAY_BUFFER, vboCubeVertexDataIndex);
    glBufferData(GL_ARRAY_BUFFER, 16 * sizeof(float) * 5, cubeVertices, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);


    glGenBuffers(1, &vboCubeVertexDataIndexReverse);
    glBindBuffer(GL_ARRAY_BUFFER, vboCubeVertexDataIndexReverse);
    glBufferData(GL_ARRAY_BUFFER, 16 * sizeof(float) * 5, cubeVerticesReverse, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);




    glGenBuffers(1, &vboCubeVertexIndicesIndex);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboCubeVertexIndicesIndex);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, 36 * sizeof(GLushort), cubeIndices, GL_STATIC_DRAW);



   // glBufferData(GL_ELEMENT_ARRAY_BUFFER, 6 * sizeof(GLushort), cubeIndices, GL_STATIC_DRAW);



    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
}




void GLES2Lesson::clearBuffers() {


    glClearColor(msClearColorR, msClearColorG, msClearColorB, 1.0f);
    glClearDepthf(1.0f);
//    checkGlError("glClearColor");
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
//    checkGlError("glClear");
}






void GLES2Lesson::shutdown() {
   // delete textureData;
//    LOGI("Shutdown!\n");
}


float GLES2Lesson::mRenderTextureWidth = 0;
float GLES2Lesson::mRenderTextureHeight= 0 ;

static glm::mat4 resetTransformMatricesFitin(int srcWidth, int srcHeight ,int srcRotate,  int dstWidth, int dstHeight, int marginLeft, int marginTop, int marginRight, int marginBottom ) {
//    LOGE("OJKTEST FitInBefore: %d %d ",srcWidth, srcHeight);
//    LOGE("OJKTEST FitInBefore: %d %d ",dstWidth, dstHeight);




    if( srcRotate == 90 || srcRotate == 270)
    {
        std::swap(srcWidth, srcHeight);
    }

    dstWidth = (dstWidth - marginLeft - marginRight);
    dstHeight = (dstHeight  - marginTop - marginBottom);

    float rateW = dstWidth / float(srcWidth);
    float rateH = dstHeight / float(srcHeight);

    float computedWidth = 0;
    float computedHeight = 0;

//    if( rateW < rateH)
    if( rateW > rateH)
    {
        computedWidth = rateH * srcWidth;
        computedHeight = rateH * srcHeight;

    } else{
        computedWidth = rateW * srcWidth;
        computedHeight = rateW * srcHeight;
    }

//    LOGE("OJKTEST FitInResult: %f %f ",computedWidth, computedHeight);
//    LOGE("OJKTEST FitInResult: %d %d ",dstWidth, dstHeight);
//    LOGE("OJKTEST FitInResult: %d, %d, %d %d",marginLeft, marginTop, marginRight, marginBottom);

    glm::mat4 identity = glm::mat4(1.0f);

    glm::vec2 size;
    size.x = computedWidth;
    size.y = computedHeight;


    glm::vec3 center;
    center.x =  0.0;
    center.y =  0.0;
    center.z = 1.0f;



    identity = glm::translate(identity, glm::vec3( 0.5f * dstWidth + marginLeft, 0.5f * dstHeight +  marginTop , 0.0f));
    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));
    identity = glm::rotate(identity,(float)srcRotate, center );


    return identity;
}


static glm::mat4 resetTransformMatricesCropin(int srcWidth, int srcHeight , int dstWidth, int dstHeight ) {

//    LOGE("OJKTEST : resetTransformMatricesCropin");

    float rateW = dstWidth / float(srcWidth);
    float rateH = dstHeight / float(srcHeight);


    float computedWidth = 0;
    float computedHeight = 0;

    if( rateW < rateH)
    {
        computedWidth = rateH * srcWidth;
        computedHeight = rateH * srcHeight;

    } else{
        computedWidth = rateW * srcWidth;
        computedHeight = rateW * srcHeight;
    }

    glm::mat4 identity = glm::mat4(1.0f);

    glm::vec2 size;
    size.x = computedWidth;
    size.y = computedHeight;

    identity = glm::translate(identity, glm::vec3( 0.5f * dstWidth , 0.5f * dstHeight , 0.0f));
    identity = glm::scale(identity, glm::vec3(size * 0.5f, 1.0f));

    return identity;
}



void GLES2Lesson::computeAverage(PreProcessorValue preProcessorValue)
{

    long computedTime = current_timestamp() - 1000;



    for( int i = 0 ; i < resultValue.size(); ++i)
    {
        if( resultValue[i].computedTime < computedTime)
        {

        } else{

            if( i > 0 )
            {
                resultValue.erase(resultValue.begin() , resultValue.begin() + i);
            }
            break;
        }

    }

    resultValue.push_back( preProcessorValue);



    float startR = 0;
    float startG = 0;
    float startB = 0;

    float endR = 0.0f;
    float endG = 0.0f;
    float endB = 0.0f;


    float mixRfromB = 0.0f;
    float mixRfromG = 0.0f;
    float mixRfromR = 0.0f;

    float mixBfromB = 0.0f;
    float mixBfromG = 0.0f;
    float mixBfromR = 0.0f;



    for( int i = 0 ; i < resultValue.size(); ++i)
    {

        startR += resultValue[i].startR;
        startG += resultValue[i].startG;
        startB += resultValue[i].startB;

        endR += resultValue[i].endR;
        endG += resultValue[i].endG;
        endB += resultValue[i].endB;

        mixRfromB += resultValue[i].mixRfromB;
        mixRfromG += resultValue[i].mixRfromG;
        mixRfromR += resultValue[i].mixRfromR;

        mixBfromB += resultValue[i].mixBfromB;
        mixBfromG += resultValue[i].mixBfromG;
        mixBfromR += resultValue[i].mixBfromR;

    }



    averageResultValue.startR = startR / float(resultValue.size());
    averageResultValue.startG = startG / float(resultValue.size());
    averageResultValue.startB = startB / float(resultValue.size());

    averageResultValue.endR = endR /  float(resultValue.size());
    averageResultValue.endG = endG /  float(resultValue.size());
    averageResultValue.endB = endB /  float(resultValue.size());

    averageResultValue.mixRfromB = mixRfromB / float(resultValue.size());
    averageResultValue.mixRfromG = mixRfromG / float(resultValue.size());
    averageResultValue.mixRfromR = mixRfromR / float(resultValue.size());

    averageResultValue.mixBfromB = mixBfromB / float(resultValue.size());
    averageResultValue.mixBfromG = mixBfromG / float(resultValue.size());
    averageResultValue.mixBfromR = mixBfromR / float(resultValue.size());



}

void GLES2Lesson::render() {
//    LOGI( "GLES2Lesson::function:render");
    render1(0);
    render2(0);
}

void GLES2Lesson::render1(int isFront) {

//    LOGI( "GLES2Lesson::function:render1");
    ++mFrameCount;



    AquaShaderBase * aquaShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        aquaShaderBase = mpAquaShader;
    } else{

        aquaShaderBase = mpAquaShaderUV;
    }


    RenderedShaderBase * miniShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        miniShaderBase = mpRenderedShader;
    } else{

        miniShaderBase = mpRenderedShaderUV;
    }


    if( mRenderingMode == VIEW_IMAGE) {

        //specify what we want for that texture
        //glActiveTexture(GL_TEXTURE0);
        // glBindTexture(GL_TEXTURE_2D, mTextureId);




        // if (dirted) {



        //upload the data
        ///     glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, dirtedWidth, dirtedHeight, 0, GL_RGBA,
        //                   GL_UNSIGNED_BYTE,
        //                textureData);


        //   textureWidth = dirtedWidth;
        //    textureHeight = dirtedHeight;


        //}



    } else{

    }


    if( FilterPreProcessor::bEnableFilter == 1  && FilterPreProcessor::inputValueScaleAll > 0.00001 ) {

        if( mFrameCount == 1 )//첫프레임은 다르게 처리 해야함
        {

            while(  FilterPreProcessor::inputFlag == 1 ) {
                usleep(10);
            }

            resultValue.clear();
        }


        if( FilterPreProcessor::inputFlag == 0)
        {
            //


            /******************************************
            //render to our MiniBuffer => 현재 화질의 Frame을 분석하지 않고 작은 화질의 Frame을 분석하기 위해서 miniBuffer를 생성한다.
            //사용하고 있는 Texture채널은 GL_TEXTURE0 1개만 사용하고 있고 모든 Shader에 전달되는 texture도 1개( GL_TEXTURE0 , index=0 )이다.
            ***********************************************/

            glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferMiniTexture);
            glViewport(0, 0, MINITEXTURE_WIDTH,
                       MINITEXTURE_HEIGHT); // Render on the whole framebuffer, complete from the lower left corner to the upper right



            projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(MINITEXTURE_WIDTH),
                                          static_cast<GLfloat>( MINITEXTURE_HEIGHT), 0.0f, -1.0f, 1.0f);


            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mTextureId);

            clearBuffers();
            miniShaderBase->prepareShaderProgram();
            miniShaderBase->setPerspective(projectionMatrix);
            resetTransformMatrices2(mRenderTextureWidth, mRenderTextureHeight, MINITEXTURE_WIDTH,
                                    MINITEXTURE_HEIGHT);


            updateTextureSizeInScreen(mTextureWidth, mTextureWidth);


            if (mRenderingMode == VIEW_IMAGE) {


                drawGeometryRendered(vboCubeVertexDataIndex,
                                     vboCubeVertexIndicesIndex,
                                     6,//36,
                                     cubeTransformMatrix,
                                     mpRenderedShader
                );


            } else {


                drawGeometryRendered(vboCubeVertexDataIndex,
                                     vboCubeVertexIndicesIndex,
                                     6,//36,
                                     cubeTransformMatrix,
                                     mpRenderedShaderUV
                );


            }




            //  glBindTexture(GL_TEXTURE_2D, mMiniTextureId);


            glReadPixels(0, 0, MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, &FilterPreProcessor::preProcessorValue.minitexture[0][0]);

            FilterPreProcessor::inputFlag = 1;

        }


        if( mFrameCount == 1 )//첫프레임은 다르게 처리 해야함
        {

            while(  FilterPreProcessor::outputFlag == 0 ) {
                usleep(10);
            }
        }





        if( FilterPreProcessor::outputFlag == 1)
        {


            computeAverage(FilterPreProcessor::preProcessorValue);

            FilterPreProcessor::outputFlag = 0;


            if(mDebugShowHistogram == 1 ) {



                //specify what we want for that texture
                //glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, mHistogramTextureId);


                //upload the data
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, 0, GL_RGBA,
                             GL_UNSIGNED_BYTE,
                             (char *) &PreProcessorValue::histogram[0][0]);

                glBindTexture(GL_TEXTURE_2D, 0);

            }





        }



    } else{


        averageResultValue = PreProcessorValue();

        resultValue.clear();



        FilterPreProcessor::outputFlag = 0;




    }


    /*************************************************
    // Render to our renderTexture
    *******************************************************/
    glBindFramebuffer(GL_FRAMEBUFFER, m_FramebufferName);
    glViewport(0,0, mRenderTextureWidth, mRenderTextureHeight); // Render on the whole framebuffer, complete from the lower left corner to the upper right



    projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(mRenderTextureWidth),
                                  static_cast<GLfloat>( mRenderTextureHeight), 0.0f, -1.0f, 1.0f);






    clearBuffers();
    aquaShaderBase->prepareShaderProgram();
    aquaShaderBase->setPerspective(projectionMatrix);
    // resetTransformMatrices(mRenderTextureWidth, mRenderTextureHeight, mRenderTextureWidth, mRenderTextureHeight);






    //GLES2Lesson::mTextureWidthInScreen = mRenderTextureWidth;
    //GLES2Lesson::mTextureHeightInScreen = mRenderTextureWidth;

    //updateTextureSizeInScreen(mRenderTextureWidth, mRenderTextureHeight);



    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId);




    if( mRenderingMode == VIEW_IMAGE) {

//        LOGE("JIS : resetTransformMatricesCropin 1");
        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, mRenderTextureWidth, mRenderTextureHeight );


        drawGeometry(vboCubeVertexDataIndex,
                     vboCubeVertexIndicesIndex,
                     6,//36,
                     cubeTransformMatrix,
                     mpAquaShader
        );




    } else{


        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, mRenderTextureWidth, mRenderTextureHeight );

        if(isFront == 1)//앞면을 촬영 중일때는 좌우를 반전 해야 함
        {
            cubeTransformMatrix = glm::scale(cubeTransformMatrix, glm::vec3(-1.0f, 1.0f, 1.0f));

        }

        drawGeometry(vboCubeVertexDataIndex,
                     vboCubeVertexIndicesIndex,
                     6,//36,
                     cubeTransformMatrix,
                     mpAquaShaderUV
        );

    }








}


void GLES2Lesson::render2(int isFront) {

//    LOGI( "GLES2Lesson::function:render2");
    /*************************************************
    // write to out screen( preview화면 - 실시간, 영상, 사진 모두 )
    *******************************************************/
    // Render to the screen
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    // Render on the whole framebuffer, complete from the lower left corner to the upper right


    glViewport(0,0,mScreenWidth,mScreenHeight); // Render on the whole framebuffer, complete from the lower left corner to the upper right



    projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(mScreenWidth),
                                  static_cast<GLfloat>( mScreenHeight), 0.0f, -1.0f, 1.0f);




    // Clear the screen
    //glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // Use our shader
    clearBuffers();
    //glGetError()

    mpRenderedShader->prepareShaderProgram();
    mpRenderedShader->setPerspective(projectionMatrix);
    //resetTransformMatrices( mRenderTextureWidth, mRenderTextureHeight,  mScreenWidth, mScreenHeight);


    {

        float srcWidth = mRenderTextureWidth;
        float srcHeight = mRenderTextureHeight;


        float dstWidth = mScreenWidth;
        float dstHeight = mScreenHeight;



        //NO TODO 동일한 인자를 drawGeometry에 넣어도 결과물이 다르고, 사용하고 있는 shader, fragment도 모두 동일하다.
        //   시작내용중에 마지막 다를 수 있는게 무엇일까??
//        LOGE("JIS : resetTransformMatricesFitin 1");

        cubeTransformMatrix =resetTransformMatricesFitin(
                srcWidth ,
                srcHeight ,
                mTextureRotate,
                dstWidth ,
                dstHeight    ,
                mLeftMargin,
                mTopMargin,
                mRightMargin,
                mBottomMargin  );




        // updateTextureSizeInScreen(mRenderTextureWidth, mRenderTextureHeight);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, m_renderedTexture);


        drawGeometryRendered(vboCubeVertexDataIndexReverse,
                             vboCubeVertexIndicesIndex,
                             6,//36,
                             cubeTransformMatrix  ,
                             mpRenderedShader
        );



    }


//    LOGE("isFront : %d mRenderingMode : %d", isFront, mRenderingMode);
//    LOGE("mAnimationUse : %d", mAnimationUse);
//    LOGE("margin : %f %f %f %f", mTopMargin, mBottomMargin, mLeftMargin,mRightMargin);

    /*************************************************
    // Render original (for Effect) to our screen
    // 가로로 애니메이션 하는 부분 ( preview화면 - 실시간, 영상, 사진 모두 )
    *******************************************************/
    if( mAnimationUse == 1)
    {
        long long current = current_timestamp();
        long long diff = current - mMAnimationTime ;

        float progress = (float)diff / 1200.0f;

//        LOGE("JIS : diff : %d", diff);

        if( diff  > 1200)
        {
            mAnimationUse = 0;
        } else{

            mpRenderedShader->prepareShaderProgram();
            mpRenderedShader->setPerspective(projectionMatrix);


            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mTextureId);

            {
                if( mRenderingMode == VIEW_IMAGE) {
                    cubeTransformMatrix=  resetTransformMatricesFitin(mTextureWidth, mTextureHeight, mTextureRotate, mScreenWidth, mScreenHeight,mLeftMargin, mTopMargin, mRightMargin, mBottomMargin);

                    //  pAnimationShader->prepareShaderProgram();
                    //  pAnimationShader->setPerspective(projectionMatrix);

                    drawAnimationGeometry(vboCubeVertexDataIndexReverse,
                                          vboCubeVertexIndicesIndex,
                                          6,//36,
                                          cubeTransformMatrix, mpAnimationShader, progress
                    );

                } else{

                    //실시간 보정과 동영상보정이 모두 이쪽으로 들어오게 되어 있음
//                    LOGE("SIZECHEK mRw=%f mRh=%f mTw=%d mTh=%d mSw=%f mSh=%f", mRenderTextureWidth,mRenderTextureHeight, mTextureWidth,mTextureHeight, mScreenWidth, mScreenHeight);
//                    LOGE("SIZECHEK ratio1=%f, 2=%f, 3=%f", mRenderTextureWidth/mRenderTextureHeight, mTextureWidth/(float)mTextureHeight, mScreenWidth/((mScreenHeight==0)?-1:mScreenHeight));

                    int orientation = videoOrientation;
                    if( mTopMargin ==  0  && mBottomMargin == 0)
                    {
                        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, mScreenWidth, mScreenHeight);
                        if(isFront == 1){
                            //실시간 보정에 한해서 isFront값이 쓰인다.
                            //실시간 보정에서는 항상 orientation이 0인데 셀피일 때에는 좌우반전이 필요하기 때문에 180으로 돌린다.
                            orientation = 180;
                            cubeTransformMatrix = glm::scale(cubeTransformMatrix, glm::vec3(-1.0f, 1.0f, 1.0f));
                        }
                    } else{
                        cubeTransformMatrix=  resetTransformMatricesFitin(
                                mTextureWidth,
                                mTextureHeight,
                                mTextureRotate,
                                mScreenWidth,
                                mScreenHeight,
                                mLeftMargin,
                                mTopMargin,
                                mRightMargin,
                                mBottomMargin
                        );

                        if(isFront == 1){
                            //실시간 보정에서는 항상 orientation이 0인데 셀피일 때에는 좌우반전이 필요하기 때문에 180으로 돌린다.
                            orientation = 180;
                            cubeTransformMatrix = glm::scale(cubeTransformMatrix, glm::vec3(-1.0f, 1.0f, 1.0f));
                        }
                    }


                    mpAnimationShaderUV->prepareShaderProgram();
                    mpAnimationShaderUV->setPerspective(projectionMatrix);

                    drawAnimationGeometry(vboCubeVertexDataIndexReverse,
                                          vboCubeVertexIndicesIndex,
                                          6,//36,
                                          cubeTransformMatrix, mpAnimationShaderUV, progress, orientation
                    );

                }

            }
        }

    }

    /***************************************
     *  Render MiniBuffer
     *  작게 표시하는 화면 예전에 사용하셨음 - 김윤태
     * ************************************/
     /*
    if( mDebugShowOriginMini == 1) {

        mpRenderedShader->prepareShaderProgram();
        mpRenderedShader->setPerspective(projectionMatrix);
        resetTransformMatrices3(MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT, MINITEXTURE_WIDTH,
                                MINITEXTURE_HEIGHT);


        updateTextureSizeInScreen(MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mMiniTextureId);


        drawGeometryRendered(vboCubeVertexDataIndex,
                             vboCubeVertexIndicesIndex,
                             6,//36,
                             cubeTransformMatrix,

                             mpRenderedShader
        );
    }*/


    /***************************************
     *  Render HISTORAM
     *  히스토그램 디버깅용으로 예전에 사용하셨음 - 김윤태
     * ************************************/
    /*
    if(mDebugShowHistogram == 1 ) {


        mpRenderedShader->prepareShaderProgram();
        mpRenderedShader->setPerspective(projectionMatrix);
        resetTransformMatrices4(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, HISTOGRAM_WIDTH,
                                HISTOGRAM_HEIGHT);


        updateTextureSizeInScreen(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mHistogramTextureId);


        drawGeometryRendered(vboCubeVertexDataIndex,
                             vboCubeVertexIndicesIndex,
                             6,//36,
                             cubeTransformMatrix,
                             mpRenderedShader
        );
    }*/
}


void GLES2Lesson::render_norendertexture() {
//    LOGI( "GLES2Lesson::function:render_norendertexture");

    ++mFrameCount;



    AquaShaderBase * aquaShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        aquaShaderBase = mpAquaShader;
    } else{

        //aquaShaderBase = mpAquaShaderUV;
        aquaShaderBase = mpAquaShaderUV_Effect;
    }


    RenderedShaderBase * miniShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        miniShaderBase = mpRenderedShader;
    } else{

        miniShaderBase = mpRenderedShaderUV;
    }






    if( mRenderingMode == VIEW_IMAGE) {

        //specify what we want for that texture
        //glActiveTexture(GL_TEXTURE0);
        // glBindTexture(GL_TEXTURE_2D, mTextureId);




        // if (dirted) {



        //upload the data
        ///     glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, dirtedWidth, dirtedHeight, 0, GL_RGBA,
        //                   GL_UNSIGNED_BYTE,
        //                textureData);


        //   textureWidth = dirtedWidth;
        //    textureHeight = dirtedHeight;


        //}



    } else{

    }






    if( FilterPreProcessor::bEnableFilter == 1  && FilterPreProcessor::inputValueScaleAll > 0.00001 ) {











        if( mFrameCount == 1 )//첫프레임은 다르게 처리 해야함
        {

            while(  FilterPreProcessor::inputFlag == 1 ) {
                usleep(10);
            }

            resultValue.clear();
        }


        if( FilterPreProcessor::inputFlag == 0)
        {
            //


            /******************************************
            //render to our MiniBuffer
            ***********************************************/

            glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferMiniTexture);
            glViewport(0, 0, MINITEXTURE_WIDTH,
                       MINITEXTURE_HEIGHT); // Render on the whole framebuffer, complete from the lower left corner to the upper right



            projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(MINITEXTURE_WIDTH),
                                          static_cast<GLfloat>( MINITEXTURE_HEIGHT), 0.0f, -1.0f, 1.0f);


            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mTextureId);

            clearBuffers();
            miniShaderBase->prepareShaderProgram();
            miniShaderBase->setPerspective(projectionMatrix);
            resetTransformMatrices2(mRenderTextureWidth, mRenderTextureHeight, MINITEXTURE_WIDTH,
                                    MINITEXTURE_HEIGHT);


            updateTextureSizeInScreen(mTextureWidth, mTextureWidth);


            if (mRenderingMode == VIEW_IMAGE) {


                drawGeometryRendered(vboCubeVertexDataIndex,
                                     vboCubeVertexIndicesIndex,
                                     6,//36,
                                     cubeTransformMatrix,
                                     mpRenderedShader
                );


            } else {


                drawGeometryRendered(vboCubeVertexDataIndex,
                                     vboCubeVertexIndicesIndex,
                                     6,//36,
                                     cubeTransformMatrix,
                                     mpRenderedShaderUV
                );


            }




            //  glBindTexture(GL_TEXTURE_2D, mMiniTextureId);


            glReadPixels(0, 0, MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, &FilterPreProcessor::preProcessorValue.minitexture[0][0]);

            FilterPreProcessor::inputFlag = 1;

        }


        if( mFrameCount == 1 )//첫프레임은 다르게 처리 해야함
        {

            while(  FilterPreProcessor::outputFlag == 0 ) {
                usleep(10);
            }
        }





        if( FilterPreProcessor::outputFlag == 1)
        {


            computeAverage(FilterPreProcessor::preProcessorValue);

            FilterPreProcessor::outputFlag = 0;


            if(mDebugShowHistogram == 1 ) {



                //specify what we want for that texture
                //glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, mHistogramTextureId);


                //upload the data
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT, 0, GL_RGBA,
                             GL_UNSIGNED_BYTE,
                             (char *) &PreProcessorValue::histogram[0][0]);

                glBindTexture(GL_TEXTURE_2D, 0);

            }





        }



    } else{


        averageResultValue = PreProcessorValue();

        resultValue.clear();



        FilterPreProcessor::outputFlag = 0;




    }





    float effectLimit_Position = 0.0f;
    /*************************************************
    // Render original (for Effect) to our screen
    *******************************************************/
    if( mAnimationUse == 1)
    {
        long long current = current_timestamp();
        long long diff = current - mMAnimationTime ;

        float progress = (float)diff / 1200.0f;

        if( diff  > 1200)
        {
            mAnimationUse = 0;
        } else{

            aquaShaderBase = mpAquaShaderUV_Effect;
            effectLimit_Position = progress * mScreenWidth;
        }

    }









    /*************************************************
    // Render to our screen
    *******************************************************/
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0,0, mScreenWidth, mScreenHeight); // Render on the whole framebuffer, complete from the lower left corner to the upper right



    projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(mScreenWidth),
                                  static_cast<GLfloat>( mScreenHeight), 0.0f, -1.0f, 1.0f);






    clearBuffers();
    aquaShaderBase->prepareShaderProgram();
    aquaShaderBase->setPerspective(projectionMatrix);
    // resetTransformMatrices(mRenderTextureWidth, mRenderTextureHeight, mRenderTextureWidth, mRenderTextureHeight);






    //GLES2Lesson::mTextureWidthInScreen = mRenderTextureWidth;
    //GLES2Lesson::mTextureHeightInScreen = mRenderTextureWidth;

    //updateTextureSizeInScreen(mRenderTextureWidth, mRenderTextureHeight);



    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId);




    if( mRenderingMode == VIEW_IMAGE) {

//        LOGE("JIS : resetTransformMatricesCropin 4");

        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, mScreenWidth, mScreenHeight );


        drawGeometry(vboCubeVertexDataIndex,
                     vboCubeVertexIndicesIndex,
                     6,//36,
                     cubeTransformMatrix,
                     mpAquaShader
        );




    } else{

//        LOGE("JIS : resetTransformMatricesCropin 5");

        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, mScreenWidth, mScreenHeight );

        if( mAnimationUse == 1) {


            glUniform1f(mpAquaShaderUV_Effect->effectLimit_Position, effectLimit_Position);//


            drawGeometry(vboCubeVertexDataIndex,
                         vboCubeVertexIndicesIndex,
                         6,//36,
                         cubeTransformMatrix,
                         mpAquaShaderUV_Effect
            );
        } else{

            glUniform1f(mpAquaShaderUV_Effect->effectLimit_Position, 999999);//
            drawGeometry(vboCubeVertexDataIndex,
                         vboCubeVertexIndicesIndex,
                         6,//36,
                         cubeTransformMatrix,
                         mpAquaShaderUV_Effect
            );
        }

    }



}




void GLES2Lesson::renderEncode(float dstWidth, float dstHeight) {



        // Render to the screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        // Render on the whole framebuffer, complete from the lower left corner to the upper right
        glViewport(0,0,dstWidth,dstHeight); // Render on the whole framebuffer, complete from the lower left corner to the upper right



        projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(dstWidth),
                                      static_cast<GLfloat>( dstHeight), 0.0f, -1.0f, 1.0f);


        // Use our shader
        clearBuffers();
        //glGetError()

        mpRenderedShader->prepareShaderProgram();
        mpRenderedShader->setPerspective(projectionMatrix);


//    LOGE("JIS : resetTransformMatricesFitin 4");
        float srcWidth = mRenderTextureWidth;
        float srcHeight = mRenderTextureHeight;
    cubeTransformMatrix = resetTransformMatricesFitin(
            srcWidth,
            srcHeight,
            mTextureRotate,
            dstWidth,
            dstHeight,
            0,
            0,
            0,
            0);


        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, m_renderedTexture);


        drawGeometryRendered(vboCubeVertexDataIndexReverse,
                               vboCubeVertexIndicesIndex,
                               6,//36,
                               cubeTransformMatrix,
                               mpRenderedShader

        );

}




void GLES2Lesson::renderEncodenorendertexture(float dstWidth, float dstHeight) {



    AquaShaderBase * aquaShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        aquaShaderBase = mpAquaShader;
    } else{

        aquaShaderBase = mpAquaShaderUV;
    }


    RenderedShaderBase * miniShaderBase = 0;


    if( mRenderingMode == VIEW_IMAGE) {

        miniShaderBase = mpRenderedShader;
    } else{

        miniShaderBase = mpRenderedShaderUV;
    }












    /*************************************************
    // Render to our screen
    *******************************************************/
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0,0, dstWidth, dstHeight); // Render on the whole framebuffer, complete from the lower left corner to the upper right



    projectionMatrix = glm::ortho(0.0f, static_cast<GLfloat>(dstWidth),
                                  static_cast<GLfloat>( dstHeight), 0.0f, -1.0f, 1.0f);






    clearBuffers();
    aquaShaderBase->prepareShaderProgram();
    aquaShaderBase->setPerspective(projectionMatrix);
    // resetTransformMatrices(mRenderTextureWidth, mRenderTextureHeight, mRenderTextureWidth, mRenderTextureHeight);






    //GLES2Lesson::mTextureWidthInScreen = mRenderTextureWidth;
    //GLES2Lesson::mTextureHeightInScreen = mRenderTextureWidth;

    //updateTextureSizeInScreen(mRenderTextureWidth, mRenderTextureHeight);



    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId);




    if( mRenderingMode == VIEW_IMAGE) {

//        LOGE("JIS : resetTransformMatricesCropin 6");

        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, dstWidth, dstHeight );


        drawGeometry(vboCubeVertexDataIndex,
                     vboCubeVertexIndicesIndex,
                     6,//36,
                     cubeTransformMatrix,
                     mpAquaShader
        );




    } else{

//        LOGE("JIS : resetTransformMatricesCropin 7");

        cubeTransformMatrix=  resetTransformMatricesCropin(mTextureWidth, mTextureHeight, dstWidth, dstHeight );

        drawGeometry(vboCubeVertexDataIndex,
                     vboCubeVertexIndicesIndex,
                     6,//36,
                     cubeTransformMatrix,
                     mpAquaShaderUV
        );

    }


}










