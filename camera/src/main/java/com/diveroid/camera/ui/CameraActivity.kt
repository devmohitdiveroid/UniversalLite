package com.diveroid.camera.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.ThumbnailUtils
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.diveroid.camera.GL2PreviewOptionLite
import com.diveroid.camera.R
import com.diveroid.camera.databinding.ActivityCameraBinding
import com.diveroid.camera.housing.Action
import com.diveroid.camera.housing.Event
import com.diveroid.camera.housing.TouchMainCenter
import com.diveroid.camera.mediaCtrl.MediaFileControl
import com.diveroid.camera.ui.view.CameraGuideView
import com.diveroid.camera.underfilter.FileUtils2
import com.diveroid.camera.underfilter.GL2JNICamera2
import com.diveroid.camera.underfilter.GL2JNILib
import com.diveroid.camera.underfilter.GL2JNIView
import com.diveroid.camera.utils.BitmapUtils
import com.diveroid.camera.utils.FlashlightUtil
import com.diveroid.camera.utils.Util
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


abstract class CameraActivity : ComponentActivity() {
    private val TAG = this::class.java.simpleName

    private lateinit var binding: ActivityCameraBinding

    protected var surfaceView: GL2JNIView? = null
    private val option: GL2PreviewOptionLite
        get() = GL2PreviewOptionLite

