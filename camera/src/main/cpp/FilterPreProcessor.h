//
// Created by monty on 23/11/15.
//

#ifndef FILTER_PREPROCESSOR_H
#define FILTER_PREPROCESSOR_H


#define MINITEXTURE_WIDTH 512
#define MINITEXTURE_HEIGHT 512

#define HISTOGRAM_WIDTH 256
#define HISTOGRAM_HEIGHT 256

//2022.03.14
#define KETI_DEBUG 0
#define KETI_COL_COR_VER 1
#define keti_clamp(X, LOW, HIGH) (X < LOW ? LOW : (X > HIGH ? HIGH : X))

class PreProcessorValue
{
public:
  long  computedTime = 0;
public:
    float startR = 0;
    float startG = 0;
    float startB = 0;

    float endR = 1.0f;
    float endG = 1.0f;
    float endB = 1.0f;


    float mixRfromB = 0.0f;
    float mixRfromG = 0.0f;
    float mixRfromR = 1.0f;

    float mixBfromB = 1.0f;
    float mixBfromG = 0.0f;
    float mixBfromR = 0.0f;



    static int minitexture[MINITEXTURE_WIDTH][MINITEXTURE_HEIGHT];

    static int histogram[HISTOGRAM_WIDTH][HISTOGRAM_HEIGHT];



    PreProcessorValue()
    {


    }

};

class FilterPreProcessor
{

public:
    static void run();

    static int inputFlag;
    static int outputFlag;

    /*****************************************
               CPU                       CPU                          CPU
    in   1   ~~~~~~ 0          ~~~~~~~ 1 ~~~~~~ 0          ~~~~~~~ 1 ~~~~~~ 0
    out  0   ~~~~~~ 1 ~~~~~~ 0           ~~~~~~ 1 ~~~~~~ 0           ~~~~~~ 1
                       GPU        GPU               GPU       GPU

*************************************************/
    static int checkRunThread();
   // static double getNormalPercent(double colorOfPix);



    static bool process(unsigned char * data, int w, int h, PreProcessorValue &resultValue);



    static void processThread(unsigned char * data, int w, int h, PreProcessorValue &resultValue);


    static PreProcessorValue preProcessorValue;





    static double inputValueScaleAll;


    static int      bEnableFilter ;

};

#endif //FILTER_PREPROCESSOR_H
