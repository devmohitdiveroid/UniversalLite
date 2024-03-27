package com.diveroid.camera.housing

import androidx.lifecycle.MutableLiveData

/*
* https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
* */
class Event(
    var isPortrait : Boolean,
    var action : Action,
    var x : Float,
    var y : Float,
    var isDone : Boolean = false
)

enum class Action {
    /**
     *
     */
    None,

    /**
     * 버튼1에서
     */
    Btn1Up, Btn1Down, Btn1Click, Btn1Long,

    /**
     *
     */
    Btn2Up, Btn2Down, Btn2Click, Btn2Long,

    /**
     *
     */
    Btn3Up, Btn3Down, Btn3Click, Btn3Long,

    /**
     *  버튼1, 버튼2를 동시에 길게 누르는 동작
     */
    Btn12Long,

    /**
     * 버튼2, 버튼3을 동시에 길게 누르는 동작
     */
    Btn23Long,

    /**
     *  버튼1, 버튼3을 동시에 길게 누르는 동작
     */
    Btn13Long,

    /**
     * 버튼1, 버튼2, 버튼3을 동시에 길게 누르는 동작
     */
    Btn123Long,

    /**
     * 버튼1, 버튼2를 누르는 동작
     */
    Btn12Down,

    /**
     * 버튼1, 버튼3를 누르는 동작
     */
    Btn13Down,

    /**
     * 버튼2, 버튼3를 누르는 동작
     */
    Btn23Down,

    /**
     * 버튼1, 버튼2, 버튼3을 누르는 동작
     */
    Btn123Down
}

class DebugEvent(
    val DebugAction : DebugAction,
    val value : Any
)
enum class DebugAction{
    ActionDebugTime
}

class TouchMainCenter {
    companion object {
        @Volatile private var instance: TouchMainCenter? = null
        @JvmStatic fun getInstance(): TouchMainCenter =
                instance ?: synchronized(this) {
                    instance ?: TouchMainCenter().also {
                        instance = it
                    }
                }
    }

    val buttonEventOb = MutableLiveData<Event>()
    val debugEventOb = MutableLiveData<DebugEvent>()
    val btnPositionOb = MutableLiveData<HousingBtnPositioner>()
}