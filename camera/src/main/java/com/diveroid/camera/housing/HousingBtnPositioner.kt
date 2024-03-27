package com.diveroid.camera.housing

import android.graphics.PointF
import com.diveroid.camera.provider.ContextProvider
import com.diveroid.core.preference.SharedPreferenceHelper
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken


/**
 * ojk
 * https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
 */

abstract class HousingBtnPositioner(var isPortrait : Boolean = false) {
    val samePositionList = arrayOf(
        arrayOf(
            Action.Btn1Up,
            Action.Btn1Down,
            Action.Btn1Click,
            Action.Btn1Long
        ), arrayOf(
            Action.Btn2Up,
            Action.Btn2Down,
            Action.Btn2Click,
            Action.Btn2Long
        ), arrayOf(
            Action.Btn3Up,
            Action.Btn3Down,
            Action.Btn3Click,
            Action.Btn3Long
        )
    )

    var landAction : Map<Action, BPosition> = Action.values().associateWith { BPosition(it) }
    var portAction : Map<Action, BPosition> = Action.values().associateWith { BPosition(it) }
    var isActive : Boolean = false
    inner class BPosition(var action : Action){
        var point = PointF(-1000F,-1000F) //버튼을 아예숨겨버림
    }

    fun getPositionOf(action : Action, isPortrait : Boolean ) = if( isPortrait ) portAction[action]!!.point else  landAction[action]!!.point
    internal fun setPositionOf(x:Float, y:Float, action: Action, isPortrait: Boolean ){
        val buttonAction = if( isPortrait ) portAction else landAction
        buttonAction[action]!!.point.x = x
        buttonAction[action]!!.point.y = y
    }

    protected fun setActive(){
        saveDefaultPosition()
    }


    /**
     *  현재가 가로모드인지 세로모드인지를 보고 현재를 기준으로 모든 버튼의 위치를 세팅하는 로직이다.
     *  현재모드를 기준으로 가로모드에서의 버튼들과 세로모드에서의 버튼 모두를 세팅한다.
     *  즉, 현재가 가로모드라면 touchX, touchY값들을 모두 신뢰해서 가로모드용버튼세팅을 하고,
     *  세로모드는 세로모드의 경우의 x, y, 를 현재 touch값들 기준으로 계산해서 세로모드 세팅을 한다.
     *  취지는 버튼이 한번이라도 클릭되면 어떤 모드에서든 세팅된 상태로 UI가 보일 수 있게 하기 위함이다.
     *  TODO:버튼이 한번도 눌릴지 않은 상태에서 위치세팅을 못해주는 단점이 있다. 디폴트 세팅값 정의가 필요하다.
     */
    abstract fun setPositionFrom(touchX: Float, touchY: Float, touchInVersX: Float, touchInVersY: Float, action: Action)
    abstract fun getHousingName() : String

    private fun saveDefaultPosition(){
        val pk = "${getHousingName()}_POINT"
        val landJson = Gson().toJson(landAction)
        val portJson= Gson().toJson(portAction)

        SharedPreferenceHelper(ContextProvider.context!!).apply {
            putStringValue("${pk}_LAND", landJson)
            putStringValue("${pk}_PORT", portJson)
        }
    }

    private fun initDefaultPosition(){
        val pk = "${getHousingName()}_POINT"

        val landJson = SharedPreferenceHelper(ContextProvider.context!!).getString("${pk}_LAND")
        val portJson = SharedPreferenceHelper(ContextProvider.context!!).getString("${pk}_PORT")
        if( landJson == "" || portJson == "" ){
            //TODO Setting Default - 현재는 버튼 안보이기
        }else{
            landAction = Gson().fromJson(JsonParser.parseString(landJson), object : TypeToken<Map<Action, BPosition>>() {}.type)
            portAction = Gson().fromJson(JsonParser.parseString(portJson), object : TypeToken<Map<Action, BPosition>>() {}.type)
        }
    }

    init {
        initDefaultPosition()
        TouchMainCenter.getInstance().btnPositionOb.value = this
        isActive = true
    }
}
