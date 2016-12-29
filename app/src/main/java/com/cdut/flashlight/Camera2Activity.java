package com.cdut.flashlight;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Camera2Activity";

    @BindView(R.id.openFlightLight)
    Button openBtn;

    private CameraManager mCameraManager;
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        ButterKnife.bind(this);  //绑定ButterKnife
        setListener();

        if (checkCamera()) {
            checkPermission();
        }
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        register();
    }

    private void setListener() {
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
//                openFlashLight();
            }
        });
    }

    private boolean isOpen = false;

    private void openFlashLight() {

        if (isOpen){
            turnOffFlashLight();
            isOpen = false;
        }else {
            turnOnFlashLight();
            isOpen = true;
        }
    }

    private void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},100);
        }else {

            initCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if (grantResults[0]==RESULT_OK){
                initCamera();
            }else {

            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void initCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
//            String cameraId = mCameraManager.getCameraIdList()[1];
            String[]  ids = mCameraManager.getCameraIdList();

            if (ids!=null&&ids.length>0){
                CameraCharacteristics characteristics =  mCameraManager.getCameraCharacteristics(ids[0]); //查询Camera Devices的可用性
                boolean isFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);           //ids[1] 不可用
                if (isFlash){
                    //            mCameraManager.setTorchMode(cameraId,true);  //此方法使用会异常
                    mCameraManager.openCamera(ids[0], mStateCallback, new Handler());  //异步打开相机
                }else {

                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

//        mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
//            @Override
//            public void onTorchModeUnavailable(String cameraId) {
//                super.onTorchModeUnavailable(cameraId);
//
//            }
//
//            @Override
//            public void onTorchModeChanged(String cameraId, boolean enabled) {
//                super.onTorchModeChanged(cameraId, enabled);
//
//            }
//        },new Handler());
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

    private CameraDevice cameraDevice;
    private CameraCaptureSession  mSession;
    CaptureRequest.Builder builder = null;

    private CameraDevice.StateCallback  mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {  //相机已打开
                LogUtil.d(Constant.DEBUG,TAG+"--> StateCallback = onOpened()");
            cameraDevice = camera;

            try {
                builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                List<Surface> list = new ArrayList<Surface>();
                SurfaceTexture mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(cameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                Surface mSurface = new Surface(mSurfaceTexture);
                list.add(mSurface);
                builder.addTarget(mSurface);
                camera.createCaptureSession(list, new MyCameraCaptureSessionStateCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            LogUtil.d(Constant.DEBUG,TAG+"--> StateCallback = onDisconnected()");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            LogUtil.d(Constant.DEBUG,TAG+"--> StateCallback = onError()");
        }
    };

    private Size getSmallestSize(String cameraId) throws CameraAccessException
    {
        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0)
        {
            throw new IllegalStateException("Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes)
        {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight())
            {
                chosen = s;
            }
        }
        return chosen;
    }

    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback
    {
        @Override
        public void onConfigured(CameraCaptureSession session)
        {
            mSession = session;
            try
            {
                mSession.setRepeatingRequest(builder.build(), null, null);
            }
            catch (CameraAccessException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session)
        {

        }
    }

    public void turnOnFlashLight() {
        try
        {
            builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            mSession.setRepeatingRequest(builder.build(), null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void turnOffFlashLight()
    {
        try
        {
            builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
            mSession.setRepeatingRequest(builder.build(), null, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void close()
    {
        if (cameraDevice == null || mSession == null)
        {
            return;
        }
        mSession.close();
        cameraDevice.close();
        cameraDevice = null;
        mSession = null;
    }

    /**
     * 启动Activity
     * @param context
     */
    public static void start(Context context){
        Intent intent = new Intent(context,Camera2Activity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
        unregister();
    }


    //消息通知
    private void sendNotification(){
        LogUtil.d(Constant.DEBUG,TAG+"-->消息通知");

//        Intent i = new Intent();  //这里测试是可以接收到广播的
//        i.setAction(ACTION);
//        i.putExtra("hell","h");
//        sendBroadcast(i);

        Notification.Builder  builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        RemoteViews  views = new RemoteViews(getPackageName(),R.layout.notification);
        builder.setContent(views);

//        Intent intent = new Intent(this,Camer2DemoActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,100,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setOnClickPendingIntent(R.id.btn,pendingIntent);
//
//
//        Intent flashIntent = new Intent(this,FlashLightReceiver.class);
//        flashIntent.setAction(ACTION);
//        PendingIntent flashPIntent = PendingIntent.getBroadcast(this,101,flashIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setOnClickPendingIntent(R.id.open_flash_light,flashPIntent);

        PendingIntent pendingIntent =null;
        Intent intent =null;

//        intent = new Intent(this,Camer2DemoActivity.class);
//        pendingIntent = PendingIntent.getActivity(this,100,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setOnClickPendingIntent(R.id.btn,pendingIntent);


//        intent = new Intent(this,FlashLightReceiver.class);
        intent = new Intent();
//        intent.setClass(this,FlashLightReceiver.class);
        intent.setAction(ACTION);
        pendingIntent = PendingIntent.getBroadcast(Camera2Activity.this,101,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.open_flash_light,pendingIntent);

        mNotificationManager.notify(5,builder.build());

    }


    private class FlashLightReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(Constant.DEBUG,TAG+"-->接收到广播");
            handler.obtainMessage(1).sendToTarget();
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    openFlashLight();
                    break;
            }
        }
    };

    private static final String ACTION = "com.cdut.flashlight.ACTION_BROADCAST_OPEN_FLASHLIGHT";
    private FlashLightReceiver receiver;
    private void register(){
        receiver = new FlashLightReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(receiver,intentFilter);
    }
    private void unregister(){
        unregisterReceiver(receiver);
    }
}
