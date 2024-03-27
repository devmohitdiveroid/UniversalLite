package com.diveroid.camera.housing

import com.diveroid.camera.constant.DebugConstant

class TouchManager(var isPortrait : Boolean ) {
    fun activeEvent(action : Action, x :Float, y: Float ){
        TouchMainCenter.getInstance().buttonEventOb.value = Event(isPortrait,action,x,y)
    }

    fun debugEvent(deBugAction: DebugAction, x:Float, y:Float ){
        when(deBugAction){
            DebugAction.ActionDebugTime -> {
                DebugConstant.scaleTime()
            }
            else->{}
        }
    }
}
