package com.cdut.flashlight.present;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;

import com.cdut.flashlight.utils.Camera2Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by hans on 2017/1/16 0016.
 */

public class Camera2Present {

    private Context mContext;
    private Camera2View camera2View;

    public Camera2Present(Context context, Camera2View camera2View) {
        this.mContext = context.getApplicationContext();
        this.camera2View = camera2View;
        init();
    }

    /**
     * 在子线程中初始化相机参数
     */
    private void init() {
        Observable.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

                    //获取相机id
                    String[] cameraIds = manager.getCameraIdList();

                    if (cameraIds != null && cameraIds.length > 0) {
                        //后置摄像头存在
                        if (cameraIds[0] != null) {
                            CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[0]);
                            //流配置
                            StreamConfigurationMap map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            //适合SurfaceTexture的显示的size
                            Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
                            //图片格式
                            int[] formatsAll = map.getOutputFormats();
                            //这里只要jpeg和dng
                            List<Integer> formatList = new ArrayList<Integer>();
                            for (int format : formatsAll) {
                                if (format == ImageFormat.JPEG || format == ImageFormat.RAW_SENSOR) {
                                    formatList.add(format);
                                }
                            }
                            Integer[] formats = formatList.toArray(new Integer[formatList.size()]);
                            //不同的format对应不同的照片size
                            Size[][] pictureSizes = new Size[formats.length][];
                            for (int i = 0; i < formats.length; i++) {
                                //这里会出现有的格式但是没有保存图片的size
                                if (null != map.getOutputSizes(formats[i])) {
                                    pictureSizes[i] = map.getOutputSizes(formats[i]);
                                } else {
                                    Log.i("Runnable", "camera0--->map.getOutputSizes为空");
                                }
                            }
                            Camera2Util.writePreferenceForCameraId(mContext, "camera0", previewSizes, formats, pictureSizes);

                        }
                        if (cameraIds[1] != null) {//前置摄像头存在
                            CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[1]);
                            //流配置
                            StreamConfigurationMap map = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                            //适合SurfaceTexture的显示的size
                            Size[] previewSizes = map.getOutputSizes(SurfaceTexture.class);
                            //图片格式
                            int[] formatsAll = map.getOutputFormats();
                            //这里只要jpeg和dng
                            List<Integer> formatList = new ArrayList<Integer>();
                            for (int format : formatsAll) {
                                if (format == ImageFormat.JPEG || format == ImageFormat.RAW_SENSOR) {
                                    formatList.add(format);
                                }
                            }
                            Integer[] formats = formatList.toArray(new Integer[formatList.size()]);
                            //不同的format对应不同的照片size
                            Size[][] pictureSizes = new Size[formats.length][];
                            for (int i = 0; i < formats.length; i++) {
                                //这里会出现有的格式但是没有保存图片的size
                                if (null != map.getOutputSizes(formats[i])) {
                                    pictureSizes[i] = map.getOutputSizes(formats[i]);
                                } else {
                                    Log.i("Runnable", "camera1--->map.getOutputSizes为空");
                                }
                            }
                            Camera2Util.writePreferenceForCameraId(mContext, "camera1", previewSizes, formats, pictureSizes);
                        }
                    }
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                    return 0;
                }
                //初始化一开始打开camera为1的摄像头
                Camera2Util.writeCurrentCameraid(mContext, "0");
                return -1;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 0) {
                            camera2View.initCameraFailure();
                        } else if (integer == -1) {
                            camera2View.initCameraSuccess();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });  //订阅才会调用
    }
}
