//
// Created by monty on 23/11/15.
//

#ifndef LESSON02_GLES2LESSON_H
#define LESSON02_GLES2LESSON_H



#include <GLES3/gl3ext.h>


#include "FilterPreProcessor.h"
#include "AquaShader.h"
#include "RenderedShader.h"
#include "AnimationShader.h"

#include <android/log.h>
#include <map>
#include <vector>




class GLES2Lesson {

    void fetchShaderLocations();
    //void setPerspective();
    //void prepareShaderProgram();
    void clearBuffers();
    void resetTransformMatrices(int srctWidth, int srcHeight , int dstWidth, int dstHeight );
    void resetTransformMatrices2(float srctWidth, float srcHeight , float dstWidth, float dstHeight );
    void resetTransformMatrices3(float srctWidth, float srcHeight , float dstWidth, float dstHeight );
    void resetTransformMatrices4(float srctWidth, float srcHeight , float dstWidth, float dstHeight );

    void printVerboseDriverInformation();
    void createVBOs();
    void deleteVBOs();
    void deleteTextures();
    void drawGeometry( const int vertexVbo, const int indexVbo, int vertexCount, const glm::mat4& transform , AquaShader * pAquaShader);
    void drawGeometryRendered( const int vertexVbo, const int indexVbo, int vertexCount, const glm::mat4& transform , RenderedShader * pRenderShader);
    void drawAnimationGeometry(const int vertexVbo, const int indexVbo, int vertexCount,
                                            const glm::mat4 &transform, AnimationShader * animationShader, float progress) ;
    void drawAnimationGeometry(const int vertexVbo, const int indexVbo, int vertexCount,
                               const glm::mat4 &transform, AnimationShader * animationShader, float progress, int orientation) ;


    GLuint createProgram(const char *pVertexSource, const char *pFragmentSource);
    GLuint loadShader(GLenum shaderType, const char *pSource);

    const static float cubeVertices[ 16 * 5 ];
    const static float cubeVerticesReverse[ 16 * 5 ];
    const static unsigned short cubeIndices[ 6 * 6 ];

    glm::mat4 cubeTransformMatrix;
    glm::mat4 projectionMatrix;

    //for shader
    //GLint vertexAttributePosition;
    //GLint modelMatrixAttributePosition;
    //GLint samplerUniformPosition;
    //GLint textureCoordinatesAttributePosition;
    //GLint projectionMatrixAttributePosition;

public:

    class TextureInfo{
    public:
        int width = 0;
        int height = 0;
        std::vector<char> buf;
    };
    static std::map<std::string, TextureInfo > m_UIImages;
    static std::map<std::string, GLuint > m_UIImagesTexture;


public:

    /*----------------------------------
    * Screen Setting
    ----------------------------------*/

    static float mScreenWidth ;
    static float mScreenHeight ;




    static float mRenderTextureWidth ;
    static float mRenderTextureHeight ;


    //static float mTextureWidth ;
    //static float mTextureHeight ;


    static int *textureData;
    static int mTextureWidth;
    static int mTextureHeight;
    static int mTextureRotate;



    int  mFrameCount =0;


    static void updateTextureSizeInScreen(int width , int height)
    {


        /*

        float rateW = mScreenWidth / float(width);
        float rateH = mScreenHeight / float(height);

        if( rateW > rateH)
        {
            GLES2Lesson::mTextureWidthInScreen = rateH * width;
            GLES2Lesson::mTextureHeightInScreen = rateH * height;

        } else{
            GLES2Lesson::mTextureWidthInScreen = rateW * width;
            GLES2Lesson::mTextureHeightInScreen = rateW * height;
        }

         */

    }


    /*----------------------------------
     * Shader Setting
     --------------------------------------*/
    //GLint fChannelMixFromGreenPosition;
    //GLint fChannelMixFromBluePosition;
    //GLint fReduceBluePosition;

    static float valueRfromG ;
    static float valueRfromB;
    static float valueBreduceB;
    static void setValueRfromG( float value );
    static void setValueRfromB( float value );
    static void setValueBreduceB( float value );