    private val batteryAndTimeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent != null) {
                when(intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                        setBatteryPercent(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
                    }
                    Intent.ACTION_TIME_TICK -> {
                        setRealTime()
                    }
                }
            }
        }
    }

    private var runningStartTime: Long = 0L
    private val runningTimeTask: TimerTask = object: TimerTask() {
        override fun run() {
            val diffVal = (System.currentTimeMillis() - runningStartTime) / 1000
            val minute = diffVal / 60
            val second = diffVal % 60
            runOnUiThread {
//                binding.txtRuntimeValue.text = if(diffVal >= 60) {
//                    String.format("%02d'%02d''", minute , second)
//                } else {
//                    String.format("%02d''", second)
//                }
                //분초로 나누어서..
                binding.txtRuntimeValue.text = String.format("%d'", minute.toInt())
                binding.txtRuntimeValue2.text = String.format("%02d", second.toInt())
            }
        }
    }

    private val runningTimer: Timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  DataBindingUtil.setContentView(this, R.layout.activity_camera)
        binding.lifecycleOwner = this
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        actionBar?.show()

        // 전체화면 모드 사용
        hideSystemUI()

        binding.root.setBackgroundColor(Color.DKGRAY)

        // 카메라 Surface 설정
        addSurfaceView()

        binding.cameraMenuLayer.apply {
            initMenu(option)
            selectMenu(option.currentSelect)
        }
        binding.btnClose.setOnClickListener {
            surfaceView?.let {
                if(it.isRecording) {
                    it.stopRecord()
                    setRecordingView(false)
                    return@setOnClickListener
                } else {
                    onBackPressed()
                }
            }
        }

        surfaceView?.let {
            binding.filterModeView.setMode(it.isOnFilterThread)
        }

        initObserves()

        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        setBatteryPercent(bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY))
        setRealTime()
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_TIME_TICK)
        }
        registerReceiver(batteryAndTimeReceiver, intentFilter)

        // 가이드뷰
        binding.guideView.apply {
            isVisible = enableGuideView()
            if(enableGuideView()) {
                setOnExitListener(object: CameraGuideView.OnExitListener {
                    override fun onExit() {
                        setEnableGuideView(false)
                    }
                })
            }
        }

        // Diving Start Time Check
        runningStartTime = System.currentTimeMillis()
        runningTimer.schedule(runningTimeTask, 0, 500)

        GL2JNICamera2.setOnImageSaverListener(object: GL2JNICamera2.OnImageSaverListener {
            override fun onImageSaveCompleted(filePath: String, thumbPath: String) {
                savePicture(filePath, thumbPath)
            }

            override fun onImageSaveFailed(msg: String) {
                Toast.makeText(
                    this@CameraActivity,
                    "${resources.getString(R.string.diving_camera_picture_failed)}($msg)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        initFocusAnimationHandler()

        startDive(runningStartTime)

        //leess 20221014 초광각-광각 순서변경
        binding.cameraModeView.setCameraMode(option.currentSelect)
    }

    private fun nextCameraMode() {
        val nextCameraMode = option.nextOptionIndex()
        binding.cameraMenuLayer.selectMenu(nextCameraMode).run {
            option.currentSelect = nextCameraMode
        }
    }

    private fun applyCameraMode() {
        // 카메라뷰 모드 변경
        surfaceView?.changeCameraMode(option.currentSelect).run {
            // 좌측 하단 카메라모드 아이콘 변경
            binding.cameraModeView.setCameraMode(option.currentSelect)
            // 모드 메뉴 레이어 숨김
            binding.cameraMenuLayer.hideLayer()
        }
        // 셀피모드일 경우, 플래시를 사용하지 못하기 때문에 flashModeView 를 강제로 Off 처리
        if(GL2JNICamera2.mIsSelfie) {
            binding.flashModeView.setMode(false)
        }
    }

    private fun initObserves() {
        TouchMainCenter.getInstance().buttonEventOb.observe(this, Observer { it ->
            Log.d(TAG, "initObserves: ${Gson().toJson(it)}")
            if(it.isDone) return@Observer


            if (enableGuideView()) { // 가이드(메뉴얼)뷰가 있는 경우 가이드 다음페이지로 이동 처리
                when (it.action) {
                    Action.Btn1Click,
                    Action.Btn2Click,
                    Action.Btn3Click -> {
                        binding.guideView.nextPage()
                    }
                    else -> {   // 아무런 동작하지 않음.
                    }
                }
                return@Observer
            }

            when (it.action) {
                Action.Btn1Click -> {   // 촬영모드 설정
                    Log.d(TAG, "initObserves: Btn1Click")
                    if(surfaceView?.isOpenCamera() == false) {
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                        return@Observer
                    }

                    if(binding.cameraMenuLayer.isVisible) {     // 카메라모드메뉴가 보여지고 있을 경우
                        nextCameraMode()
                    } else {
                        binding.cameraMenuLayer.showLayer()
                    }
                }
                Action.Btn1Long -> { // 절전모드 On/Off
                    Log.d(TAG, "initObserves: Btn1Long")
                    if (surfaceView?.isOpenCamera() == true) {
                        if (surfaceView?.isRecording == true) {
                            Toast.makeText(
                                this,
                                getString(R.string.diving_camera_eco_mode_failed_recording),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Observer
                        }

                        binding.cameraMenuLayer.hideLayer()

                        Toast.makeText(
                            this,
                            getString(R.string.diving_camera_eco_mode_on),
                            Toast.LENGTH_SHORT
                        ).show()
                        surfaceView?.stopCamera()
                        binding.txtEcoMode.isVisible = true

                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.diving_camera_eco_mode_off),
                            Toast.LENGTH_SHORT
                        ).show()
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                    }
                }
                Action.Btn2Click -> {   // 사진 촬영
                    if(surfaceView?.isOpenCamera() == false) {
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                        return@Observer
                    }

                    if(binding.cameraMenuLayer.isVisible) {
                        applyCameraMode()
                        return@Observer
                    }

                    if(surfaceView?.isRecording == true) {
                        surfaceView?.stopRecord()
                        setRecordingView(false)
                        return@Observer
                    }
                    if(option.currentSelect == GL2PreviewOptionLite.OPTION_SELFIE) {
                        showSelfieCaptureEffect()
                    } else {
                        surfaceView?.captureImage()
                        showCameraEffect()
                    }

                }
                Action.Btn2Long -> {    // 동영상 촬영
                    if(surfaceView?.isOpenCamera() == false) {
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                        return@Observer
                    }
                    surfaceView?.let { view ->
                        if(view.isRecording) {
                            view.stopRecord()
                        } else {
                            view.startRecord()
                        }
                        setRecordingView(view.isRecording)
                    }
                }
                Action.Btn3Click -> {   // 자동초점
                    Log.d(TAG, "initObserves: Btn3Click")
                    if(surfaceView?.isOpenCamera() == false) {
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                        return@Observer
                    }

                    GL2JNICamera2.autoFocus().run {
                        focusAnimation()
                    }
                }
                Action.Btn3Long -> {    // 실시간 보정 On/Off
                    if(surfaceView?.isOpenCamera() == false) {
                        surfaceView?.startCamera()
                        binding.txtEcoMode.isVisible = false
                        return@Observer
                    }
                    surfaceView?.let {
                        if(it.isOnFilterThread) {
                            it.offFilterThread()
                        } else {
                            it.onFilterThread()

                        }
                        // 보정 설정값에 따른 UI 업데이트(필터 아이콘 On/Off)
                        binding.filterModeView.setMode(it.isOnFilterThread)
                    }
                }
                Action.Btn13Down -> {
                    Log.d(TAG, "initObserves: Btn13Down")
                    /*  2022-09-20 아티슨앤오션에서 기능 제거 요청
                    // 플래시 On/Off
                    if(GL2JNICamera2.mIsSelfie) {
                        Toast.makeText(
                            this,
                            getString(R.string.diving_camera_flash_failed_selfie),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Observer
                    }
                    flashOnOff()
                     */

                    /* 2022-10-28 아티슨앤오션에서 수정모드 추가 요청 */
                    surfaceView?.let {
                        if(it.isUnderwaterModeOn) {
                            it.underwaterModeOff()
                        } else {
                            it.underwaterModeOn()
                        }
                        binding.underwaterModeView.setMode(it.isUnderwaterModeOn)
                        if(it.isUnderwaterModeOn) {
                            Toast.makeText(this, getString(R.string.diving_camera_underwater_mode_on), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, getString(R.string.diving_camera_underwater_mode_off), Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@Observer
                }
                Action.Btn13Long -> {
                    /* 기능 삭제
                    Log.d(TAG, "initObserves: Btn13Long")
                    if (!enabledSendLocationSMS()) {
                        Toast.makeText(
                            this,
                            getString(R.string.diving_location_send_failed_disabled),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Observer
                    }
                    // 위치전송
                    sendLocationSMS(getString(R.string.diving_location_template))
                     */
                }
                else -> {}
            }
        })
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController = WindowCompat.getInsetsController(window, binding.layCamera)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.systemGestures())

        // TODO: 상태바까지 포함되도록 작업 필요. 삼성 카메라 앱은 상태바까지 차지함.
    }


    private fun setRecordingView(isRecording: Boolean) {
        if(isRecording) {
            binding.recordingIndicator.startRecord()
        } else {
            binding.recordingIndicator.stopRecord()
        }
    }

    private fun flashOnOff() {
        if(!FlashlightUtil.hasFlashlight(this)) {
            Toast.makeText(
                this,
                getString(R.string.diving_camera_flash_failed_not_support),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        surfaceView?.let {
            if(it.isFlashOn) {
                it.flashOff()
            } else {
                it.flashOn()
            }
            binding.flashModeView.setMode(it.isFlashOn)
        }
    }

    override fun onPause() {
        super.onPause()
        surfaceView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        surfaceView?.onResume()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // observe 할 때 LiveData에 할당된 값이 있으면 그 값을 전달해주기 때문에
        // 버튼 이벤트가 발생하는 것을 방지하기 위해 화면이 닫힐 때 none Action 값을 할당.
        // FIXME: 다른 방법을 찾는게 좋을 것으로 판단
        TouchMainCenter.getInstance().buttonEventOb.value = Event(false, Action.None, 0f, 0f, false)

        surfaceView?.stopCamera()
        surfaceView = null

        GL2JNILib.onDestroy()

        try {
            unregisterReceiver(batteryAndTimeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Timer
        runningTimer.cancel()

        // End Dive
        endDive(System.currentTimeMillis())
    }


    private fun addSurfaceView() {
        option.apply {
            isScubaOrSnorkelMode = true
            isWideAvailable = GL2JNICamera2.hasBackspaceWideCamera(this@CameraActivity)
        }

        val layoutParams = binding.layCamera.layoutParams as ConstraintLayout.LayoutParams
        when(option.SCREEN_RATIO) {
            Util.CameraRatio.RATIO_4_3 -> layoutParams.dimensionRatio = "W,4:3"
            Util.CameraRatio.RATIO_16_9 -> layoutParams.dimensionRatio = "W,16:9"
            else -> {

            }
        }

        binding.layCamera.layoutParams = layoutParams

        surfaceView = GL2JNIView(this, option).apply {
            this.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )

            setGL2JNILibCallback(object: GL2JNILib.Callback() {
                override fun GL2JNIRealtimeRecordStoped(savedFile: String?) {
                    super.GL2JNIRealtimeRecordStoped(savedFile)
                    if(!savedFile.isNullOrEmpty()) {
                        val thumbPath = saveVideoThumbNail(savedFile)
                        saveVideo(savedFile, thumbPath)
                    }
                }
            })
//            onFilterThread()
        }

        binding.layCamera.addView(surfaceView, 0)
        binding.txtEcoMode.isVisible = true
    }

    private fun setBatteryPercent(percent: Int) {
        val lp = binding.batteryView.batteryPercent.layoutParams as LinearLayout.LayoutParams
        lp.weight = percent / 100f
        binding.batteryView.batteryPercent.layoutParams = lp
    }

    private fun setRealTime() {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeStr = formatter.format(Date(System.currentTimeMillis()))
        binding.txtTime.text = timeStr
    }

    private fun saveVideoThumbNail(savedFile: String?): String {
        if (savedFile.isNullOrEmpty()) return ""

        val file = File(savedFile)
        val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val size = FileUtils2.getVideoSize(savedFile)
            ThumbnailUtils.createVideoThumbnail(file, size, null)
        } else {
            ThumbnailUtils.createVideoThumbnail(savedFile, MediaStore.Video.Thumbnails.MINI_KIND)
        }

        return if (thumbnail != null) {
            val thumbnailPath = MediaFileControl.getLocalMediaPath(file.name, true)!!
            val resizedBitmap =
                BitmapUtils.getResizedBitmap(thumbnail, MediaFileControl.SIZE_THUMBNAIL_X)
            BitmapUtils.saveBitmap(
                thumbnailPath,
                resizedBitmap
            )
            thumbnail.recycle()
            resizedBitmap.recycle()

            thumbnailPath
        } else {
            ""
        }
    }

    /**
     * 다이빙(스노쿨링)이 시작할 때 호출
     */
    abstract fun startDive(startTimeMillis: Long)

    /**
     *  다이빙(스노쿨링)이 종료될 때 호출됨
     */
    abstract fun endDive(endTimeMillis: Long)

    abstract fun enabledSendLocationSMS() : Boolean

    /**
     *  위치
     */
    abstract fun sendLocationSMS(msg: String)

    /**
     *
     */
    abstract fun savePicture(filePath: String ,thumbPath: String)

    /**
     *
     */
    abstract fun saveVideo(filePath: String, thumbPath: String)

    abstract fun enableGuideView(): Boolean
    abstract fun setEnableGuideView(isExit: Boolean)

    internal fun initFocusAnimationHandler() {
        startOuterRadius = 80f
        startInnerRadius = 0f
        focusAnimationRatio = 600f / 30f // 30 frame
        targetRadius = (startOuterRadius - startInnerRadius) / 2f
        radiusFactor = targetRadius / focusAnimationRatio
        outerRadius = 0f
        innerRadius = 0f

        focusAnimationRunnable = object : Runnable {
            override fun run() {
                innerRadius += radiusFactor
                outerRadius -= radiusFactor

                if (innerRadius >= targetRadius || outerRadius <= targetRadius) {
                    innerRadius = 0f
                    outerRadius = 0f
                } else {
                    focusAnimationHandler!!.postDelayed(this, focusAnimationRatio.toLong())
                }
                binding.animationView.setInnerRadius(innerRadius)
                binding.animationView.setOuterRadius(outerRadius)
                binding.animationView.invalidate()
            }
        }
    }

    internal var focusAnimationHandler: Handler? = null
    internal var startOuterRadius: Float = 0.toFloat()
    internal var startInnerRadius:Float = 0.toFloat()
    internal var focusAnimationRatio:Float = 0.toFloat() // 30 frame
    internal var targetRadius: Float = 0.toFloat()
    internal var radiusFactor:Float = 0.toFloat()
    internal var outerRadius = 0f
    internal var innerRadius = 0f

    internal var focusAnimationRunnable: Runnable? = null

    fun focusAnimation() {
        if (focusAnimationHandler == null)
            focusAnimationHandler = Handler()
        else
            focusAnimationHandler!!.removeCallbacks(focusAnimationRunnable!!)

        innerRadius = startInnerRadius
        outerRadius = startOuterRadius

        focusAnimationHandler!!.post(focusAnimationRunnable!!)
    }

    var mPhotoEffectmiliSecond: Long = 0
    var mPhotoEffectHandler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val currentMili = System.currentTimeMillis()
            var diffMili = currentMili - mPhotoEffectmiliSecond
            if (diffMili > 150) {
                diffMili = 150
            }
            if (diffMili < 0) {
                diffMili = 0
            }
            if (diffMili > 50) {
                binding.viewEffect.alpha = 1.0f - (diffMili - 50).toFloat() / 100.0f
                binding.viewEffect.invalidate()
            }
            if (currentMili - mPhotoEffectmiliSecond > 50) {
                binding.viewEffect.alpha = 1.0f
                binding.viewEffect.visibility = View.INVISIBLE
                binding.viewEffect.invalidate()
                return
            }
            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            this.sendEmptyMessageDelayed(0, 10)
        }
    }

    fun showCameraEffect() {
        mPhotoEffectmiliSecond = System.currentTimeMillis()
        binding.viewEffect.visibility = View.VISIBLE
        binding.viewEffect.alpha = 1.0f
        binding.viewEffect.invalidate()
        mPhotoEffectHandler!!.sendEmptyMessageDelayed(0, 0)
    }

    var mSelfiePhotoEffectmiliSecond: Long = 0
    var bfinishSelpieCapture = false
    var bfinishBlackView = false
    var mPhotoSelfieEffectHandler: Handler? = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val currentMili = System.currentTimeMillis()
            var diffMili = currentMili - mSelfiePhotoEffectmiliSecond
            if (diffMili > 1000) {
                diffMili = 1000
            }
            if (diffMili < 0) {
                diffMili = 0
            }
            if (bfinishBlackView && !bfinishSelpieCapture) {
                surfaceView?.captureImage()
                bfinishSelpieCapture = true
                Log.d("SelfieCamera", "CaptureCamera")
            }
            if (currentMili - mSelfiePhotoEffectmiliSecond > 500 && bfinishSelpieCapture) //if( bfinishBlackView)
            {
                //CaptureCamera();
                bfinishBlackView = false
                binding.viewEffect.alpha = 1.0f
                binding.viewEffect.visibility = View.INVISIBLE
                binding.viewEffect.invalidate()
                Log.d("SelfieCamera", "end")
                return
            }

            // 메세지를 처리하고 또다시 핸들러에 메세지 전달 (1000ms 지연)
            if (!bfinishSelpieCapture) {
                this.sendEmptyMessageDelayed(0, 400)
                bfinishBlackView = true
            } else {
                this.sendEmptyMessageDelayed(0, 10)
            }
        }
    }

    fun showSelfieCaptureEffect() {
        bfinishSelpieCapture = false
        bfinishBlackView = false
        mSelfiePhotoEffectmiliSecond = System.currentTimeMillis()
        binding.viewEffect.visibility = View.VISIBLE
        binding.viewEffect.alpha = 1.0f
        binding.viewEffect.invalidate()
        mPhotoSelfieEffectHandler!!.sendEmptyMessageDelayed(0, 0)
    }
}
