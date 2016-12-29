package com.cdut.flashlight;

import android.util.Log;

/**
 * Created by Administrator on 2016/5/9 0009.
 */
public class LogUtil {
    private static boolean isLog=true;
    public static void i(String tag,String msg){
        if(isLog){
            Log.i(tag,msg);
        }
    }
    public static void d(String tag,String msg){
        if(isLog){
            Log.d(tag,msg);
        }
    }
    public static void e(String tag,String msg){
        if(isLog){
            Log.e(tag,msg);
        }
    }
}
