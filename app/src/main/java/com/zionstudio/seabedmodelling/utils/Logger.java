package com.zionstudio.seabedmodelling.utils;

import android.util.Log;

import static com.zionstudio.seabedmodelling.AppConfig.IS_DEBUG;

/**
 * Created by QiuXi'an on 2017/12/12 0012.
 * Email Zionlife1025@163.com
 */

public class Logger {

    public static void v(String tag, String msg) {
        if (IS_DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (IS_DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (IS_DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(tag, msg);
        }
    }
}
