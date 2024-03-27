package com.diveroid.lite

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.diveroid.lite.native_bridge.GetSerialNumber
import com.diveroid.camera.filter.RetouchResultActivity
import com.diveroid.lite.native_bridge.StartDive
import com.diveroid.lite.util.AppUtil
import com.diveroid.lite.util.PrefUtil
import com.diveroid.lite.util.SqliteUtil


class MainActivity : BaseActivity() {

    var back = 0L
    var url = mutableStateOf(Const.HYBRID_WEB_LINK)
    var webView: WebView? = null
    var hideIntro by mutableStateOf(false)

    val REQUEST_CONTACT = 100001
    val REQUEST_SELECT_FILE = 100002

    private var uploadMessage: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true) // 크롬 웹뷰 디버깅

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        }

        mActivity = this
        var versionCode = 0
        var versionName = ""
        var statusBarHeight = getStatusBarHeight()
        try {
            val pInfo = mActivity.packageManager.getPackageInfo(packageName, 0)
            versionCode = pInfo.versionCode
            versionName = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
        }

        //데이터베이스
        //SqliteUtil.getInstance(this).dropAllTable()
        SqliteUtil.getInstance(this).createTable()
        //테스트
//        var query = String.format("insert into TB_LOG (userId, startDate) values ('onlytree', '%s')", DateUtil.toString(Date(), "yyyyMMddHHmmss"))
//        LogUtil.log("onlytree", SqliteUtil.getInstance(this).runQuery(query))
//        var querys = mutableListOf<String>()
//        for(i in 0..100) {
//            var query = String.format("insert into TB_LOG (userId, startDate) values ('onlytree', '%s')", DateUtil.toString(Date(), "yyyyMMddHHmmss"))
//            querys.add(query)
//        }
//        LogUtil.log("onlytree", SqliteUtil.getInstance(this).runQuerys(querys))
//        LogUtil.log("onlytree", SqliteUtil.getInstance(this).selectQuery("select * from TB_LOG;"))
        var userAgent = ""
        userAgent += ",os:android"
        userAgent += ",versionCode:$versionCode"
        userAgent += ",versionName:$versionName"
        userAgent += ",sBarHeight:$statusBarHeight"
        userAgent += ",device_uuid:" + AppUtil.getDeviceUuid(mActivity)
        userAgent += ",token:" + PrefUtil.getInstance(mActivity).getString(Const.PREF_KEY_LOGIN_TOKEN, "") + ":end:"
        setContent {
            MainContent(act = mActivity, userAgent = userAgent)
//            Scaffold(
//                topBar = { TopAppBar(title = { Text("GFG | WebView", color = Color.White) }, backgroundColor = Color(0xff0f9d58)) },
//                content = { pd ->
//                    Column(modifier = Modifier.padding(pd)) {
//                        MainWeb()
//                    }
//                },
//            )
        }
    }

    fun hideIntroAction() {
        hideIntro = true
    }

    fun changeConfiguraiont(value: Int) {
        requestedOrientation = value;
    }

    // 뒤로가기 버튼 처리
    override fun onBackPressed() {
        //webView?.loadUrl("javascript:goTest()")
        if (!hideIntro || !webView!!.canGoBack()) {
            if (System.currentTimeMillis() > back + 2000) {
                back = System.currentTimeMillis()
                showCenterToast(R.string.backkey_finish)
            } else {
                hideCenterToast()
                finish()
            }
        } else {
            webView?.loadUrl("javascript:backRouter()")
        }
    }
    @Preview
    @Composable
    fun MainContent(act: Activity, userAgent: String) {
        Scaffold(
            content = { pd ->
                Box(modifier = Modifier.padding(pd)) {
                    MainWeb(act = act, userAgent = userAgent)
                    AnimatedVisibility(visible = !hideIntro, enter = fadeIn(initialAlpha = 0.9f), exit = fadeOut()) {
                        Box(
                            Modifier
                                .background(Color(0xFF15D4E0))
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(R.drawable.logo_intro),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }
                    }
                }
            },
        )
    }

