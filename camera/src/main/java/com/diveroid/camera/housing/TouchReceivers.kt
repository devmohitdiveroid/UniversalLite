package com.diveroid.camera.housing

import android.content.Context
import android.graphics.RectF
import com.diveroid.camera.housing.*

/**
 * ojk
 * https://www.notion.so/Diving-View-c0c2475c748a4154a112b41a23920387
 */

class UniversalTouchReceiver(context:  Context) : TouchReceiverBaseView(context) {
    override fun getBound(isPortraintMode: Boolean, phx: Float, phy: Float): Array<Bound?> {
        return if (isPortraintMode) {
            val width = getRelativePositionX(relativeWidth)
            arrayOf(
                    Bound(Bound.BOUND.TOP, RectF(4.5f / phx * width, getRelativePositionY(relativeHeight * 3 / 4), width, getRelativePositionY(relativeHeight))),
                    Bound(Bound.BOUND.MIDDLE, RectF(2.6f / phx * width, getRelativePositionY(relativeHeight * 3 / 4), 4.5f / phx * width, getRelativePositionY(relativeHeight))),
                    Bound(Bound.BOUND.BOTTOM, RectF(0f, getRelativePositionY(relativeHeight * 3 / 4), 2.6f / phx * width, getRelativePositionY(relativeHeight))))
        } else {
            val height = getRelativePositionY(relativeHeight)
            arrayOf(Bound(Bound.BOUND.TOP, RectF(getRelativePositionX(relativeWidth) * 3 / 4, 0f, getRelativePositionX(relativeWidth), (phy - 4.5f) / phy * height)),
                    Bound(Bound.BOUND.MIDDLE, RectF(getRelativePositionX(relativeWidth) * 3 / 4, (phy - 4.5f) / phy * height, getRelativePositionX(relativeWidth), (phy - 2.6f) / phy * height)),
                    Bound(Bound.BOUND.BOTTOM, RectF(getRelativePositionX(relativeWidth) * 3 / 4, (phy - 2.6f) / phy * height, getRelativePositionX(relativeWidth), getRelativePositionY(relativeHeight))))
        }
    }

    override fun getHousingBtnPointManager(): HousingBtnPositioner {
        return UniversalBtnPointManager(isPortraintMode)
    }
}

class MpacTouchReceiver(context:  Context) : TouchReceiverBaseView(context) {
    override fun getBound(isPortraintMode: Boolean, phx: Float, phy: Float): Array<Bound?> {
        return if (isPortraintMode) {
            val width = getRelativePositionX(relativeWidth)
            val height = getRelativePositionY(relativeHeight)
            arrayOf(
                    Bound(Bound.BOUND.TOP, RectF(width*2/3, height*3/4, width, height)),
                    Bound(Bound.BOUND.MIDDLE, RectF(width/3, height*3/4, width*2/3, height)),
                    Bound(Bound.BOUND.BOTTOM, RectF(0f, height*3/4, width*1/3, height)))
        } else {
            val width = getRelativePositionX(relativeWidth)
            val height = getRelativePositionY(relativeHeight)
            arrayOf(Bound(Bound.BOUND.TOP, RectF(width * 3 / 4, 0f, width, height/3)),
                    Bound(Bound.BOUND.MIDDLE, RectF(width * 3 / 4, height/3, width, height*2/3)),
                    Bound(Bound.BOUND.BOTTOM, RectF(width * 3 / 4, height*2/3, width , height)))
        }
    }

    override fun getHousingBtnPointManager(): HousingBtnPositioner {
        return MpacBtnPositioner(isPortraintMode)
    }
}

class PatimaTouchReceiver(context:  Context) : TouchReceiverBaseView(context) {
    override fun getBound(isPortraintMode: Boolean, phx: Float, phy: Float): Array<Bound?> {
        return if (isPortraintMode) {
            val width = getRelativePositionX(relativeWidth)
            arrayOf(
                    Bound(Bound.BOUND.TOP, RectF(4.835f / phx * width, getRelativePositionY(relativeHeight * 2 / 4), width, getRelativePositionY(relativeHeight))),
                    Bound(Bound.BOUND.MIDDLE, RectF(2.61f / phx * width, getRelativePositionY(relativeHeight * 2 / 4), 4.835f / phx * width, getRelativePositionY(relativeHeight))),
                    Bound(Bound.BOUND.BOTTOM, RectF(0f, getRelativePositionY(relativeHeight * 2 / 4), 2.61f / phx * width, getRelativePositionY(relativeHeight))))
        } else {
            val height = getRelativePositionY(relativeHeight)
            arrayOf(Bound(Bound.BOUND.TOP, RectF(getRelativePositionX(relativeWidth) * 2 / 4, 0f, getRelativePositionX(relativeWidth), (phy - 4.835f) / phy * height)),
                    Bound(Bound.BOUND.MIDDLE, RectF(getRelativePositionX(relativeWidth) * 2 / 4, (phy - 4.835f) / phy * height, getRelativePositionX(relativeWidth), (phy - 2.61f) / phy * height)),
                    Bound(Bound.BOUND.BOTTOM, RectF(getRelativePositionX(relativeWidth) * 2 / 4, (phy - 2.61f) / phy * height, getRelativePositionX(relativeWidth), getRelativePositionY(relativeHeight))))
        }
    }

    override fun getHousingBtnPointManager(): HousingBtnPositioner {
        return PatimaBtnPoisitioner(isPortraintMode)
    }
}

class XpoovvTouchReceiver(context:  Context) : TouchReceiverBaseView(context) {
    override fun getBound(isPortraintMode: Boolean, phx: Float, phy: Float): Array<Bound?> {
        return if (isPortraintMode) {
            val width = getRelativePositionX(relativeWidth)
            val height = getRelativePositionY(relativeHeight)
            arrayOf(
                    Bound(Bound.BOUND.TOP, RectF(width*3/4, height*2/3, width, height)),
                    Bound(Bound.BOUND.MIDDLE, RectF(width*2/4, height*2/3, width*3/4, height)),
                    Bound(Bound.BOUND.BOTTOM, RectF(width*1/4, height*2/3, width*2/4, height)))
        } else {
            val width = getRelativePositionX(relativeWidth)
            val height = getRelativePositionY(relativeHeight)
            arrayOf(Bound(Bound.BOUND.TOP, RectF(width * 2 / 3, 0f, width, height/4)),
                    Bound(Bound.BOUND.MIDDLE, RectF(width* 2 / 3, height/4, width, height*2/4)),
                    Bound(Bound.BOUND.BOTTOM, RectF(width * 2 / 3, height*2/4, width , height*3/4)))
        }
    }

    override fun getHousingBtnPointManager(): HousingBtnPositioner {
        return XpoovvBtnPositioner(isPortraintMode)
    }
}