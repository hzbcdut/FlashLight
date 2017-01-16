package com.cdut.flashlight.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cdut.flashlight.R;
import com.cdut.flashlight.present.Camera2Present;
import com.cdut.flashlight.present.Camera2View;
import com.cdut.flashlight.utils.ToastUtil;

/**
 * 自定义相机
 */
public class CustomCamera2Activity extends AppCompatActivity implements Camera2View {

    private Camera2Present present;
    public static void start(Context context) {
        Intent intent = new Intent(context, CustomCamera2Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera2);

        present = new Camera2Present(this,this);
    }

    @Override
    public void initCameraSuccess() {
        ToastUtil.showToast(this, "初始化相机成功！！！");
    }

    @Override
    public void initCameraFailure() {
        ToastUtil.showToast(this, "初始化相机失败！！！");
    }
}
