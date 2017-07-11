package com.ue.camerafacedemo.helper;

/**
 * Created by hujiang on 2017/7/10.
 */

public interface OnCameraListener {
    void onCameraOpenError(String msg);

    void onFaceFound();

    void onFaceNotFound();
}
