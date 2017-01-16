package com.cdut.flashlight.utils;

import android.content.Context;
import android.widget.Toast;

import com.cdut.flashlight.constant.AppConfig;


/**
 * Created by  on 2016/5/17 0017.
 */
public class ToastUtil {

    public static void showToast(Context context, String s) {
        if (AppConfig.DEBUG)
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int res) {
        if (AppConfig.DEBUG)
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }
}
