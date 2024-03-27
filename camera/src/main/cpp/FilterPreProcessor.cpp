


#include "FilterPreProcessor.h"
#include "GLES2Lesson.h"


#include <pthread.h>
#include <cmath>


int PreProcessorValue::histogram[HISTOGRAM_WIDTH][HISTOGRAM_HEIGHT];



double FilterPreProcessor::inputValueScaleAll = 1.0;

int      FilterPreProcessor::bEnableFilter  = 0;

#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <pthread.h>

#include <unistd.h>

// Thread id
static pthread_t threadId;

void * threadFunc(void * arg)
{
    std::cout << "Thread Function :: Start" << std::endl;
//    std::cout << "Thread Function :: End" << std::endl;
    // Return value from thread


    while(true)
    {
        //pthread_t thId = pthread_self();

        //if( threadId != thId)
        //{

         //   std::cout << "Thread Function :: End ExitThread" << std::endl;

         //   break;
        //}


        //if( FilterPreProcessor::inputFlag == 0 && FilterPreProcessor::outputFlag ==1 )
       // {

        //    FilterPreProcessor::process((unsigned char *) &FilterPreProcessor::preProcessorValue.minitexture[0][0], MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT,FilterPreProcessor::preProcessorValue);

        //}

        usleep(1000*10);


    }
    return NULL;
}


PreProcessorValue FilterPreProcessor::preProcessorValue;


int PreProcessorValue::minitexture[MINITEXTURE_WIDTH][MINITEXTURE_HEIGHT] ={ {0,}, };


void FilterPreProcessor::run()
{

    if( FilterPreProcessor::inputFlag == 1 && FilterPreProcessor::outputFlag ==0 )
    {

       bool result =  FilterPreProcessor::process((unsigned char *) &FilterPreProcessor::preProcessorValue.minitexture[0][0], MINITEXTURE_WIDTH, MINITEXTURE_HEIGHT,FilterPreProcessor::preProcessorValue);

       if( result == false)
       {
           FilterPreProcessor::inputFlag = 0;
           return;
       }

         preProcessorValue.computedTime = current_timestamp();

        FilterPreProcessor::inputFlag = 0;
        FilterPreProcessor::outputFlag =1;

    }

}








/***************************************************************************************************
 * *************************************************************************************************

        아래가 실제로 필터를 구현한 내용임

 ***************************************************************************************************
 ***************************************************************************************************/




static double getNormalPercent(double colorOfPix){
    return (5+0.000000003*(colorOfPix+100) *( colorOfPix - 255)*( colorOfPix - 255)*( colorOfPix - 255))/200;
}




int FilterPreProcessor::checkRunThread(){




// Create a thread that will funtion threadFunc()
    int err = pthread_create(&threadId, NULL, &threadFunc, NULL);
    // Check if thread is created sucessfuly
    if (err)
    {
        std::cout << "Thread creation failed : " << strerror(err);
        return err;
    }
    else
        std::cout << "Thread Created with ID : " << threadId << std::endl;
    // Do some stuff

   // err = pthread_detach(threadId);
    //if (err)
     //   std::cout << "Failed to detach Thread : " << strerror(err) << std::endl;

    // Sleep for 2 seconds because if main function exits, then other threads will
    // be also be killed. Sleep for 2 seconds, so that detached exits by then
   // sleep(2);





}




int FilterPreProcessor::inputFlag = 0;
int FilterPreProcessor::outputFlag = 0;




#define NORMALISE_WIDTH_PERCENT  1
#define NORMALISE_HEIGHT          2

#define NORMALISE_ALGORITHM NORMALISE_HEIGHT




