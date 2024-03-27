//
// Created by kyt77 on 2019-06-19.
//

#ifndef ANDROID_SAMPLE_RENDERED_AQUASHADERUV_H
#define ANDROID_SAMPLE_RENDERED_AQUASHADERUV_H


#include <GLES3/gl3.h>
#include <string>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "AquaShader.h"
#include "RenderedShader.h"


class RenderedShaderUV : public RenderedShaderBase {

public:

public :
    static std::string strVertexShader;
    static std::string strPixelShader;


    GLuint gProgram;

    //for shader
    GLint vertexAttributePosition;
    GLint modelMatrixAttributePosition;
    GLint samplerUniformPosition;
    GLint textureCoordinatesAttributePosition;
    GLint projectionMatrixAttributePosition;




    GLint startR_Position = 0;
    GLint stopR_Position = 0;

    GLint startG_Position = 0;
    GLint stopG_Position = 0;

    GLint startB_Position = 0;
    GLint stopB_Position = 0;

    GLint channelMix_Position = 0;

    GLint scalarBF_Position= 0;
    GLint scalarGF_Position= 0;


    GLint mixRfromR_Position = 0;
    GLint mixRfromG_Position = 0;
    GLint mixRfromB_Position = 0;

    GLint mixBfromR_Position = 0;
    GLint mixBfromG_Position = 0;
    GLint mixBfromB_Position = 0;



    GLint rofg_Position= 0;//glGetUniformLocation(gProgram, "rofg");
    GLint rofb_Position= 0;//glGetUniformLocation(gProgram, "rofb");


    /*--------------------------------------
     * Shader를 생성 하고, 변수 위치를 구해 놓는다.
     ----------------------------------------*/
    void initShader();

    GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) ;

    GLuint loadShader(GLenum shaderType, const char *pSource) ;

    void finiShader();


    RenderedShaderUV()
    {
        initShader();
    }

    virtual ~RenderedShaderUV()
    {
        finiShader();
    }


    /*******************************
     * 렌더링시 사용하는 것
     *******************************/
    void prepareShaderProgram();
    void setPerspective( glm::mat4 &projectionMatrix);






};


#endif //ANDROID_SAMPLE_RENDERED_AQUASHADERUV_H
