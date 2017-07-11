package com.ue.camerafacedemo.helper;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by hujiang on 2017/7/7.
 */

public class OldCameraHelper implements ICameraHelper {
    private static final String TAG = OldCameraHelper.class.getSimpleName();
    private Activity context;
    private Camera mCamera;
    private OnCameraListener onCameraListener;
    private TextureView mTextureView;
    private boolean isCameraOpenSuccess = false;

    public OldCameraHelper(Activity context, TextureView textureView) {
        this.context = context;
        this.mTextureView = textureView;
    }

    public void setOnCameraListener(OnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }

    @Override
    public boolean isCameraOpened() {
        return isCameraOpenSuccess;
    }


    public void openCamera() {
        if (onCameraListener == null) {
            throw new IllegalArgumentException("onCameraListener not set");
        }
        if (mCamera != null) {
            return;
        }
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());//在5.0不设置这句也可以获取到face数据,在4.4的时候不行
            mCamera.startPreview();

            Camera.Parameters parameters = mCamera.getParameters();
            Log.e(TAG,"parameters.getMaxNumDetectedFaces()=" + parameters.getMaxNumDetectedFaces());
            if (parameters.getMaxNumDetectedFaces() >= 1) {
                mCamera.startFaceDetection();
                isCameraOpenSuccess = true;
            } else {
                onCameraListener.onCameraOpenError("can not detect face");
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
        if (mCamera != null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
