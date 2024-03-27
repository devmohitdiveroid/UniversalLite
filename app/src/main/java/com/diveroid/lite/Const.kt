package com.diveroid.lite

import com.diveroid.lite.native_bridge.*

object Const {
    val INTERNAL_UPLOAD_DIR = ""

//     val HYBRID_WEB_LINK = "http://192.168.0.49:8080"
    val HYBRID_WEB_LINK = "file:///android_asset/www/dist/index.html"
    var APP_BRIDGE_LIST = arrayOf(
        GetPrefData(),
        RemovePrefData(),
        SetPrefData(),
        Sqlite(),
        TestLog(),
        HideIntro(),
        ExitApp(),
        GetContact(),
        ShareImage(),
        FileDownload(),
        FileDelete(),
        ShowFilterView(),
        StartDive(),
        SetStatusBarFontColor(),
        ChangeOrientation(),
        TestLog2(),
        ShowAuthView(),
        ShowLicense(),
        CheckAuth(),
        GetSerialNumber()
    )

    val PREF_KEY_LOGIN_TOKEN = "PREF_KEY_LOGIN_TOKEN"
    val PREF_LANG_SETTING = "PREF_LANG_SETTING"
    val PREF_VIEW_DIVING_MANUAL = "PREF_VIEW_DIVING_MANUAL"
}
