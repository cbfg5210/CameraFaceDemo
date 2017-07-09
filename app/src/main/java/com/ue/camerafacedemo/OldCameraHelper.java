package com.ue.camerafacedemo;

import android.app.Activity;
import android.hardware.Camera;

/**
 * Created by hawk on 2017/7/9.
 */

public class OldCameraHelper implements ICameraHelper {
    private static final String TAG = OldCameraHelper.class.getSimpleName();
    private Activity context;
    //camera1
    private Camera mCamera;
    private OnCameraListener onCameraListener;

    public OldCameraHelper(Activity activity) {
        this.context = activity;
    }

    public void setOnCameraListener(OnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }

    public void openCamera() {
        if (onCameraListener == null) {
            throw new IllegalArgumentException("onCameraListener not set");
        }
        openCamera1();
    }

    private void openCamera1() {
        if (mCamera != null) {
            return;
        }

        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.startPreview();

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumDetectedFaces() >= 1) {
                mCamera.startFaceDetection();
                onCameraListener.onCameraOpenSuccess();
            } else {
                onCameraListener.onCameraOpenError("can not detect face");
                return;
            }

            mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    if (faces == null || faces.length <= 0) {
                        onCameraListener.onFaceNotFound();
                    } else {
                        onCameraListener.onFaceFound();
                    }
                }
            });
        } catch (Exception exp) {
            onCameraListener.onCameraOpenError("failed to open camera1,err=" + exp.getMessage());
        }
    }

    public void closeCamera() {
        closeCamera1();
    }

    private void closeCamera1() {
        if (mCamera != null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