  //  GLint startR_Position;
 //   GLint stopR_Position;

 //   GLint startG_Position;
 //   GLint stopG_Position;

 //   GLint startB_Position;
 //   GLint stopB_Position;

  //  GLint channelMix_Position;


 //   GLint scalarBF_Position;
 //   GLint scalarGF_Position;

  //  GLint rofg_Position;
  //  GLint rofb_Position;

    static std::vector<PreProcessorValue> resultValue;
    static PreProcessorValue averageResultValue;



    /*----------------------------------
      * RenderTexture Setting
    ----------------------------------*/
    static GLuint m_FramebufferName;
    static GLuint m_renderedTexture;




    static void initRenderTextue(int textureWidth, int textureHeight)
    {

        if( m_FramebufferName > 0)
        {
            // Set "renderedTexture" as our colour attachement #0
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);


            glDeleteFramebuffers(1, &m_FramebufferName);
            m_FramebufferName =0;
        }


        if( m_renderedTexture > 0)
        {
            glDeleteTextures(1, &m_renderedTexture);
            m_renderedTexture =0;
        }



        GLES2Lesson::mRenderTextureWidth = textureWidth;
        GLES2Lesson::mRenderTextureHeight= textureHeight ;


        GLuint FramebufferName;

        glGenFramebuffers(1, &FramebufferName);
        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName);


        GLuint renderedTexture;

/*
        if( mRenderingMode == VIEW_MOVIE)
        {

            GLuint textureId = 0;

            //Generate texture storage
            glGenTextures(1, &renderedTexture);

            //specify what we want for that texture

            glActiveTexture(renderedTexture);

            glBindTexture(GL_TEXTURE_EXTERNAL_OES, renderedTexture);

            //upload the data
            // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData );

            // Set the filtering mode - surprisingly, this is needed.
            glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_NEAREST);


            // Set "renderedTexture" as our colour attachement #0
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_EXTERNAL_OES, renderedTexture, 0);

            //https://stackoverflow.com/questions/21747600/texture-for-yuv420-to-rgb-conversion-in-opengl-es/21763014#21763014

            //https://github.com/google/angle/blob/master/src/tests/gl_tests/ImageTest.cpp


            glBindFramebuffer(GL_FRAMEBUFFER, 0);


        } else{

        */
            glGenTextures(1, &renderedTexture);

            // "Bind" the newly created texture : all future texture functions will modify this texture
            glBindTexture(GL_TEXTURE_2D, renderedTexture);



            // Give an empty image to OpenGL ( the last "0" means "empty" )
            glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, textureWidth, textureHeight, 0,GL_RGB, GL_UNSIGNED_BYTE, 0);


            // Poor filtering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);//GL_NEAREST
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);//GL_NEAREST
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);


            // Set "renderedTexture" as our colour attachement #0
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderedTexture, 0);



            glBindFramebuffer(GL_FRAMEBUFFER, 0);



/*
        }


*/



        m_FramebufferName =  FramebufferName;
        m_renderedTexture = renderedTexture;



    }




    static GLuint mMiniTextureId;
    static GLuint mFrameBufferMiniTexture;
    static GLuint mHistogramTextureId;

    static void initMiniTextue(int textureWidth, int textureHeight)
    {



        if( mFrameBufferMiniTexture > 0)
        {
            // Set "renderedTexture" as our colour attachement #0
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);


            glDeleteFramebuffers(1, &mFrameBufferMiniTexture);
            mFrameBufferMiniTexture =0;
        }


        if( mMiniTextureId > 0)
        {
            glDeleteTextures(1, &mMiniTextureId);
            mMiniTextureId =0;
        }



        GLuint FramebufferName;

        glGenFramebuffers(1, &FramebufferName);
        glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName);


        GLuint renderedTexture;


        glGenTextures(1, &renderedTexture);

        // "Bind" the newly created texture : all future texture functions will modify this texture
        glBindTexture(GL_TEXTURE_2D, renderedTexture);



        // Give an empty image to OpenGL ( the last "0" means "empty" )
        glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, textureWidth, textureHeight, 0,GL_RGB, GL_UNSIGNED_BYTE, 0);


        // Poor filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);


        // Set "renderedTexture" as our colour attachement #0
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderedTexture, 0);



        glBindFramebuffer(GL_FRAMEBUFFER, 0);



