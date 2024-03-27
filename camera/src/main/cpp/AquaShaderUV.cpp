//
// Created by kyt77 on 2019-06-19.
//

#include "AquaShaderUV.h"

#include <GLES2/gl2.h>

#include "glm/glm.hpp"
#include "glm/gtc/matrix_transform.hpp"

#include <string>

#include <android/log.h>

#include "GLES2Lesson.h"
#include "NdkGlue.h"


std::string AquaShaderUV::strVertexShader;
std::string AquaShaderUV::strPixelShader;


void AquaShaderUV::initShader()
{

    gProgram = createProgram(strVertexShader.c_str(), strPixelShader.c_str());



    vertexAttributePosition = glGetAttribLocation(gProgram, "aPosition");
    modelMatrixAttributePosition = glGetUniformLocation(gProgram, "uModel");
    projectionMatrixAttributePosition = glGetUniformLocation(gProgram, "uProjection");
    samplerUniformPosition = glGetUniformLocation(gProgram, "sTexture");
    textureCoordinatesAttributePosition = glGetAttribLocation(gProgram, "aTexCoord");




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


    mixRfromR_Position = glGetUniformLocation(gProgram, "mixRfromR");// resultValue.mixRfromR );
    mixRfromG_Position = glGetUniformLocation(gProgram, "mixRfromG");// resultValue.mixRfromG );
    mixRfromB_Position = glGetUniformLocation(gProgram, "mixRfromB");// resultValue.mixRfromB );


    mixBfromR_Position = glGetUniformLocation(gProgram, "mixBfromR");// resultValue.mixBfromR );
    mixBfromG_Position = glGetUniformLocation(gProgram, "mixBfromG");// resultValue.mixBfromG );
    mixBfromB_Position = glGetUniformLocation(gProgram, "mixBfromB");// resultValue.mixBfromB );


}

void AquaShaderUV::finiShader()
{






}



GLuint AquaShaderUV::loadShader(GLenum shaderType, const char *pSource) {
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
                    LOGE("Could not compile shader %d:\n%s\n", shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint AquaShaderUV::createProgram(const char *pVertexSource, const char *pFragmentSource) {
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
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}






void AquaShaderUV::prepareShaderProgram() {
    glUseProgram(gProgram);
//    checkGlError("glUseProgram");
}

void AquaShaderUV::setPerspective( glm::mat4 &projectionMatrix) {
    glUniformMatrix4fv(projectionMatrixAttributePosition, 1, false, &projectionMatrix[0][0]);
}