//@Composable
//fun web() {
//    val state = rememberWebViewState(Const.HYBRID_WEB_LINK)
//    WebView(
//        state,
//        Modifier,
//        onCreated = {
//            it.settings.javaScriptEnabled = true
//            it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        }
//    )
//}

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun MainWeb(
        act: Activity,
        userAgent: String,
    ) {
//    var value by remember { mutableStateOf(Const.HYBRID_WEB_LINK) }
        var value by url;
        AndroidView(factory = {
            WebView(it).apply {

                webView = this

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.builtInZoomControls = false
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
                settings.domStorageEnabled = true
                settings.textZoom = 100
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.userAgentString = settings.userAgentString + userAgent

                settings.allowFileAccessFromFileURLs = true;
                settings.allowUniversalAccessFromFileURLs = true;

                settings.javaScriptCanOpenWindowsAutomatically = true
//                settings.mediaPlaybackRequiresUserGesture = true

                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadsImagesAutomatically = true

                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.setPluginState(WebSettings.PluginState.ON);
                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        try {
                            if (fileChooserParams != null) {
                                uploadMessage?.onReceiveValue(null)
                                uploadMessage = null
                                uploadMessage = filePathCallback
                                val isMultiSelect = fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                                if (fileChooserParams.acceptTypes.indexOf(".png") != -1 || fileChooserParams.acceptTypes.indexOf("image/*") != -1) {
                                    // 이미지 선택기
                                    val galleryIntent = Intent(Intent.ACTION_PICK)
                                    galleryIntent.type = "image/*"
                                    if (isMultiSelect) {
                                        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }
                                    val intent = Intent.createChooser(galleryIntent, "")
                                    startActivityForResult(intent, REQUEST_SELECT_FILE)
                                } else if (fileChooserParams.acceptTypes.indexOf(".avi") != -1 || fileChooserParams.acceptTypes.indexOf("video/*") != -1) {
                                    // 비디오 선택기
                                    val videoIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                                    videoIntent.type = "video/*"
                                    if (isMultiSelect) {
                                        videoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }
                                    startActivityForResult(videoIntent, REQUEST_SELECT_FILE)
                                } else {
                                    // 파일 선택기
                                    val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
                                    galleryIntent.type = "*/*"
                                    if (isMultiSelect) {
                                        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }
                                    galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)
                                    try {
                                        startActivityForResult(galleryIntent, REQUEST_SELECT_FILE)
                                    } catch (e: ActivityNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                                return true
                            }
                        } catch (e: Exception) {
                            uploadMessage = null
                        }
                        return false
                    }

                    /*
                    -->
                     */
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources);
//                        runOnUiThread {
//                            val PERMISSIONS = arrayOf(
//                                PermissionRequest.RESOURCE_VIDEO_CAPTURE
//                            )
//                            request.grant(PERMISSIONS)
//                        }
                    }

                    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                        super.onPermissionRequestCanceled(request)
                        Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                    showErrorPage(request?.url)
                        super.onReceivedError(view, request, error)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
//                    pageFinishedAction(Uri.parse(url))
                        super.onPageFinished(view, url)
                    }

                    // 브릿지 처리
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                    if (checkUrl(view, request?.url)) {
//                        return true
//                    }
                        if (!request?.url.toString().startsWith(Const.HYBRID_WEB_LINK)) {
                            val intent = Intent(Intent.ACTION_VIEW, request?.url)
                            startActivity(intent)
                            return true
                        }
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }

                this.addJavascriptInterface(object {
                    @JavascriptInterface
                    fun nativeBridge(cmd: String?, data: String?, callback: String?) {
                        if (cmd != null) {
                            for (bridgeItem in Const.APP_BRIDGE_LIST) {
                                if (bridgeItem.cmd.equals(cmd, true)) {
                                    bridgeItem.startAction(act, webView, data, callback)
                                }
                            }
                        }
                    }
                }, "Android");
            }
        }, update = {
            it.loadUrl(value)
        })
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MainContent(Activity(), "Android")
    }

    var contactCallback: String? = null;
    fun getContact(callback: String?) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val getResultContact = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    var cursor: Cursor? = null
                    if (it.data != null) {
                        cursor = it.data?.data?.let {
                            contentResolver.query(
                                it, arrayOf(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                ), null, null, null
                            )
                        }
                    }
                    if (cursor != null) {
                        cursor.moveToFirst()
                        webView?.loadUrl("javascript:${callback}('${cursor.getString(1).replace("-", "")}');")
                        cursor.close()
                    }
                }
            }
            getResultContact.launch(intent)
        } else {
            contactCallback = callback;
            startActivityForResult(intent, REQUEST_CONTACT);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // MediaFilterActivity result check
        if(requestCode == RetouchResultActivity.REQUEST_CODE_RETOUCH) {
            if(resultCode == RESULT_OK
                && data?.hasExtra(RetouchResultActivity.CALLBACK_NAME) == true) {   // 정상적으로  보정작업이 완료됨
                val callback = data.getStringExtra(RetouchResultActivity.CALLBACK_NAME)
                Log.d("csson", "onActivityResult: callback = $callback")
                callback?.let {
                    runOnUiThread {
                        webView?.loadUrl("javascript:${callback}(true)")
                    }
                }
            }
        }


        if (resultCode == RESULT_OK && requestCode == REQUEST_CONTACT) {
            var cursor: Cursor? = null
            if (data != null) {
                cursor = data.data?.let {
                    contentResolver.query(
                        it, arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ), null, null, null
                    )
                }
            }
            if (cursor != null) {
                cursor.moveToFirst()
                webView?.loadUrl("javascript:${contactCallback}('${cursor.getString(1).replace("-", "")}');")
                cursor.close()
            }
        } else if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) {
                return
            }
            var results: Array<Uri>? = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            if (results != null) {
                uploadMessage?.onReceiveValue(results)
            } else {
                val uris = ArrayList<Uri>()
                if (data != null) {
                    val clipData = data.clipData!!
                    if (clipData.itemCount > 0) {
                        var i = 0
                        val size = clipData.itemCount
                        while (i < size) {
                            val uri = clipData.getItemAt(i).uri
                            uris.add(uri)
                            i++
                        }
                        results = uris.toTypedArray()
                    }
                }
                if (results != null) {
                    uploadMessage?.onReceiveValue(results)
                } else {
                    uploadMessage?.onReceiveValue(null)
                }
            }
            uploadMessage = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StartDive.REQUEST_CAMERA_VIEW_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                StartDive().startCameraViewActivity(this)
            } else {
                StartDive().retryPermissionCheck(this)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        webView?.loadUrl("javascript:onOrientationChange(${newConfig.orientation})")
    }
}
