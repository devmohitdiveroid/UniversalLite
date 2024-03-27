package com.diveroid.lite

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.diveroid.lite.native_bridge.GetSerialNumber
import com.diveroid.lite.native_bridge.ShowAuthView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlin.math.roundToInt


class SerialScanActivity : BaseActivity() {

    private var imageCapture: ImageCapture? = null
    private var cameraPreview: PreviewView? = null
    private var mostDetectedCode: String = ""
    private var mostDetectedCodeCount = 0
    private var detectedCodes: ArrayList<String> = ArrayList()
    private var finshed = false
    private var control: CameraControl? = null
    private var scanAreaViewLayout: FrameLayout? = null
    private  var serialText: TextView? = null
    private  var serialPercentText: TextView? = null
    private  var scanBorderView: View? = null
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serial_scan)

        cameraPreview = findViewById(R.id.scanCameraView)
        scanAreaViewLayout = findViewById(R.id.scanAreaViewLayout)
        serialText = findViewById(R.id.scan_serial_text)
        serialPercentText = findViewById(R.id.scan_percent_text)
        scanBorderView = findViewById(R.id.scan_border_view)
        var cancelBtn: Button? = findViewById(R.id.camera_cancel)
        cancelBtn!!.setOnClickListener {
            if (!finshed) {

                GetSerialNumber.webView!!.loadUrl("javascript:${GetSerialNumber.callback!!}('');")
                finish()
            }
        }
        var commitBtn: Button? = findViewById(R.id.camera_commit)
        commitBtn!!.setOnClickListener {
            if (!finshed) {
                finshed = true
                handler?.removeCallbacksAndMessages(null)

                var serial = serialText!!.text.toString().replace(" ", "")
                if(serial.length != 8) serial = ""
                GetSerialNumber.webView!!.loadUrl("javascript:${GetSerialNumber.callback!!}('${serial}');")
                finish()
            }
        }

        handler = Handler()
        handler?.postDelayed(object : Runnable {
            override fun run() {
                cameraPreview?.bitmap?.let {
                    checkSerial(it)
                }
                handler?.postDelayed(this, 100)
            }
        }, 0)


        startCamera()

        Handler().postDelayed(Runnable {
            control?.setZoomRatio(2.0f)
        }, 100)
    }

    override fun onBackPressed() {
        GetSerialNumber.webView!!.loadUrl("javascript:${GetSerialNumber.callback!!}('');")
        super.onBackPressed()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(cameraPreview!!.surfaceProvider)
                    }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                val camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview)
                control = camera.cameraControl

            } catch(exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(this))


    }

    fun cropCenter(bmp: Bitmap): Bitmap {

        return Bitmap.createBitmap(
                bmp,
                scanAreaViewLayout!!.x.roundToInt(),
                scanAreaViewLayout!!.y.roundToInt(),
                scanAreaViewLayout!!.width,
                scanAreaViewLayout!!.height
        )
    }

    private fun checkSerial(bmp: Bitmap) {

        val cropImage = cropCenter(bmp)
        val image = InputImage.fromBitmap(cropImage, 0)

        val recognizer = TextRecognition.getClient()
        val result = recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    checkText(visionText.text)
                }
    }

    private fun checkText(text: String) {

        var newText = text.toUpperCase()
        newText.replace("0", "O")
        val regex = "[A-Z ]*".toRegex()
        if (!newText.matches(regex)) {
            return
        } else if (newText.length != 9 || !newText.contains(" ")) {
            return
        } else if (newText.split(" ").count() != 2) {
            return
        }

        detectedCodes.add(newText)

        var count = detectedCodes.filter {
            it == newText
        }.count()

        if (mostDetectedCode == "") {
            mostDetectedCode = newText
            mostDetectedCodeCount = count
        } else {
            if (mostDetectedCode != newText) {
                if (count > mostDetectedCodeCount) {
                    mostDetectedCode = newText
                    mostDetectedCodeCount = count
                }
            } else {
                mostDetectedCode = newText
                mostDetectedCodeCount = count
            }
        }

        serialText?.text = mostDetectedCode
        setTextColor(count)
    }

    private fun setTextColor(count: Int) {
        if (finshed) { return }

        if (count > 10) {
            finshed = true
            serialText?.setTextColor(Color.GREEN)
            changeViewBorder(Color.GREEN)

            handler?.removeCallbacksAndMessages(null)

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                //완료
                var serial = serialText!!.text.toString().replace(" ", "")
                GetSerialNumber.webView!!.loadUrl("javascript:${GetSerialNumber.callback!!}('${serial}');")
                finish()
            }, 800)
        } else if (count > 5) {
            serialText?.setTextColor(Color.YELLOW)
            changeViewBorder(Color.YELLOW)
        } else {
            serialText?.setTextColor(Color.WHITE)
            changeViewBorder(Color.WHITE)
            serialPercentText?.alpha = 1f
        }

        var percentage = count.toDouble() / 11.0 * 100.0
        if (percentage > 100.0) { percentage = 100.0 }
        serialPercentText?.text = "${percentage.roundToInt()}%"
    }

    private fun changeViewBorder(color: Int) {
        val shapedrawable = ShapeDrawable()
        shapedrawable.shape = RectShape()
        shapedrawable.paint.color = color
        shapedrawable.paint.strokeWidth = 20f
        shapedrawable.paint.style = Paint.Style.STROKE
        scanBorderView?.setBackground(shapedrawable)
    }


}

