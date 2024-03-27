//
// Created by kyt77 on 2019-06-19.
//

#ifndef ANDROID_SAMPLE_AQUASHADER_H
#define ANDROID_SAMPLE_AQUASHADER_H


#include <GLES2/gl2.h>
#include <string>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"

class AquaShaderBase
{

public:
    /*******************************
     * 렌더링시 사용하는 것
     *******************************/
    virtual void prepareShaderProgram()= 0;
    virtual void setPerspective( glm::mat4 &projectionMatrix) =0;






};
class AquaShader : public AquaShaderBase{

public:

public :
    static std::string strVertexShader;
    static std::string strPixelShader;
    static std::string strPixelShaderUV;
    static std::string strPixelShaderUV_Effect;




    GLuint gProgram = -1;

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



    GLint mixRfromR_Position = 0;//ñglGetUniformLocation(gProgram, "mixRfromR");// resultValue.mixRfromR );
    GLint mixRfromG_Position = 0;//glGetUniformLocation(gProgram, "mixRfromG");// resultValue.mixRfromG );
    GLint mixRfromB_Position = 0;//glGetUniformLocation(gProgram, "mixRfromB");// resultValue.mixRfromB );


    GLint mixBfromR_Position = 0;//glGetUniformLocation(gProgram, "mixBfromR");// resultValue.mixBfromR );
    GLint mixBfromG_Position = 0;//glGetUniformLocation(gProgram, "mixBfromG");// resultValue.mixBfromG );
    GLint mixBfromB_Position = 0;//glGetUniformLocation(gProgram, "mixBfromB");// resultValue.mixBfromB );




    GLint rofg_Position= 0;//glGetUniformLocation(gProgram, "rofg");
    GLint rofb_Position= 0;//glGetUniformLocation(gProgram, "rofb");


    GLint effectLimit_Position= 0;//glGetUniformLocation(gProgram, "rofb");



    /*--------------------------------------
     * Shader를 생성 하고, 변수 위치를 구해 놓는다.
     ----------------------------------------*/
    void initShader(int bUV);

    GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) ;

    GLuint loadShader(GLenum shaderType, const char *pSource) ;

    void finiShader();


    AquaShader(int bUV)
    {
        initShader(bUV);
    }

    virtual ~AquaShader()
    {
        finiShader();
    }


    /*******************************
     * 렌더링시 사용하는 것
     *******************************/
    void prepareShaderProgram();
    void setPerspective( glm::mat4 &projectionMatrix);






};


#endif //ANDROID_SAMPLE_AQUASHADER_H