/*
        }


*/



        mFrameBufferMiniTexture =  FramebufferName;
        mMiniTextureId = renderedTexture;


    }







    static void initHistogramTextue(int textureWidth, int textureHeight)
    {



        if( mHistogramTextureId > 0)
        {
            glDeleteTextures(1, &mHistogramTextureId);
            mHistogramTextureId =0;
        }




        GLuint generatedTexture;



        glGenTextures(1, &generatedTexture);

        // "Bind" the newly created texture : all future texture functions will modify this texture
        glBindTexture(GL_TEXTURE_2D, generatedTexture);



        // Give an empty image to OpenGL ( the last "0" means "empty" )
        glTexImage2D(GL_TEXTURE_2D, 0,GL_RGBA, textureWidth, textureHeight, 0,GL_RGBA, GL_UNSIGNED_BYTE, 0);


        // Poor filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);



        glBindTexture(GL_TEXTURE_2D, 0);




        mHistogramTextureId = generatedTexture;
    }










    /*----------------------------------
      * Texture Setting
    ----------------------------------*/
    static GLuint mTextureId;




    //static bool dirted ;
    //static char * dirtedTextureData;
    //static int dirtedWidth ;
    //static int dirtedHeight;


    static void updateTextureDate( int w , int h, char * data);



    AquaShader * mpAquaShader = 0;
    AquaShader * mpAquaShaderUV = 0;
    AquaShader * mpAquaShaderUV_Effect = 0;
    RenderedShader * mpRenderedShader = 0;
    RenderedShader * mpRenderedShaderUV = 0;
    AnimationShader * mpAnimationShader = 0;
    AnimationShader * mpAnimationShaderUV = 0;

    //GLuint gProgram;



    //VBO stuff
    GLuint vboCubeVertexDataIndex = 0;
    GLuint vboCubeVertexDataIndexReverse= 0;
    GLuint vboCubeVertexIndicesIndex = 0;


    float cubeRotationAngleYZ;
    float cubeRotationAngleXZ;
public:
    GLES2Lesson();
    ~GLES2Lesson();
    bool init( float w, float h, int textureWidth, int textureHeight );
    //void setTexture( int *bitmapData, int width, int height, int format );
    void render();
    void render1(int isFront);
    void render2(int isFront);
    void render_norendertexture();
    void renderEncode(float dstWidth, float dstHeight);
    void renderEncodenorendertexture(float dstWidth, float dstHeight);
    void shutdown();


    void computeAverage(PreProcessorValue preProcessorValue);

   enum {
       VIEW_IMAGE = 0,
       VIEW_MOVIE = 1,
       ENCODE_MOVIE=2,
       VIEW_REALTIME= 3,
       ENCODE_REALTIME=4,
   };

   static int mRenderingMode ;


    void changeRenderingMode(int renderingMode , int textureWidth, int textureHeight, int textureRotate, int renderTextureWidth, int renderTextureHeight);
    static  GLuint getMainTextureId();



    static int mDebugShowOriginMini;
    static int mDebugShowHistogram;



    static float msClearColorR;
    static float msClearColorG;
    static float msClearColorB;


    static float mBottomMargin;
    static float mTopMargin;
    static float mLeftMargin;
    static float mRightMargin;


    static int              mAnimationUse;
    static long long        mMAnimationTime;
    static int videoOrientation ;






};


#include <sys/time.h>

static long long current_timestamp() {
    struct timeval te;
    gettimeofday(&te, NULL); // get current time
    long long milliseconds = te.tv_sec*1000LL + te.tv_usec/1000; // calculate milliseconds
    // printf("milliseconds: %lld\n", milliseconds);
    return milliseconds;
}


#endif //LESSON02_GLES2LESSON_H
