package com.diveroid.camera.utils;

import org.opencv.core.Mat;

/**
 * Created by yunsuk on 16/11/2017.
 */

public class CvUtil {
    public static void filter (Mat mat, double level ) {
        filter(mat.getNativeObjAddr(), level);
    }

    public static native long filter(long matAddr, double level);

}