bool FilterPreProcessor::process(unsigned char * data, int w, int h, PreProcessorValue &resultValue)
{

    if ( bEnableFilter == 0)
    {
        resultValue = PreProcessorValue();

        return false;
    }


    if( inputValueScaleAll <= 0.00001)
    {

        resultValue = PreProcessorValue();

        return false;
    }
    ///GL_TEXTURE_EXTERNAL_OES


//RGBA

    double r1 = 0;
    double g1 = 0;
    double b1 = 0;

    int sumR = 0.0f;
    int sumG = 0.0f;
    int sumB = 0.0f;

    //int totalSum =0;

    int histoR[256] = {0};
    int histoG[256] = {0};
    int histoB[256] = {0};


    /********************************************
     ************  create Histogram *************
     *******************************************/


    for( int iw = 0 ; iw < w; ++iw )
    {
        for( int ih = 0 ; ih < h; ++ih ) {

            int index = 4 * (w * ih + iw);
            int indexR = index + 0;
            int indexG = index + 1;
            int indexB = index + 2;

            int R = data[index + 0];
            int G = data[index + 1];
            int B = data[index + 2];


            ++histoR[R];// = {0};
            ++histoG[G];// = {0};
            ++histoB[B];// = {0};


            ++sumR;
            ++sumG;
            ++sumB;


            r1 += R;
            g1 += G;
            b1 += B;
        }
    }


    double rofb =  r1/b1;
    double rofg =  r1/g1;
    double bofg =  b1/g1;

    double b1Ofpix = b1/(MINITEXTURE_WIDTH * MINITEXTURE_HEIGHT);
    double g1Ofpix = g1/(MINITEXTURE_WIDTH * MINITEXTURE_HEIGHT);
    double r1Ofpix = r1/(MINITEXTURE_WIDTH * MINITEXTURE_HEIGHT);

    double channelNormalPercentR = getNormalPercent(r1Ofpix);
    double channelNormalPercentG = getNormalPercent(g1Ofpix);
    double channelNormalPercentB = getNormalPercent(b1Ofpix);


    bool  isNeedMulMix = (r1/b1 < 0.75 || r1/g1 < 0.85);


    /********************************************
    *********  Compute Nomalize Point **********
    ********************************************/
    int minIndexSumR= 0;
    int minIndexSumG= 0;
    int minIndexSumB= 0;

    int maxIndexSumR= 255;
    int maxIndexSumG= 255;
    int maxIndexSumB= 255;








#if (NORMALISE_ALGORITHM == NORMALISE_WIDTH_PERCENT)

    int currentSumR= 0;
    int currentSumG= 0;
    int currentSumB= 0;

    int minPointSumR = 0.5 * sumR / 100;
    int minPointSumG = 0.5 * sumG / 100;
    int minPointSumB = 0.5 * sumB / 100;


    int maxPointSumR = 99.5 * sumR / 100;
    int maxPointSumG = 99.5 * sumG / 100;
    int maxPointSumB = 99.5 * sumB / 100;

    for( int i = 0; i < 256; ++i)
    {

        if( minIndexSumR == 0 )
        {
            if( currentSumR >= minPointSumR)
            {
                minIndexSumR = i;
            }
        }

        if( minIndexSumG == 0 )
        {
            if( currentSumG >= minPointSumG)
            {
                minIndexSumG = i;
            }
        }


        if( minIndexSumB == 0 )
        {
            if( currentSumB >= minPointSumB)
            {
                minIndexSumB = i;
            }
        }



        if( maxIndexSumR ==255 )
        {
            if( currentSumR >= maxPointSumR)
            {
                maxIndexSumR = i;
            }
        }

        if( maxIndexSumG ==255)
        {
            if( currentSumG >= maxPointSumG)
            {
                maxIndexSumG = i;
            }
        }


        if( maxIndexSumB ==255 )
        {
            if( currentSumB >= maxPointSumB)
            {
                maxIndexSumB = i;
            }
        }

        currentSumR += histoR[i];
        currentSumG += histoG[i];
        currentSumB += histoB[i];
    }


    if( minIndexSumR > 100)
    {
        minIndexSumR = 100;
    }


    if( minIndexSumG > 100)
    {
        minIndexSumG = 100;
    }



    if( minIndexSumB > 100)
    {
        minIndexSumB = 100;
    }


    if( maxIndexSumR < 140)
    {
        maxIndexSumR = 140;
    }



    if( maxIndexSumG < 200)
    {
        maxIndexSumG = 200;
    }




    if( maxIndexSumB < 200)
    {
        maxIndexSumB = 200;
    }

#elif (NORMALISE_ALGORITHM == NORMALISE_HEIGHT)
    /*
      히스토그램을 보면 처음 빛이 찍힌범위가 dark부분과 bright부분에 나타난다.
      normalization 범위를 지정하는데에는 여러가지 요소가 고려되야 한다.
      1. normalization 범위가 255/2 이상은 되야지 색의 끊김이 생기지 않는다.
      2. 빛이 전체범위에 많이 분포할 때에는 dark,bright가 처음 찍힌부분보다 조금 안쪽으로 normalization하는 것이 대비?효과를 줄 수 있다.
         이때, 가장 픽셀수가 많은 곳보다 channelNormalPercent 정도되는 부분까지 잘라내면 괜찮은 사진이 찍힌다.( 최대 분포기준으로 짜름 )
      3. 히스토그램 대부분이 작다가 특정영역만 튀는 사진의 경우는 channelNormalPercent를 사용하면 시작지점에서 너무 많이 안쪽으로 들어오게 된다. 이를 방지하기 위해서 channelNormalPercent조절은 시작과 끝점에서 16이내에서 조절한다.
      4. 시작과 끝점을 찾는 방법은 누적넓이 비유로 한다.
      5. 3개 채널 모두 normalization한다.
      6. 시작 끝지점 사이 범위가 128보다 작을 경우에는 128로 맞춘다.
      */

#if KETI_COL_COR_VER == 1
    float brightThr[3] = { 0.05, 0.0005, 0.005 };
    float redZerRatio = float(histoR[0]) / (MINITEXTURE_WIDTH * MINITEXTURE_HEIGHT);
    int red5proPos = 0;
#else
    float brightThr[3] = { 0.0005, 0.0005, 0.0005 };
#endif

    for( int channel = 0; channel < 3; ++channel)
    {
        int *histo = 0;
        double channelNormalPercent = 0;

        if( channel == 0)
        {
            histo = histoR;
            channelNormalPercent = channelNormalPercentR;
        }
        else if( channel == 1)
        {
            histo = histoG;
            channelNormalPercent = channelNormalPercentG;
        }
        else if( channel == 2)
        {
            histo = histoB;
            channelNormalPercent = channelNormalPercentB;
        }

        int maxPixels = 0;
        int startP = 0, endP = 255 , middle = 127;
        int darkExistPoint = -1 , brightExistPoint = 256; //normaliza 최소,최대값을 구할 때

        //TODO-CHECK darkStack, brightStack 로직 추가
        double pixels = MINITEXTURE_WIDTH * MINITEXTURE_HEIGHT ;/// 4.0f;//channels[cindex].rows*channels[cindex].cols/4.0;
        int darkStack=0, brightStack=0;
        for( int i=0; i<256; i++ ){
            if( maxPixels < histo[i] ) maxPixels = histo[i];
            /*normalization에서 L자 또는 L대칭형의 히스토그램일 경우 문제가 존재하기 때문에
             dark or bright 부분에서 일 정 이상 떨어지지 못하도록 구현해야한다.
             이는 어두운 사진의 경우에도 영향을 많이 미친다.
             시작과 끝지점을 찾아내는 방법은 좌우에서 중앙으로 이동하면서 히스토그램의 하단 넓이의 합을 구하고 합이
             전체 넓이의 0.05%이상일때로 잡는다.
             */
            darkStack += histo[i];
            brightStack += histo[255-i];
            if( darkExistPoint == -1 && ( darkStack != 0 && darkStack/pixels > 0.0005 ) ){
                darkExistPoint = i;
            }

            if( brightExistPoint == 256 && ( brightStack != 0 && brightStack/pixels > brightThr[channel] ) ){
                brightExistPoint = 255-i;
            }
        }
        //TODO-CHECK darkExistPoint, brightExistPoint에서 16이내로 조정하는 로직으로 수정
        for( int i=0; i<256; i++ )
            if( histo[i] > maxPixels*channelNormalPercent || i-darkExistPoint > 32 ){
                if( i-darkExistPoint > 32 ){
                    startP = darkExistPoint + 8;
                }else
                    startP = i;
                break;
            }

        for( int i=255; i>=0; i-- ){
            if( histo[i] > maxPixels*channelNormalPercent || brightExistPoint - i > 32 ){
                if( brightExistPoint - i > 32 ){
                    endP = brightExistPoint - 8;
                }else
                    endP = i;
                break;
            }
        }
        //TODO-CHECK start-end 범위가 128이내로 조정하는 아래 코드의 버그를 수정했습니다.
        middle = (startP+endP)/2;
        if( (endP-startP) < 128 ){
            if( middle + 64 > 255 ){
                endP = 255;
                middle = 255-64;
                startP = 255 - 128;
            }else if( middle - 64 < 0 ){
                startP = 0;
                endP = 128;
                middle = 64;
            }else{
                endP = ( middle + 64 );
                startP = (middle - 64);
            }
        }
        middle = (startP+endP)/2;

        if( inputValueScaleAll < 1 ){
#if KETI_COL_COR_VER == 1
            startP = startP*inputValueScaleAll;
            endP = endP + (255-endP)* (1.0 - inputValueScaleAll);
#else
            startP = startP*(1.0-inputValueScaleAll);
            endP = endP + (255-endP)*inputValueScaleAll;
#endif
        }else{
            endP -= ((endP-startP)/4)*(inputValueScaleAll-1)/0.5;
        }
       // Mat mask;
        //cv::inRange(channels[cindex], Scalar(0), Scalar(startP), mask);
        //channels[cindex].setTo(Scalar(startP), mask);

        //cv::inRange(channels[cindex], Scalar(endP), Scalar(255), mask);
        //c/hannels[cindex].setTo(Scalar(endP), mask);
        //mask.release();

        //cv::normalize(channels[cindex], channels[cindex], 0, 255, cv::NORM_MINMAX);

        if( channel == 0)
        {
            minIndexSumR = 0;//startP;
            maxIndexSumR = 255;//endP;
        }else if( channel == 1)
        {
            minIndexSumG = startP;
            maxIndexSumG = endP;
        }else if( channel == 2)
        {
            minIndexSumB = startP;
            maxIndexSumB = endP;
        }
    }
#endif

    //resultValue.isChannelMix = 0;

    /********************************
    //channel mix
   **********************************/

    /*----------------------------------
     * outputValue
     ---------------------------------*/

    float mixRFromB = 0.0f;
    float mixRFromG = 0.0f;
    float mixRFromR = 1.0f;

    float mixBFromB = 1.0f;
    float mixBFromG = 0.0f;
    float mixBFromR = 0.0f;

#if KETI_COL_COR_VER == 1
    // b1Ofpix : average
    // g1Ofpix : average
    // r1Ofpix : average

#if KETI_DEBUG == 1
    printf("red_zero_ratio: %f \t red_5pro_position: %d \n", redZerRatio, red5proPos);
#endif

    //////////////////////////////////////////////////////////////////////////////////////////
    // 기존의 xofy 는 estHE_xofy 로 대체되었음.
    if (isNeedMulMix) {

        double estHeAvgR = (r1Ofpix - minIndexSumR) / (maxIndexSumR - minIndexSumR) * 255;
        double estHeAvgG = (g1Ofpix - minIndexSumG) / (maxIndexSumG - minIndexSumG) * 255;
        double estHeAvgB = (b1Ofpix - minIndexSumB) / (maxIndexSumB - minIndexSumB) * 255;

        double estHe_rofb = estHeAvgR / estHeAvgB;
        double estHe_rofg = estHeAvgR / estHeAvgG;
        double estHe_bofg = estHeAvgB / estHeAvgG;

#if KETI_DEBUG == 1
        printf("Average R,G,B: %3.2f  %3.2f  %3.2f\n", r1Ofpix, g1Ofpix, b1Ofpix);
    printf("est Average R,G,B after HE: %3.2f  %3.2f  %3.2f\n", estHeAvgR, estHeAvgG, estHeAvgB);
    printf("est RofB, RofG, BofG after HE: %3.2f  %3.2f  %3.2f\n", estHe_rofb, estHe_rofg, estHe_bofg);
#endif

        //TODO-CHECK 현재 mulVal 수식에서 Green비율에 조정이 필요해서 GreenScalar값을 추가해두었습니다...
        double greenScalar = -7.01 * estHe_rofg * estHe_rofg * estHe_rofg + 7.64 * estHe_rofg * estHe_rofg - 3.02 * estHe_rofg + 1.18;
        if (estHe_rofg < 0.05)
            greenScalar = 1.02;
        if (estHe_rofg > 0.64)
            greenScalar = 0.52;

        float mixScale = inputValueScaleAll;
        if (mixScale > 1)
            mixScale = 1;
        estHe_rofb = estHe_rofb > 0.5 ? 0.5 : estHe_rofb;
        estHe_rofg = estHe_rofg > 1 ? 1 : estHe_rofg;

        //  double mulVal[] = { mixScale*(-0.45 -0.4*(0.5-rofb)/0.7), mixScale*greenScalar*(-6.4*( rofg - 0.5 )*( rofg - 0.5 )*( rofg - 0.5 ) + 1) , 1};

        mixRFromB = mixScale * (-0.45 - 0.4 * (0.5 - estHe_rofb) / 0.7);

        // added by jwjeong
        float mixRFromG_old = mixScale * greenScalar * (-6.4 * (estHe_rofg - 0.5) * (estHe_rofg - 0.5) * (estHe_rofg - 0.5) + 1); //old
        float mixRFromG_new = mixScale * (1 - (estHeAvgR + estHeAvgB * mixRFromB) / estHeAvgG);  // jwjeong  // new
        mixRFromG = mixRFromG_old < mixRFromG_new ? mixRFromG_old : mixRFromG_new;

        mixRFromR = 1.0f;

        // Mat chFloatTmp1, chFloatTmp2, tmpC2;
        // channels[0].convertTo(chFloatTmp1, CV_32FC1);  //blue
        // channels[1].convertTo(chFloatTmp2, CV_32FC1);
        // chFloatTmp1 *= mulVal[0];
        // chFloatTmp2 *= mulVal[1];
        // chFloatTmp1 += chFloatTmp2;
        // channels[2].convertTo(chFloatTmp2, CV_32FC1);
        // chFloatTmp1 += chFloatTmp2;
        // tmpC2 = channels[2];
        //chFloatTmp1.convertTo(channels[2], CV_8UC1);


        //about green

        //channels[1].convertTo(chFloatTmp1, CV_32FC1);
        //tmpC2.convertTo(chFloatTmp2, CV_32FC1);

        /*
         //TODO-CHECK
         녹조가 낀 사진처럼 녹색이 가장 분포가 클 경우에는
         블루도 보강을 해줄 필요가 존재한다. 보강하는 방법은 Green을 더해주고 Red를 빼주어서 보강한다.
         단, Red채널이 블루에 비해 상대적으로 작은 값을 가지고 있을 경우에는 보강비율을 줄여야 한다( 녹조랑, 그냥 녹색이 많은 경우를 구분! )
         */
        //std::pow
        float ratioG = 0, ratioR = 0;
        float scaler = 1.2 / (1 + pow(M_E, 8 * estHe_rofb - 4)); //with sigmoid
        if (estHe_bofg < 1.1)
        {
            if (estHe_bofg > 0.75)
            {
                ratioG = (0.05 + 0.4 * (1.1 - estHe_bofg) / 0.35) * scaler;
                ratioR = -1 * ratioG;
            }
            else
            {
                ratioG = (0.45 + 0.35 * (0.75 - estHe_bofg) / 0.75) * scaler;
                ratioR = ratioG * -0.9;
            }

            //chFloatTmp1 *= ratioG; //green mult
            //chFloatTmp2 *= ratioR; //red mult
            // chFloatTmp1 += chFloatTmp2;
            // channels[0].convertTo(chFloatTmp2, CV_32FC1);
            // chFloatTmp1 += chFloatTmp2;  //bluemix
            // chFloatTmp1.convertTo(channels[0], CV_8UC1);

            mixBFromB = 1.0f;
            mixBFromG = mixScale * ratioG;
            mixBFromR = mixScale * ratioR;
        }
        //  chFloatTmp1.release();
        //  chFloatTmp2.release();
        //  tmpC2.release();

        // cv::merge(channels, destMat);
        // channels[0].release();channels[1].release();channels[2].release();

        float estR = mixRFromR * estHeAvgR + mixRFromG * estHeAvgG + mixRFromB * estHeAvgB;
        float estB = mixBFromR * estR + mixBFromG * estHeAvgG + mixBFromB * estHeAvgB;

        float estRfromG = mixRFromG * estHeAvgG;
        float estRfromB = mixRFromB * estHeAvgB;

        // ver83
        float add = keti_clamp(-redZerRatio * 30 + 12, 0, 12);
        float r5proGuide = keti_clamp(-red5proPos * 0.02 + 2.4, 0, 1.);
        float add_Gratio = estRfromG != 0 ? add * r5proGuide / estRfromG : 0;
        float add_Bratio = estRfromB != 0 ? add * r5proGuide / estRfromB : 0;

        mixRFromG = mixRFromG + add_Gratio;
        mixRFromB = mixRFromB - add_Bratio;

        float rZeroGuide = keti_clamp(redZerRatio * 5 - 0.75, 0., 1.);
        float rZeroGuideOffset = keti_clamp(redZerRatio * 15 - 3, 0., 6.);

        //ver86
        float modB = keti_clamp(rZeroGuideOffset + estR - estB, 0, 10) * rZeroGuide;
        float modG = keti_clamp(rZeroGuideOffset + estR - estHeAvgG, 0, 10) * rZeroGuide;

        float mod_Gratio = estRfromG != 0 ? modG / estRfromG : 0;
        float mod_Bratio = estRfromB != 0 ? modB / estRfromB : 0; // if est_r_from_b > 0 else 0.

        mixRFromG = mixRFromG - mod_Gratio;
        mixRFromB = mixRFromB + mod_Bratio;
    }
#else
    if( isNeedMulMix) {

        //TODO-CHECK 현재 mulVal 수식에서 Green비율에 조정이 필요해서 GreenScalar값을 추가해두었습니다...
        double greenScalar = -7.01 * rofg * rofg * rofg + 7.64 * rofg * rofg - 3.02 * rofg + 1.18;
        if (rofg < 0.05) greenScalar = 1.02;
        if (rofg > 0.64) greenScalar = 0.52;

        float mixScale = inputValueScaleAll;
        if (mixScale > 1) mixScale = 1;
        rofb = rofb > 0.5 ? 0.5 : rofb;
        rofg = rofg > 1 ? 1 : rofg;

        //  double mulVal[] = { mixScale*(-0.45 -0.4*(0.5-rofb)/0.7), mixScale*greenScalar*(-6.4*( rofg - 0.5 )*( rofg - 0.5 )*( rofg - 0.5 ) + 1) , 1};

        mixRFromB = mixScale * (-0.45 - 0.4 * (0.5 - rofb) / 0.7);
        mixRFromG = mixScale * greenScalar * (-6.4 * (rofg - 0.5) * (rofg - 0.5) * (rofg - 0.5) + 1);
        mixRFromR = 1.0f;



        // Mat chFloatTmp1, chFloatTmp2, tmpC2;
        // channels[0].convertTo(chFloatTmp1, CV_32FC1);  //blue
        // channels[1].convertTo(chFloatTmp2, CV_32FC1);
        // chFloatTmp1 *= mulVal[0];
        // chFloatTmp2 *= mulVal[1];
        // chFloatTmp1 += chFloatTmp2;
        // channels[2].convertTo(chFloatTmp2, CV_32FC1);
        // chFloatTmp1 += chFloatTmp2;
        // tmpC2 = channels[2];
        //chFloatTmp1.convertTo(channels[2], CV_8UC1);




        //about green

        //channels[1].convertTo(chFloatTmp1, CV_32FC1);
        //tmpC2.convertTo(chFloatTmp2, CV_32FC1);

        /*
         //TODO-CHECK
          녹조가 낀 사진처럼 녹색이 가장 분포가 클 경우에는
          블루도 보강을 해줄 필요가 존재한다. 보강하는 방법은 Green을 더해주고 Red를 빼주어서 보강한다.
         단, Red채널이 블루에 비해 상대적으로 작은 값을 가지고 있을 경우에는 보강비율을 줄여야 한다( 녹조랑, 그냥 녹색이 많은 경우를 구분! )
         */
        //std::pow
        float ratioG = 0, ratioR = 0;
        float scaler = 1.2 / (1 + pow(M_E, 8 * rofb - 4)); //with sigmoid
        if (bofg < 1.1) {
            if (bofg > 0.75) {
                ratioG = (0.05 + 0.4 * (1.1 - bofg) / 0.35) * scaler;
                ratioR = -1 * ratioG;
            } else {
                ratioG = (0.45 + 0.35 * (0.75 - bofg) / 0.75) * scaler;
                ratioR = ratioG * -0.9;
            }

            //chFloatTmp1 *= ratioG; //green mult
            //chFloatTmp2 *= ratioR; //red mult
            // chFloatTmp1 += chFloatTmp2;
            // channels[0].convertTo(chFloatTmp2, CV_32FC1);
            // chFloatTmp1 += chFloatTmp2;  //bluemix
            // chFloatTmp1.convertTo(channels[0], CV_8UC1);


            mixBFromB = 1.0f;
            mixBFromG = mixScale * ratioG;
            mixBFromR = mixScale * ratioR;


        }
        //  chFloatTmp1.release();
        //  chFloatTmp2.release();
        //  tmpC2.release();

        // cv::merge(channels, destMat);
        // channels[0].release();channels[1].release();channels[2].release();

    }
#endif

    resultValue.startR = float(minIndexSumR) / 255.0f;
    resultValue.startG = float(minIndexSumG) / 255.0f;
    resultValue.startB = float(minIndexSumB) / 255.0f;

    resultValue.endR = float(maxIndexSumR) / 255.0f;
    resultValue.endG = float(maxIndexSumG) / 255.0f;
    resultValue.endB = float(maxIndexSumB) / 255.0f;

    resultValue.mixRfromB = mixRFromB;
    resultValue.mixRfromG = mixRFromG;
    resultValue.mixRfromR = mixRFromR;

    resultValue.mixBfromB = mixBFromB;
    resultValue.mixBfromG = mixBFromG;
    resultValue.mixBfromR = mixBFromR;

#if KETI_DEBUG == 1
    printf("startR : %.3f, endR : %.3f\n", resultValue.startR, resultValue.endR);
    printf("startG : %.3f, endG : %.3f\n", resultValue.startG, resultValue.endG);
    printf("startB : %.3f, endB : %.3f\n", resultValue.startB, resultValue.endB);

    printf("mixRfromB : %.3f, mixRfromG : %.3f, mixRfromR : %.3f\n", resultValue.mixRfromB, resultValue.mixRfromG, resultValue.mixRfromR);
    printf("mixBfromB : %.3f, mixBfromG : %.3f, mixBfromR : %.3f\n", resultValue.mixBfromB, resultValue.mixBfromG, resultValue.mixBfromR);
#endif


    /********************************
    //create histogram graph
   **********************************/

    if(GLES2Lesson::mDebugShowHistogram) {

        for (int w = 0; w < 256; ++w) {
            for (int h = 0; h < 256; ++h) {

                unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];

                value[0] = 0;
                value[1] = 0;
                value[2] = 0;
                value[3] = 128;
            }
        }


        for (int w = 0; w < 256; ++w) {

            int percent = 50 * 255 * histoR[w] / sumR;


            for (int h = 0; h < std::min(256, percent); ++h) {
                unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];

                value[0] = 128;
                value[3] = 255;
            }
        }


        for (int w = 0; w < 256; ++w) {

            int percent = 50 * 255 * histoG[w] / sumG;


            for (int h = 0; h < std::min(256, percent); ++h) {
                unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];

                value[1] = 128;
                value[3] = 255;
            }
        }


        for (int w = 0; w < 256; ++w) {

            int percent = 50 * 255 * histoB[w] / sumB;


            for (int h = 0; h < std::min(256, percent); ++h) {
                unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];

                value[2] = 128;
                value[3] = 255;
            }
        }


        for (int w = minIndexSumR; w <= maxIndexSumR; ++w) {

            int divValue = maxIndexSumR - minIndexSumR;


            if (w == minIndexSumR || w == maxIndexSumR) {

                for (int h = 0; h < 256; ++h) {
                    unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];
                    if ((h / 10) % 2)
                        value[0] = 255;
                    value[3] = 255;
                }
            }

            int h = 255 * (w - minIndexSumR) / divValue;


            unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];
            //if( (h /10) %2 )
            value[0] = 255;
            value[3] = 255;
        }


        for (int w = minIndexSumG; w <= maxIndexSumG; ++w) {

            int divValue = maxIndexSumG - minIndexSumG;

            if (w == minIndexSumG || w == maxIndexSumG) {

                for (int h = 0; h < 256; ++h) {
                    unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];

                    if ((h / 10) % 2)
                        value[1] = 255;
                    value[3] = 255;
                }
            }


            int h = 255 * (w - minIndexSumG) / divValue;


            unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];
            //if( (h /10) %2 )
            value[1] = 255;
            value[3] = 255;
        }


        for (int w = minIndexSumB; w <= maxIndexSumB; ++w) {

            int divValue = maxIndexSumB - minIndexSumB;


            if (w == minIndexSumB || w == maxIndexSumB) {

                for (int h = 0; h < 256; ++h) {
                    unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];
                    if ((h / 10) % 2)
                        value[2] = 255;

                    value[3] = 255;
                }
            }


            int h = 255 * (w - minIndexSumB) / divValue;


            unsigned char *value = (unsigned char *) &PreProcessorValue::histogram[h][w];
            //if( (h /10) %2 )
            value[2] = 255;
            value[3] = 255;
        }
    }

    return true;
}

