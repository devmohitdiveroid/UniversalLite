package com.diveroid.camera.housing

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Handler
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.view.MotionEventCompat
import com.diveroid.camera.BuildConfig
import com.diveroid.camera.housing.TouchReceiverBaseView.Bound.BOUND
import com.google.gson.Gson
import java.util.*
import kotlin.math.abs

/**
 * ojk
 * https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
 */
abstract class TouchReceiverBaseView : View {
    private val CLICK_ACTION_THRESHOLD = 200
    private var w = 0
    private var h = 0
    protected var relativeWidth = 737f // 디자인 파일의 가로길이 가정
    protected var relativeHeight = 414f
    protected var phy = 0f
    protected var phx = 0f
    protected var isPortraintMode = false
    abstract fun getBound(isPortraintMode: Boolean, phx: Float, phy: Float): Array<Bound?>
    abstract fun getHousingBtnPointManager(): HousingBtnPositioner
    private val touchManager = TouchManager(false)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initViewBound()
    }

    constructor(context: Context?) : super(context) {
        initViewBound()
    }

    fun initType(isPortraitMode: Boolean, phx: Float, phy: Float) {
        isPortraintMode = isPortraitMode
        touchManager.isPortrait = isPortraitMode
        getHousingBtnPointManager().isPortrait = isPortraitMode
        this.phx = phx
        this.phy = phy
        initViewBound()
    }

    // 디자인크기 대비 실제 X좌표( 가로위치 )
    protected fun getRelativePositionX(position: Float): Float {
        return w * (position / relativeWidth)
    }

    // 디자인크기 대비 실제 Y좌표( 세로위치 )
    protected fun getRelativePositionY(position: Float): Float {
        return h * (position / relativeHeight)
    }

    // 디자인크기 대비 실제 X크기( 가로너비 )
    protected fun getRelativeValX(position: Float): Float {
        val relativeRatio = relativeWidth / relativeHeight
        return (if (w > h * relativeRatio) h * relativeRatio * (position / relativeWidth)  else w * (position / relativeWidth))
    }

    // 디자인크기 대비 실제 Y크기( 세로너비 )
    protected fun getRelativeValY(position: Float): Float {
        val relativeRatio = relativeHeight / relativeWidth
        return (if (h > w * relativeRatio) w * relativeRatio * (position / relativeHeight) else h * (position / relativeHeight))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    class Bound(var bound: BOUND, var rect: RectF) {
        enum class BOUND {
            TOP, MIDDLE, BOTTOM, NONE, DEBUG_DEPTH_CHANGE, DEBUG_SCALE_TIME
        }
    }

    lateinit var bounds: Array<Bound>
    private fun initViewBound() {
        val bounds : ArrayList<Bound> = ArrayList(listOf(*getBound(isPortraintMode, phx, phy)))
        if (!isPortraintMode) {
            if(BuildConfig.DEBUG) {
                //add Debug Bound
                Collections.addAll(bounds,
                    Bound(BOUND.DEBUG_DEPTH_CHANGE, RectF(getRelativePositionX(360f), getRelativePositionY(98f), getRelativePositionX(360f + 55f), getRelativePositionY(98f + 60 * 4))),
                    Bound(BOUND.DEBUG_SCALE_TIME, RectF(getRelativePositionX(23f), getRelativePositionY(25f), getRelativePositionX((23 + 138).toFloat()), getRelativePositionY((25 + 43).toFloat())))
                )
            }
        }

        // Close Button 추가
        this.bounds = bounds.toTypedArray()
        Arrays.fill(btnType, Action.None)

        this.bounds.forEach {
            Log.d("csson", "initViewBound: bound = ${it.bound.name}, rect = ${it.rect.toShortString()}")
        }
    }

    private fun getBoundType(x: Float, y: Float): BOUND? {
        var bound: BOUND? = null
        for (b in bounds) {
            if (b.rect.contains(x, y)) {
                bound = b.bound
                break
            }
        }
        return bound
    }

    private val MAX_TOUCH_NUM = 10
    var startX = FloatArray(MAX_TOUCH_NUM)
    var startY = FloatArray(MAX_TOUCH_NUM)
    var downTime = LongArray(MAX_TOUCH_NUM)
    var eventBound = arrayOfNulls<BOUND>(MAX_TOUCH_NUM)
    var isCtrlTarget = BooleanArray(MAX_TOUCH_NUM)
    var btnType = arrayOfNulls<Action>(MAX_TOUCH_NUM)
    val longClickHandler = Handler()
    private val longClickRunnable = Runnable {
        for (tId in 0 until MAX_TOUCH_NUM) {
            if (isCtrlTarget[tId]) {
                /* 엠펙의 특별한 경우가 아니면 롱클릭 되고 있는 버튼들은 모두 무시하게 되어 있다.
                    왜냐하면 엠펙 하우징 자체가 물의 압력에 의해서 자동적으로 화면이 눌릴 수 있기 때문이다.
                    사용자가 의도하지 않은 롱클릭이 무분별하게 일어나기 떄문에 모든 롱클릭을 막아둔다.
                 */
                isCtrlTarget[tId] = false
            } else continue

            if (eventBound[tId] == BOUND.TOP) {
                btnTypePostCtrl(startX[tId], startY[tId], Action.Btn1Long, tId)
            }
            if (eventBound[tId] == BOUND.MIDDLE) {
                btnTypePostCtrl(startX[tId], startY[tId], Action.Btn2Long, tId)
            }
            if (eventBound[tId] == BOUND.BOTTOM) {
                btnTypePostCtrl(startX[tId], startY[tId], Action.Btn3Long, tId)
            }
        }
    }

    private fun checkLongPressEvent(index: Int): Boolean {
        val longPressTimeOut = ViewConfiguration.getLongPressTimeout()
        return SystemClock.uptimeMillis() - downTime[index] >= longPressTimeOut
    }

    private fun btnTypePostCtrl(x: Float, y: Float, type: Action, btnIndex: Int) {
        var action = type
        updateBtnType(type, btnIndex)
        val set: Set<*> = HashSet(listOf(*btnType))
        if (set.contains(Action.Btn1Long) && set.contains(Action.Btn2Long) && set.contains(Action.Btn3Long)) {
            action = Action.Btn123Long
        } else if (set.contains(Action.Btn1Long) && set.contains(Action.Btn2Long) && !set.contains(
                Action.Btn1Down)) {
            action = Action.Btn12Long
        } else if (set.contains(Action.Btn2Long) && set.contains(Action.Btn3Long) && !set.contains(
                Action.Btn2Down)) {
            action = Action.Btn23Long
        } else if (set.contains(Action.Btn3Long) && set.contains(Action.Btn1Long) && !set.contains(
                Action.Btn3Down)) {
            action = Action.Btn13Long
        } else if (action === Action.Btn1Long && (set.contains(Action.Btn2Down) || set.contains(Action.Btn3Down)) ||
                action === Action.Btn2Long && (set.contains(Action.Btn1Down) || set.contains(Action.Btn3Down)) ||
                action === Action.Btn3Long && (set.contains(Action.Btn1Down) || set.contains(Action.Btn2Down))) {
            action = Action.None
        } else if (set.contains(Action.Btn1Down) && set.contains(Action.Btn2Down) && !set.contains(
                Action.Btn3Down)) {
            action = Action.Btn12Down
        } else if (set.contains(Action.Btn2Down) && set.contains(Action.Btn3Down) && !set.contains(
                Action.Btn1Down)) {
            action = Action.Btn23Down
        } else if (set.contains(Action.Btn1Up) && set.contains(Action.Btn3Up) && !set.contains(
                Action.Btn2Down)) {
            action = if (checkLongPressEvent(btnIndex)) Action.None else Action.Btn13Down
        } else if (set.contains(Action.Btn1Down) && set.contains(Action.Btn2Down) && set.contains(
                Action.Btn3Down)) {
            action = Action.Btn123Down
        }

//        if(isMultiTouch) {
//            return
//        }

        Log.d("csson", "btnTypePostCtrl: action $action")


        when (action) {
//            Action.Btn1Up -> touchManager.activeEvent(Action.Btn1Up, x, y)
//            Action.Btn1Down -> touchManager.activeEvent(Action.Btn1Down, x, y)
            Action.Btn1Click -> touchManager.activeEvent(Action.Btn1Click, x, y)
            Action.Btn1Long -> touchManager.activeEvent(Action.Btn1Long, x, y)
//            Action.Btn2Up -> touchManager.activeEvent(Action.Btn2Up, x, y)
//            Action.Btn2Down -> touchManager.activeEvent(Action.Btn2Down, x, y)
            Action.Btn2Click -> touchManager.activeEvent(Action.Btn2Click, x, y)
            Action.Btn2Long -> touchManager.activeEvent(Action.Btn2Long, x, y)
//            Action.Btn3Up -> touchManager.activeEvent(Action.Btn3Up, x, y)
//            Action.Btn3Down -> touchManager.activeEvent(Action.Btn3Down, x, y)
            Action.Btn3Click -> touchManager.activeEvent(Action.Btn3Click, x, y)
            Action.Btn3Long -> touchManager.activeEvent(Action.Btn3Long, x, y)
            Action.Btn12Long -> touchManager.activeEvent(Action.Btn12Long, x, y)
            Action.Btn23Long -> touchManager.activeEvent(Action.Btn23Long, x, y)
            Action.Btn13Long -> touchManager.activeEvent(Action.Btn13Long, x, y)
            Action.Btn123Long -> touchManager.activeEvent(Action.Btn123Long, x, y)
            Action.Btn12Down -> touchManager.activeEvent(Action.Btn12Down, x, y)
            Action.Btn23Down -> touchManager.activeEvent(Action.Btn23Down, x, y)
            Action.Btn13Down -> touchManager.activeEvent(Action.Btn13Down, x, y)
            Action.Btn123Down -> touchManager.activeEvent(Action.Btn123Down, x, y)
            Action.None -> {
            }

            else -> {

            }
        }
    }

    private fun updateBtnType(type: Action, btnIndex: Int) {
        if (!type.name.contains("Click")) btnType[btnIndex] = type
    }

    private var isMultiTouch: Boolean = false

    /**
     *
     *
     * 다중터치를 받아온다. 터치의 ID를 가지고 downTime, eventBound( 각터치의 event종류 ), isCtrlTarget( 처리유무 ) 등등을 저장하고 처리한다.
     *
     * @param event
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("csson", "onTouchEvent: $event")
        val tIndex = event.actionIndex //(event.getActionIndex() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        val tId = event.getPointerId(tIndex)
        val curX = event.getX(tIndex)
        val curY = event.getY(tIndex)
        if (tId > MAX_TOUCH_NUM) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                startX[tId] = curX
                startY[tId] = curY
                downTime[tId] = event.eventTime
                eventBound[tId] = getBoundType(curX, curY)
                isCtrlTarget[tId] = eventBound[tId] != null && controlTouch(eventBound[tId], false, event.action, curX, curX, curY, curY, tId)
                longClickHandler.postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                isMultiTouch = event.pointerCount > 1
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                val ctrlTarget = if (isMultiTouch) false else isCtrlTarget[tId]
                if (controlTouch(eventBound[tId], ctrlTarget, event.action, startX[tId], curX, startY[tId], curY, tId)) {
                    longClickHandler.removeCallbacks(longClickRunnable)
                    initializingTouchPointer()
                }
            }
            MotionEvent.ACTION_CANCEL -> isCtrlTarget[tId] = false
        }

        buttonEventLog()

        return true
    }

    private fun buttonEventLog() {
        Log.d("csson", "buttonEventLog: startX = ${startX.asList()}")
        Log.d("csson", "buttonEventLog: startY = ${startY.asList()}")
        Log.d("csson", "buttonEventLog: downTime = ${downTime.asList()}")
        Log.d("csson", "buttonEventLog: eventBound = ${eventBound.asList()}")
        Log.d("csson", "buttonEventLog: isCtrlTarget = ${isCtrlTarget.asList()}")
        Log.d("csson", "buttonEventLog: btnType = ${btnType.asList()}")
        Log.d("csson", "buttonEventLog: isMultiTouch = $isMultiTouch")
    }

    private fun initializingTouchPointer() {
        for (i in 0 until MAX_TOUCH_NUM) isCtrlTarget[i] = false
    }

    private fun postBtnEvent(btnNumber: Int, action: Int, x: Float, y: Float, buttonIndex: Int) {
        when (btnNumber) {
            1 -> when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> btnTypePostCtrl(x, y, Action.Btn1Up, buttonIndex)
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> btnTypePostCtrl(x, y, Action.Btn1Down, buttonIndex)
            }
            2 -> when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> btnTypePostCtrl(x, y, Action.Btn2Up, buttonIndex)
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> btnTypePostCtrl(x, y, Action.Btn2Down, buttonIndex)
            }
            3 -> when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> btnTypePostCtrl(x, y, Action.Btn3Up, buttonIndex)
                MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> btnTypePostCtrl(x, y, Action.Btn3Down, buttonIndex)
            }
        }
    }

    private fun controlTouch(type: BOUND?, isNeedAction: Boolean, action: Int, startX: Float, endX: Float, startY: Float, endY: Float, btnIndex: Int): Boolean {
        var isControlTarget = true
        if (isAClick(startX, endX, startY, endY)) {
            if (type == BOUND.TOP) {
                getHousingBtnPointManager().setPositionFrom(startX, startY, w - startX, h - startY, Action.Btn1Click)
                if (isNeedAction) {
                    touchManager.activeEvent(Action.Btn1Click, startX, startY)
                } else {
                    postBtnEvent(1, action, startX, startY, btnIndex)
                }
            } else if (type == BOUND.MIDDLE) {
                getHousingBtnPointManager().setPositionFrom(startX, startY, w - startX, h - startY, Action.Btn2Click)
                postBtnEvent(2, action, startX, startY, btnIndex)
                if (isNeedAction) {
                    touchManager.activeEvent(Action.Btn2Click, startX, startY)
                } else {
                    postBtnEvent(1, action, startX, startY, btnIndex)
                }
            } else if (type == BOUND.BOTTOM) {
                getHousingBtnPointManager().setPositionFrom(startX, startY, w - startX, h - startY, Action.Btn3Click)
                if (isNeedAction) {
                    touchManager.activeEvent(Action.Btn3Click, startX, startY)
                } else {
                    postBtnEvent(3, action, startX, startY, btnIndex)
                }
            } else {
                isControlTarget = false
            }
        }
        return isControlTarget
    }

    private fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        return differenceX <= CLICK_ACTION_THRESHOLD /* =5 */ && differenceY <= CLICK_ACTION_THRESHOLD
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        if (w != 0 && h != 0) {
            initViewBound()
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }
}