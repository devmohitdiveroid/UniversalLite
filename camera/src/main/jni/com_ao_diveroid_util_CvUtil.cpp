//
// Created by YunSuk Yeo on 16/11/2017.
//

/*
 히스토그램을 보면 처음 빛이 찍힌범위가 dark부분과 bright부분에 나타난다.
 normalization 범위를 지정하는데에는 여러가지 요소가 고려되야 한다.
 1. normalization 범위가 255/2 이상은 되야지 색의 끊김이 생기지 않는다.
 2. 빛이 전체범위에 많이 분포할 때에는 dark,bright가 처음 찍힌부분보다 조금 안쪽으로 normalization하는 것이 대비?효과를 줄 수 있다.
    이때, 가장 픽셀수가 많은 곳보다 channelNormalPercent 정도되는 부분까지 잘라내면 괜찮은 사진이 찍힌다.( 최대 분포기준으로 짜름 )
 3. 히스토그램 대부분이 작다가 특정영역만 튀는 사진의 경우는 channelNormalPercent를 사용하면 시작지점에서 너무 많이 안쪽으로 들어오게 된다. 이를 방지하기 위해서
    channelNormalPercent조절은 최대 시작지점에서 10이내로만 하도록 정한다.
 */


#include "com_ao_diveroid_util_CvUtil.h"
#include <android/log.h>
#include <vector>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>


#define LOG_TAG     "CvUtil"
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define KETI_COL_COR_VER 1
#define clamp(X, LOW, HIGH) (X < LOW ? LOW : (X > HIGH ? HIGH : X))
#define max(X, LOW) (X < LOW ? LOW : X)

using namespace cv;
using namespace std;

/*
 * 사용자 인풋에 의해서 변경될 수 있는 값은 scalarBF, scalarGF 입니다. 앱화면에서 SeekBar로 되어 있는 부분에서 값을 가져옵니다.
 * */
static float scalePercent;
static Mat savedMat;
double b1, g1, r1, scaleAll=1;

#if KETI_COL_COR_VER==1
struct ImageInfo{
    double avgB;
    double avgG;
    double avgR;

    double minIndexSumB;
    double minIndexSumG;
    double minIndexSumR;

    double maxIndexSumB;
    double maxIndexSumG;
    double maxIndexSumR;

    double red5proPos;
    double redZerRatio;
};
#endif

/*
 * 계산된 rofb, rofg값을 사용하여 채널믹스를 합니다.
 * */
