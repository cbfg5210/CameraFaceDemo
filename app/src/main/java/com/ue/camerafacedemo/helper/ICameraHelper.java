package com.ue.camerafacedemo.helper;

/**
 * Created by hujiang on 2017/7/10.
 */

public interface ICameraHelper {
    void openCamera();

    void closeCamera();

    void setOnCameraListener(OnCameraListener onCameraListener);

    boolean isCameraOpened();
}
