package com.diveroid.camera.housing

import android.graphics.PointF


/**
 * ojk
 * https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
 */

class UniversalBtnPointManager(isPortrait : Boolean = false ) : HousingBtnPositioner(isPortrait) {
    override fun getHousingName() = "Universal"
    override fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action) {

        //Portrait Setting
        //가로모드일때에는 inVersY가 필요 없고 x로 사용하면됨 구현해둠
        var x = if( isPortrait ) touchX else touchInVersY
        var y = if( isPortrait ) touchY else touchX
        var pointList = if (action.name.contains("Btn1")) {
            arrayOf(PointF(x, y),
                    PointF(x * 38.5f / 56, y + x * 10f / 56f),
                    PointF(x * 21f / 56, y))
        } else if (action.name.contains("Btn2")) {
            arrayOf(PointF( x * 56f / 38.5f, y - x * 10f / 38.5f),
                    PointF(x, y),
                    PointF( x * 21f / 38.5f, y - x * 10f / 38.5f))
        } else if (action.name.contains("Btn3")) {
            arrayOf(PointF(x * 56f / 21f , y),
                    PointF(x * 38.5f / 21f, y + x * 10f / 21f),
                    PointF(x, y))
        } else arrayOf(PointF(), PointF(), PointF())

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x,pointList[i].y, it, true)
            }
        }

        //Portrait Setting
        x = if( isPortrait ) y else touchX
        y = if( isPortrait ) touchInVersX else touchY
        val inVersY =  if( isPortrait ) x else touchInVersY

        pointList = if (action.name.contains("Btn1")) {
            arrayOf(
                    PointF( x, y),
                    PointF(x + inVersY*10/56f, y + inVersY*17.5f/56),
                    PointF(x,y + 2* inVersY*17.5f/56)
            )
        } else if (action.name.contains("Btn2")) {
            arrayOf(
                    PointF(x - inVersY*10/38.5f, y - inVersY*17.5f/38.5f),
                    PointF(x,y),
                    PointF(x - inVersY*10/38.5f, y + inVersY*17.5f/38.5f)
            )
        } else if (action.name.contains("Btn3")) {
            arrayOf(
                    PointF(x,y - 2* inVersY*17.5f/( 38.5f - 17.5f )),
                    PointF(x + inVersY*10/56f, y - inVersY*17.5f/( 38.5f - 17.5f ) ),
                    PointF(x,y)
            )
        } else arrayOf(PointF(), PointF(), PointF())

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x,pointList[i].y, it, false)
            }
        }
        setActive()
    }
}

class XpoovvBtnPositioner( isPortrait : Boolean = false ) : HousingBtnPositioner(isPortrait) {
    override fun getHousingName() = "Xpoovv"
    override fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action) {
        val term = if( isPortrait ) (touchX+touchInVersX)/4 else (touchY+touchInVersY)/4
        val y = if( isPortrait ) touchY else touchX
        var pointList = arrayOf(
            PointF(term*3.5f, y ),
            PointF(term*2.5f, y ),
            PointF(term*1.5f, y )
        )

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x, pointList[i].y, it,
                        true) // Portrait 버튼들을 세팅하는 과정
            }
        }

        val x = if( isPortrait ) touchY else touchX
        pointList = arrayOf(
                PointF(x, term*0.5f  ),
                PointF(x, term*1.5f  ),
                PointF(x, term*2.5f  )
        )

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x, pointList[i].y, it,
                        false) // Portrait 버튼들을 세팅하는 과정
            }
        }
        // LandScapeList도 위와 비슷하게 진행한다.!
        setActive()
    }
}

class MpacBtnPositioner( isPortrait : Boolean = false ) : HousingBtnPositioner(isPortrait) {
    override fun getHousingName() = "Mpac"
    override fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action) {
        val term = if( isPortrait ) (touchX+touchInVersX)/3 else (touchY+touchInVersY)/3
        val y = if( isPortrait ) touchY else touchX
        var pointList = arrayOf(
            PointF(term*2.5f, y ),
            PointF(term*1.5f, y ),
            PointF(term*0.5f, y )
        )

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x, pointList[i].y, it,
                        true) // Portrait 버튼들을 세팅하는 과정
            }
        }

        val x = if( isPortrait ) touchY else touchX
        pointList = arrayOf(
                PointF(x, term*0.5f  ),
                PointF(x, term*1.5f  ),
                PointF(x, term*2.5f  )
        )

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x, pointList[i].y, it,
                        false) // Portrait 버튼들을 세팅하는 과정
            }
        }
        // LandScapeList도 위와 비슷하게 진행한다.!
        setActive()
    }
}

