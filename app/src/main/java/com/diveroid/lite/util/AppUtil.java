package com.diveroid.lite.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.diveroid.lite.BuildConfig;

import java.util.UUID;

public class AppUtil {
    @SuppressLint("HardwareIds")
    public static String getDeviceUuid(Context cont) {
        try {
            String uuid = PrefUtil.getInstance(cont).getString("DEVICE_UUID", "");
            if (uuid != null && !uuid.isEmpty()) {
                return uuid;
            }
            String tmDevice = "", tmSerial = "", androidId = "";
            androidId = "" + Settings.Secure.getString(cont.getContentResolver(), Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            PrefUtil.getInstance(cont).put("DEVICE_UUID", deviceUuid.toString());
            return deviceUuid.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getTestString(String str) {
        return str + ":::test";
    }
}
