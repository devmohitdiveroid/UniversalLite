//
// Created by kyt77 on 2019-07-04.
//

static float scalarBF, scalarGF, scaleAll;


//TODO 이거 퍼센트 함수 개선할 필요성 있다. 초록색 사진에 대해서 잘 안된다...
double getNormalPercent(double colorOfPix){
    return (5+0.000000003*(colorOfPix+100) *( colorOfPix - 255)*( colorOfPix - 255)*( colorOfPix - 255))/200;
}

double b1, g1, r1;
BOOL isNeedMulMix;
void colorBalance(Mat& bgrMat, Mat&destMat) {

    vector<Mat> channels;

    cv::split(bgrMat, channels);
    bgrMat.release();

    b1 = cv::sum(channels[0])[0];
    g1 = cv::sum(channels[1])[0];
    r1 = cv::sum(channels[2])[0];
    NSLog(@"filter ratio r/b=%.2f r/g=%.2f", r1/b1, r1/g1 );
    double b1Ofpix = b1/(channels[0].rows*channels[0].cols);
    double g1Ofpix = g1/(channels[0].rows*channels[0].cols);
    double r1Ofpix = r1/(channels[0].rows*channels[0].cols);
    double channelNormalPercent[3] = { getNormalPercent(b1Ofpix),getNormalPercent(g1Ofpix),getNormalPercent(r1Ofpix)};
    isNeedMulMix = (r1/b1 < 0.75 || r1/g1 < 0.85);
    for(int cindex = 0; cindex < channels.size()-1; cindex++) {
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
        int maxPixels = 0;
        int startP = 0, endP = 255 , middle = 127;
        int darkExistPoint = -1 , brightExistPoint = 256; //normaliza 최소,최대값을 구할 때
        int histo[256] = {0};

        for(int i=0; i<channels[cindex].rows;i = i + 2)
            for(int j=0; j<channels[cindex].cols;j = j + 2)
                histo[(int)channels[cindex].at<uchar>(i,j)]++;

        //TODO-CHECK darkStack, brightStack 로직 추가
        double pixels = channels[cindex].rows*channels[cindex].cols/4.0;
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
            if( brightExistPoint == 256 && ( brightStack != 0 && brightStack/pixels > 0.0005 ) ){
                brightExistPoint = 255-i;
            }
        }
        //TODO-CHECK darkExistPoint, brightExistPoint에서 16이내로 조정하는 로직으로 수정
        for( int i=0; i<256; i++ )
            if( histo[i] > maxPixels*channelNormalPercent[cindex] || i-darkExistPoint > 16 ){
                if( i-darkExistPoint > 16 ){
                    startP = darkExistPoint + 8;
                }else
                    startP = i;
                break;
            }

        for( int i=255; i>=0; i-- ){
            if( histo[i] > maxPixels*channelNormalPercent[cindex] || brightExistPoint - i > 16 ){
                if( brightExistPoint - i > 16 ){
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

        if( scaleAll < 1 ){
            startP = startP*scaleAll;
            endP = endP + (255-endP)*scaleAll;
        }else{
            endP -= ((endP-startP)/4)*(scaleAll-1)/0.5;
        }
        Mat mask;
        cv::inRange(channels[cindex], Scalar(0), Scalar(startP), mask);
        channels[cindex].setTo(Scalar(startP), mask);

        cv::inRange(channels[cindex], Scalar(endP), Scalar(255), mask);
        channels[cindex].setTo(Scalar(endP), mask);
        mask.release();

        cv::normalize(channels[cindex], channels[cindex], 0, 255, cv::NORM_MINMAX);
    }

    cv::merge(channels, destMat);
    channels[0].release();channels[1].release();channels[2].release();
}

void channelMix(Mat& bgrMat, Mat&destMat, double rofb , double rofg , double bofg ) {
    vector<Mat> channels;
    cv::split(bgrMat, channels);
    bgrMat.release();


    //TODO-CHECK 현재 mulVal 수식에서 Green비율에 조정이 필요해서 GreenScalar값을 추가해두었습니다...
    double greenScalar = -7.01*rofg*rofg*rofg+7.64*rofg*rofg-3.02*rofg+1.18;
    if( rofg < 0.05 ) greenScalar =1.02;
    if( rofg > 0.64 ) greenScalar = 0.52;

    float mixScale = scaleAll;
    if( mixScale > 1 ) mixScale = 1;
    rofb = rofb>0.5?0.5:rofb;
    rofg = rofg>1?1:rofg;

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

    /*
     //TODO-CHECK
      녹조가 낀 사진처럼 녹색이 가장 분포가 클 경우에는
      블루도 보강을 해줄 필요가 존재한다. 보강하는 방법은 Green을 더해주고 Red를 빼주어서 보강한다.
     단, Red채널이 블루에 비해 상대적으로 작은 값을 가지고 있을 경우에는 보강비율을 줄여야 한다( 녹조랑, 그냥 녹색이 많은 경우를 구분! )
     */
    float ratioG = 0, ratioR = 0;
    float scaler = 1.2/( 1+ pow( M_E, 8*rofb - 4 )); //with sigmoid
    if( bofg < 1.1 ){
        if( bofg > 0.75 ){
            ratioG = ( 0.05 + 0.4 * ( 1.1 - bofg )/0.35 )*scaler;
            ratioR = -1*ratioG;
        }else{
            ratioG = (0.45 + 0.35 * ( 0.75 - bofg )/0.75)*scaler;
            ratioR = ratioG*-0.9;
        }

        chFloatTmp1 *= ratioG;
        chFloatTmp2 *= ratioR;
        chFloatTmp1 += chFloatTmp2;
        channels[0].convertTo(chFloatTmp2, CV_32FC1);
        chFloatTmp1 += chFloatTmp2;
        chFloatTmp1.convertTo(channels[0], CV_8UC1);
    }
    chFloatTmp1.release();
    chFloatTmp2.release();
    tmpC2.release();

    cv::merge(channels, destMat);
    channels[0].release();channels[1].release();channels[2].release();
}
long int getMillisecond(){
    struct timeval tp;
    gettimeofday(&tp, NULL);
    return tp.tv_sec * 1000 + tp.tv_usec / 1000;
}
// main process
-(UIImage *)simplistCB:(UIImage *) m{
return [self simplistCB:m isNewImg:true isApplyToOrigin:false scaleNormalPercent:5 scalB:0.8 scalG:0.8];
}
-(UIImage *)simplistCB:(UIImage *) m isApplyToOrigin:(BOOL)isApplyToOrigin{
return [self simplistCB:m isNewImg:false isApplyToOrigin:isApplyToOrigin scaleNormalPercent:5 scalB:0.8 scalG:0.8];
}
-(UIImage *)simplistCB:(UIImage *) m isNewImg:(BOOL)isNewImg isApplyToOrigin:(BOOL)isApplyToOrigin scalB:(float)scalB scalG:(float)scalG{
return [self simplistCB:m isNewImg:isNewImg isApplyToOrigin:isApplyToOrigin scaleNormalPercent:5 scalB:scalB scalG:scalG];
}
-(UIImage *)simplistCB:(UIImage *) m isNewImg:(BOOL)isNewImg isApplyToOrigin:(BOOL)isApplyToOrigin scaleNormalPercent:(float)scaleNormalPercent scalB:(float)scalB scalG:(float)scalG
{
scaleAll = scalB;

cv::Mat image;
UIImageToMat(m, image, false);
cv::cvtColor(image, image, CV_RGBA2BGRA);
image.convertTo(image, CV_8UC1);
//NSLog(@"Filter Log1 %ld", (getMillisecond() - startTime));

Mat resultMat;
colorBalance(image, resultMat);
if(isNeedMulMix ){
channelMix( resultMat, resultMat , r1/b1, r1/g1, b1/g1 );
}

cv::cvtColor(resultMat, resultMat, cv::COLOR_BGR2RGBA);

UIImage* result = MatToUIImage(resultMat);
result = [[UIImage alloc] initWithCGImage:result.CGImage scale:result.scale orientation:m.imageOrientation];
resultMat.release();

return result;
}