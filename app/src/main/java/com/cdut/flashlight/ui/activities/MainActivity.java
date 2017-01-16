package com.cdut.flashlight.ui.activities;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cdut.flashlight.R;
import com.cdut.flashlight.ui.Camera2Activity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.open)
    Button openBtn;

    @BindView(R.id.camera2)
    Button camera2Btn;

    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);  //绑定ButterKnife

        setListener();

    }

    private void setListener() {
        openBtn.setOnClickListener(this);
        camera2Btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.open:
                openFlashLight();
                break;
            case R.id.camera2:
                Camera2Activity.start(this);
//                Camer2DemoActivity.start(this);
                break;
        }
    }

    private boolean isOpen = false;
    private void openFlashLight() {
        if (checkCamera()){
            Toast.makeText(MainActivity.this, "设备支持闪光灯", Toast.LENGTH_SHORT).show();
            if (!isOpen){
                open();
            }else {
                close();
            }
        }else {
            Toast.makeText(MainActivity.this, "不支持闪光灯", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 打开闪光灯
     */
    private void open() {
        //            int cameraNum = Camera.getNumberOfCameras();
//            mCamera = Camera.open(cameraNum-1);  //为何没用
        mCamera = Camera.open();
//        Camera.Parameters p = getParameters(Camera.Parameters.FLASH_MODE_ON);  //这个不能用
        Camera.Parameters p = getParameters(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(p);
        mCamera.startPreview();

        isOpen = true;
    }

    /**
     * 关闭闪光灯
     */
    private void close() {
            mCamera.setParameters(getParameters(Camera.Parameters.FLASH_MODE_OFF));
            mCamera.stopPreview();
            mCamera.release();

            isOpen = false;
    }

    @NonNull
    private Camera.Parameters getParameters(String flashMode) {
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(flashMode);
        return p;
    }

    /**
     * 检查设备是否支持闪光灯
     * @return  是否支持闪光灯
     */
    private boolean checkCamera(){
        boolean hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        return hasFlash;
    }

    /**
     * @return 摄像头是否存在
     */
    private boolean checkIfCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}