#if KETI_COL_COR_VER==1
void channelMix(Mat& bgrMat, Mat&destMat, double rofb , double rofg, double bofg, ImageInfo info ) {
#else
void channelMix(Mat& bgrMat, Mat&destMat, double rofb , double rofg, double bofg) {
#endif
    vector<Mat> channels;
    cv::split(bgrMat, channels);
    bgrMat.release();

#if KETI_COL_COR_VER==1
    float scaler = 1.2 / (1 + pow(M_E, 8 * rofb - 4)); //with sigmoid

    double estHeAvgR = (info.avgR - info.minIndexSumR) / (info.maxIndexSumR - info.minIndexSumR) * 255;
    double estHeAvgG = (info.avgG - info.minIndexSumG) / (info.maxIndexSumG - info.minIndexSumG) * 255;
    double estHeAvgB = (info.avgB - info.minIndexSumB) / (info.maxIndexSumB - info.minIndexSumB) * 255;

    rofb = estHeAvgR / estHeAvgB;
    rofg = estHeAvgR / estHeAvgG;
    bofg = estHeAvgB / estHeAvgG;
#endif

    double greenScalar = -7.01*rofg*rofg*rofg+7.64*rofg*rofg-3.02*rofg+1.18;
    if( rofg < 0.05 ) greenScalar =1.02;
    if( rofg > 0.64 ) greenScalar = 0.52;

    float mixScale = scaleAll;
    if( mixScale > 1 ) mixScale = 1;
    rofb = rofb>0.5?0.5:rofb;
    rofg = rofg>1?1:rofg;

#if KETI_COL_COR_VER==1
    // mulVal[] = {mixRFromB, mixRFromG, mixRFromR}
    double mulVal[] = { mixScale * (-0.45 - 0.4 * (0.5 - rofb) / 0.7), mixScale * greenScalar * (-6.4 * (rofg - 0.5) * (rofg - 0.5) * (rofg - 0.5) + 1) , 1 };
    float mixRFromG_new = mixScale * (1 - (estHeAvgR + estHeAvgB * mulVal[0]) / estHeAvgG);  // jwjeong  // new
    mulVal[1] = mulVal[1] < mixRFromG_new ? mulVal[1] : mixRFromG_new;

    float ratioG = 0, ratioR = 0;
    float mixBFromB = 1.0, mixBFromG = 0, mixBFromR = 0;
    if (bofg < 1.1) {
        if (bofg > 0.75) {
            ratioG = (0.05 + 0.4 * (1.1 - bofg) / 0.35) * scaler * mixScale;
            ratioR = -1 * ratioG;
        }
        else {
            ratioG = (0.45 + 0.35 * (0.75 - bofg) / 0.75) * scaler * mixScale;
            ratioR = ratioG * -0.9;
        }
        mixBFromB = 1.0f;
        mixBFromG = mixScale * ratioG;
        mixBFromR = mixScale * ratioR;
    }

    // added by jwjeong
    float estR = mulVal[2] * estHeAvgR + mulVal[1] * estHeAvgG + mulVal[0] * estHeAvgB;
    float estB = mixBFromR * estR + mixBFromG * estHeAvgG + mixBFromB * estHeAvgB;

    float estRfromG = mulVal[1] * estHeAvgG;
    float estRfromB = mulVal[0] * estHeAvgB;

    // ver83
    float add = clamp(-info.redZerRatio * 30 + 12, 0, 12);
    float r5proGuide = clamp(-info.red5proPos * 0.02 + 2.4, 0, 1.);
    float add_Gratio = estRfromG != 0 ? add * r5proGuide / estRfromG : 0;
    float add_Bratio = estRfromB != 0 ? add * r5proGuide / estRfromB : 0;

    mulVal[1] = mulVal[1] + add_Gratio;
    mulVal[0] = mulVal[0] - add_Bratio;

    float rZeroGuide = clamp(info.redZerRatio * 5 - 0.75, 0., 1.);
    float rZeroGuideOffset = clamp(info.redZerRatio * 15 - 3, 0., 6.);

    //ver86
    float modB = clamp(rZeroGuideOffset + estR - estB, 0, 10) * rZeroGuide;
    float modG = clamp(rZeroGuideOffset + estR - estHeAvgG, 0, 10) * rZeroGuide;

    float mod_Gratio = estRfromG != 0 ? modG / estRfromG : 0;
    float mod_Bratio = estRfromB != 0 ? modB / estRfromB : 0;  // if est_r_from_b > 0 else 0.
    mulVal[1] = mulVal[1] - mod_Gratio;
    mulVal[0] = mulVal[0] + mod_Bratio;

    // Red channel mixing
    Mat chFloatTmp1, chFloatTmp2, tmpC2;
    channels[0].convertTo(chFloatTmp1, CV_32FC1);
    channels[1].convertTo(chFloatTmp2, CV_32FC1);
    chFloatTmp1 *= mulVal[0];
    chFloatTmp2 *= mulVal[1];
    chFloatTmp1 += chFloatTmp2;
    channels[2].convertTo(chFloatTmp2, CV_32FC1);
    chFloatTmp1 += chFloatTmp2;
    tmpC2 = channels[2];
    chFloatTmp1.convertTo(channels[2], CV_8UC1);

    channels[1].convertTo(chFloatTmp1, CV_32FC1);
    tmpC2.convertTo(chFloatTmp2, CV_32FC1);

    //float scaler = 1.2 / (1 + pow(M_E, 8 * rofb - 4)); //with sigmoid
    if (bofg < 1.1) {
        chFloatTmp1 *= ratioG;
        chFloatTmp2 *= ratioR;
        chFloatTmp1 += chFloatTmp2;
        channels[0].convertTo(chFloatTmp2, CV_32FC1);
        chFloatTmp1 += chFloatTmp2;
        chFloatTmp1.convertTo(channels[0], CV_8UC1);
    }
#else
    double mulVal[] = { mixScale*(-0.45 -0.4*(0.5-rofb)/0.7), mixScale*greenScalar*(-6.4*( rofg - 0.5 )*( rofg - 0.5 )*( rofg - 0.5 ) + 1) , 1};

    Mat chFloatTmp1, chFloatTmp2, tmpC2;
    channels[0].convertTo(chFloatTmp1, CV_32FC1);
    channels[1].convertTo(chFloatTmp2, CV_32FC1);
    chFloatTmp1 *= mulVal[0];
    chFloatTmp2 *= mulVal[1];
    chFloatTmp1 += chFloatTmp2;
    channels[2].convertTo(chFloatTmp2, CV_32FC1);
    chFloatTmp1 += chFloatTmp2;
    tmpC2 = channels[2];
    chFloatTmp1.convertTo(channels[2], CV_8UC1);

    channels[1].convertTo(chFloatTmp1, CV_32FC1);
    tmpC2.convertTo(chFloatTmp2, CV_32FC1);

    float ratioG = 0, ratioR = 0;
    float scaler = 1.2/( 1+ pow( M_E, 8*rofb - 4 )); //with sigmoid
    if( bofg < 1.1 ){
        if( bofg > 0.75 ){
            ratioG = ( 0.05 + 0.4 * ( 1.1 - bofg )/0.35 )*scaler*mixScale;
            ratioR = -1*ratioG;
        }else{
            ratioG = (0.45 + 0.35 * ( 0.75 - bofg )/0.75)*scaler*mixScale;
            ratioR = ratioG*-0.9;
        }

        chFloatTmp1 *= ratioG;
        chFloatTmp2 *= ratioR;
        chFloatTmp1 += chFloatTmp2;
        channels[0].convertTo(chFloatTmp2, CV_32FC1);
        chFloatTmp1 += chFloatTmp2;
        chFloatTmp1.convertTo(channels[0], CV_8UC1);
    }
#endif

    chFloatTmp1.release();
    chFloatTmp2.release();
    tmpC2.release();

    cv::merge(channels, destMat);
    channels[0].release();channels[1].release();channels[2].release();
}

double getNormalPercent(double colorOfPix){
    return (5+0.000000003*(colorOfPix+100) *( colorOfPix - 255)*( colorOfPix - 255)*( colorOfPix - 255))/200;
}

/*
 * isNeedMulMix는 채널 믹스를 할지 말지를 결정하는 변수입니다. 지상에서 찍은 사진처럼 레드값이 많이 분포하는 사진의 경우는 3채널 normalization만 수행합니다.
 * isNeedMulMix가 true일 경우는 레드채널에 대해서는 normalization을 하지 않습니다.
 * */
bool isNeedMulMix;
#if KETI_COL_COR_VER==1
void colorBalance(Mat& bgrMat, Mat&destMat, ImageInfo &info) { //percent값은 무시하세요~! 안쓰입니다.
#else
void colorBalance(Mat& bgrMat, Mat&destMat, int percent) { //percent값은 무시하세요~! 안쓰입니다.
#endif

    vector<Mat> channels;

    //double halfPercent = percent / 200.0;

    cv::split(bgrMat, channels);
    bgrMat.release();

    b1 = cv::sum(channels[0])[0];
    g1 = cv::sum(channels[1])[0];
    r1 = cv::sum(channels[2])[0];
    double b1Ofpix = b1/(channels[0].rows*channels[0].cols);
    double g1Ofpix = g1/(channels[0].rows*channels[0].cols);
    double r1Ofpix = r1/(channels[0].rows*channels[0].cols);

#if KETI_COL_COR_VER==1
    info.avgB = b1Ofpix;
    info.avgG = g1Ofpix;
    info.avgR = r1Ofpix;
#endif

    // normalization범위를 얻기 위해서 사용되는 값입니다.
    double channelNormalPercent[3] = { getNormalPercent(b1Ofpix),getNormalPercent(g1Ofpix),getNormalPercent(r1Ofpix)};
    isNeedMulMix = (r1/b1 < 0.75 || r1/g1 < 0.85);

#if KETI_COL_COR_VER==1
    // Added by jwjeong
    double brightThr[3] = { 0.005, 0.0005, 0.05 };   //B, G, R
#else
    double brightThr[3] = { 0.0005, 0.0005, 0.0005 };   //B, G, R
#endif

    for(int cindex = 0; cindex < 3; cindex++) {

        int maxPixels = 0;
        int startP = 0, endP = 255 , middle = 127;
        int darkExistPoint = -1 , brightExistPoint = 256; //normaliza 최소,최대값을 구할 때
        int histo[256] = {0};

        for(int i=0; i<channels[cindex].rows;i = i + 2)
            for(int j=0; j<channels[cindex].cols;j = j + 2)
                histo[(int)channels[cindex].at<uchar>(i,j)]++;

        double pixels = channels[cindex].rows*channels[cindex].cols/4.0;

#if KETI_COL_COR_VER==1
        // Added by jwjeong
        if (cindex == 2)
            info.redZerRatio = histo[0] / pixels;
#endif

        int darkStack=0, brightStack=0;
        for( int i=0; i<256; i++ ){
            if( maxPixels < histo[i] ) maxPixels = histo[i];

            darkStack += histo[i];
            brightStack += histo[255-i];
            if( darkExistPoint == -1 && ( darkStack != 0 && darkStack/pixels > 0.0005 ) ){
                darkExistPoint = i;
            }
            if( brightExistPoint == 256 && ( brightStack != 0 && brightStack/pixels > brightThr[cindex] ) ){
                brightExistPoint = 255-i;
            }

#if KETI_COL_COR_VER==1
            if (cindex == 2)
                info.red5proPos = brightExistPoint;
#endif
        }
        for( int i=0; i<256; i++ )
            if( histo[i] > maxPixels*channelNormalPercent[cindex]  || i-darkExistPoint > 32 ){
                if( i-darkExistPoint > 32 ){
                    startP = darkExistPoint + 8;
                }else{
                    startP = i;
                }

                break;
            }

        for( int i=255; i>=0; i-- ){
            if( histo[i] > maxPixels*channelNormalPercent[cindex] || brightExistPoint - i > 32 ){
                if( brightExistPoint - i > 32 ){
                    endP = brightExistPoint - 8;
                }else{
                    endP = i;
                }
                break;
            }
        }
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

        if( scaleAll < 1 ){
            startP = startP*scaleAll;
            endP = endP + (255-endP)*(1-scaleAll);
        }else{
            endP -= ((endP-startP)/4)*(scaleAll-1)/0.5;
        }

#if KETI_COL_COR_VER==1
        if (cindex == 0)
        {
            info.minIndexSumB = startP;
            info.maxIndexSumB = endP;
        }
        else if (cindex == 1)
        {
            info.minIndexSumG = startP;
            info.maxIndexSumG = endP;
        }
        else
        {
            startP = 0;
            endP = 255;
            info.minIndexSumR = startP;
            info.maxIndexSumR = endP;
        }
#endif

        Mat mask;
        cv::inRange(channels[cindex], Scalar(0), Scalar(startP), mask);
        channels[cindex].setTo(Scalar(startP), mask);

        cv::inRange(channels[cindex], Scalar(endP), Scalar(255), mask);
        channels[cindex].setTo(Scalar(endP), mask);
        mask.release();

#if KETI_COL_COR_VER==1
        if (cindex <= 1)
            cv::normalize(channels[cindex], channels[cindex], 0, 255, cv::NORM_MINMAX);
#else
        cv::normalize(channels[cindex], channels[cindex], 0, 255, cv::NORM_MINMAX);
#endif

         /*
         위의 normalization을 pixel단위로 구현한 코드이다.
         하지만 위의 normalization과 큰 차이가 있다.
         normalize함수는 startP,endP와 상관없이 분포도를 보고 최소->0 최대->255로 맞춰준다. 그 위에 inRange로 startP, endP를 맞춰주지만, startP==0, endP==255 일때에는 inRagne는 효력이 없다.
         그런데 normalize함수는 분포도를 보고 알아서 normal시키기 때문에 아래 코드와 차이가 존재한다.
         아래코드는 startP, endP에 맞춰서 scale해준다. => 실제 OpenGL의 로직은 아래와 같다.
        for(int i=0; i<channels[cindex].rows;i = i + 1)
            for(int j=0; j<channels[cindex].cols;j = j + 1)
                if( (int)channels[cindex].at<uchar>(i,j) <= startP ){
                    channels[cindex].at<uchar>(i,j) = (uchar)0;
                }else if( (int)channels[cindex].at<uchar>(i,j) >= endP ){
                    channels[cindex].at<uchar>(i,j) = (uchar)255;
                }else{
                    channels[cindex].at<uchar>(i,j) = (uchar)((int)((255.0/(endP-startP))*((int)channels[cindex].at<uchar>(i,j)-startP) ));
                }
                */
    }

    cv::merge(channels, destMat);
    channels[0].release();channels[1].release();channels[2].release();
}

extern "C" {
JNIEXPORT
jlong Java_com_diveroid_camera_utils_CvUtil_filter
( JNIEnv * env , jclass cls, jlong
mat_addr , double level ) {
Mat &rgbaMat = *(Mat *) mat_addr; // 8UC3
Mat bgrMat(rgbaMat.rows, rgbaMat.cols, CV_8UC3);
scaleAll = level;
cv::cvtColor(rgbaMat, bgrMat, COLOR_RGBA2BGR ) ;

if ( bgrMat . channels() != 3 ) {
LOGE("image bitmap is abnormal") ;
return 0 ;
}

Mat resultMat;
#if KETI_COL_COR_VER == 1
struct ImageInfo info;
colorBalance(bgrMat, resultMat, info
);
#else
colorBalance(bgrMat, resultMat, scalePercent);
#endif
if(isNeedMulMix){
#if KETI_COL_COR_VER == 1
channelMix( resultMat, resultMat, r1
/b1, r1/g1 , b1/g1 , info );
#else
channelMix( resultMat, resultMat , r1/b1, r1/g1 , b1/g1 );
#endif
}
cv::cvtColor(resultMat, rgbaMat, cv::COLOR_BGR2RGBA
);
resultMat.

release();

return (jlong)&
rgbaMat;
}
}




