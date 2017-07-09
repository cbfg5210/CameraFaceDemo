package com.ue.camerafacedemo;

/**
 * Created by hawk on 2017/7/9.
 */

public interface OnCameraListener {
    void onCameraOpenError(String msg);

    void onCameraOpenSuccess();

    void onFaceFound();

    void onFaceNotFound();
}
