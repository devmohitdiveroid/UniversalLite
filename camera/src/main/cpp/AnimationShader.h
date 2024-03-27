//
// Created by kyt77 on 2019-06-19.
//

#ifndef ANDROID_SAMPLE_RENDERED_ANIMATIONSHADER_H
#define ANDROID_SAMPLE_RENDERED_ANIMATIONSHADER_H


#include <GLES3/gl3.h>
#include <string>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"




class AnimationShader {

public:

public :
    static std::string strVertexShader;
    static std::string strPixelShader;

    static std::string strPixelShaderUV;

    GLuint gProgram = -1;

    //for shader
    GLint vertexAttributePosition;
    GLint modelMatrixAttributePosition;
    GLint samplerUniformPosition;
    GLint textureCoordinatesAttributePosition;
    GLint projectionMatrixAttributePosition;




    GLint animationProgressPosition ;
    GLint orientationPosition ;


    /*--------------------------------------
     * Shader를 생성 하고, 변수 위치를 구해 놓는다.
     ----------------------------------------*/
    void initShader(bool bUV);

    GLuint createProgram(const char *pVertexSource, const char *pFragmentSource) ;

    GLuint loadShader(GLenum shaderType, const char *pSource) ;

    void finiShader();


    AnimationShader(bool bUV)
    {
        initShader(bUV);
    }

    virtual ~AnimationShader()
    {
        finiShader();
    }


    /*******************************
     * 렌더링시 사용하는 것
     *******************************/
    void prepareShaderProgram();
    void setPerspective( glm::mat4 &projectionMatrix);






};


#endif //ANDROID_SAMPLE_RENDERED_ANIMATIONSHADER_H