class PatimaBtnPoisitioner( isPortrait : Boolean = false ) : HousingBtnPositioner(isPortrait) {
    override fun getHousingName() = "Patima"
    override fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action) {
        //Portrait Setting
        //가로모드일때에는 inVersY가 필요 없고 x로 사용하면됨 구현해둠
        var x = if( isPortrait ) touchX else touchInVersY
        var y = if( isPortrait ) touchY else touchX
        var pointList = if (action.name.contains("Btn1")) {
            arrayOf(PointF(x, y),
                    PointF(x * 38.5f / 61.5f, y + x * 15.5f / 61.5f),
                    PointF(x * 17f / 61.5f, y + x * 15.5f / 61.5f))
        } else if (action.name.contains("Btn2")) {
            arrayOf(PointF( x * 61.5f / 35.2f, y - x * 15.5f / 35.2f),
                    PointF(x, y),
                    PointF( x * 17f / 35.2f, y))
        } else if (action.name.contains("Btn3")) {
            arrayOf(PointF(x * 61.5f / 17f , y - x * 15.5f / 17f),
                    PointF(x * 35.2f / 17f, y),
                    PointF(x, y))
        } else arrayOf(PointF(), PointF(), PointF())

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x,pointList[i].y, it, true)
            }
        }

        //Portrait Setting
        x = if( isPortrait ) y else touchX
        y = if( isPortrait ) touchInVersX else touchY
        val inVersY =  if( isPortrait ) x else touchInVersY

        pointList = if (action.name.contains("Btn1")) {
            arrayOf(
                    PointF( x, y),
                    PointF(x + inVersY*15.5f/61.5f, y + inVersY*26.3f/61.5f),
                    PointF(x,y + inVersY*44.5f/61.5f)
            )
        } else if (action.name.contains("Btn2")) {
            arrayOf(
                    PointF(x - inVersY*15.5f/35.2f, y - inVersY*26.3f/35.2f),
                    PointF(x,y),
                    PointF(x, y + inVersY*18.2f/35.2f)
            )
        } else if (action.name.contains("Btn3")) {
            arrayOf(
                    PointF(x - inVersY*15.5f/17f ,y - inVersY*44.5f/17f),
                    PointF(x, y - inVersY*18.2f/17f ),
                    PointF(x,y)
            )
        } else arrayOf(PointF(), PointF(), PointF())

        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( pointList[i].x,pointList[i].y, it, false)
            }
        }
        // LandScapeList도 위와 비슷하게 진행한다.!
        setActive()
    }
}

class HousingBtnPoisitioner_NewPositioner_template( isPortrait : Boolean = false ) : HousingBtnPositioner(isPortrait) {
    override fun getHousingName() = "Example"
    override fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action) {
        // 먼저 PortraitPointList를 계산한다. 현재 상태가 Landscape여도 Portrait를 계산할 수 있다.
        var x = if( isPortrait ) touchX else touchInVersY
        var y = if( isPortrait ) touchY else touchX
        var PortraitPointList = if(action.name.contains("Btn1")) {
            //Btn1번 입력이 들어왔다는 가정하에 다른 버튼들을 세팅한다.
            arrayOf(
                    PointF(),
                    PointF(),
                    PointF()
            )
        }else if( action.name.contains("Btn2")) {
            //Btn2번 입력이 들어왔다는 가정하에 다른 버튼들을 세팅한다.
            arrayOf(
                    PointF(),
                    PointF(),
                    PointF()
            )
        }else{
            //Btn3번 입력이 들어왔다는 가정하에 다른 버튼들을 세팅한다.
            arrayOf(
                    PointF(),
                    PointF(),
                    PointF()
            )
        }
        for( i in 0..2 ){
            samePositionList[i].iterator().forEach {
                setPositionOf( PortraitPointList[i].x, PortraitPointList[i].y, it,
                        true) // Portrait 버튼들을 세팅하는 과정
            }
        }
        // LandScapeList도 위와 비슷하게 진행한다.!
        setActive()
    }
}