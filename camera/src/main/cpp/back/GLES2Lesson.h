//
// Created by monty on 23/11/15.
//

#ifndef LESSON02_GLES2LESSON_H
#define LESSON02_GLES2LESSON_H

#include "AquaShader.h"

class GLES2Lesson {

    void fetchShaderLocations();
    void setPerspective();
    //void prepareShaderProgram();
    void clearBuffers();
    void resetTransformMatrices();
    void printVerboseDriverInformation();
    void createVBOs();
    void deleteVBOs();
    void drawGeometry( const int vertexVbo, const int indexVbo, int vertexCount, const glm::mat4& transform );

    GLuint createProgram(const char *pVertexSource, const char *pFragmentSource);
    GLuint loadShader(GLenum shaderType, const char *pSource);

    const static float cubeVertices[ 16 * 5 ];
    const static unsigned short cubeIndices[ 6 * 6 ];

    glm::mat4 cubeTransformMatrix;
    glm::mat4 projectionMatrix;






public:

    /*----------------------------------
    * Screen Setting
    ----------------------------------*/

    static float mScreenWidth ;
    static float mScreenHeight ;

    static float mTextureWidthInScreen ;
    static float mTextureHeightInScreen ;


    static void updateTextureSizeInScreen(int width , int height)
    {

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


    }


    /*----------------------------------
     * Shader Setting
     --------------------------------------*/





    AquaShader * auaShader = 0;


    /*----------------------------------
      * Texture Setting
    ----------------------------------*/
    static GLuint textureId;

    static bool dirted ;
    static char * dirtedTextureData;
    static int dirtedWidth ;
    static int dirtedHeight;


    static void updateTextureDate( int w , int h, char * data);






    //VBO stuff
    GLuint vboCubeVertexDataIndex;
    GLuint vboCubeVertexIndicesIndex;

    int *textureData;
    int textureWidth;
    int textureHeight;


public:
    GLES2Lesson();
    ~GLES2Lesson();
    bool init( float w, float h, const std::string& vertexShader, const std::string& fragmentShader );
    void setTexture( int *bitmapData, int width, int height, int format );
    void render();
    void shutdown();
    void tick();


  //  static float triangleVertices[ 9 ];
  //  static float squareVertices[ 18 ];
  //  static glm::mat4 triangleTransformMatrix;
   // static glm::mat4 squareTransformMatrix;


};

#endif //LESSON02_GLES2LESSON_H
