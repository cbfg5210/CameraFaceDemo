package com.ue.camerafacedemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;

/**
 * Created by hujiang on 2017/7/7.
 */

public class CameraHelper {
    private static final String TAG = CameraHelper.class.getSimpleName();
    private Activity context;
    //camera1
    private Camera mCamera;
    //camera2
    private String mCameraId;
    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private CameraCaptureSession mCaptureSession;

    private CameraDevice.StateCallback mStateCallback;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;
    private OnCameraListener onCameraListener;
    private CameraManager manager;

    public CameraHelper(Activity activity) {
        this.context = activity;
    }

    public void setOnCameraListener(OnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(@NonNull CameraDevice cameraDevice) {
                // This method is called when the camera is opened.  We start camera preview here.
                mCameraDevice = cameraDevice;
                createCameraPreviewSession();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                closeCamera2();
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                closeCamera2();
            }
        };

        mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

            private void process(CaptureResult result) {
                if (!CallbackUtils.isActivityValid(context)) {
                    return;
                }
//                Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
                Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
                Log.e("tag", "faces : " + faces.length);

                if (faces == null || faces.length <= 0) {
                    onCameraListener.onFaceNotFound();
                } else {
                    onCameraListener.onFaceFound();
                }
            }

            @Override
            public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                            @NonNull CaptureRequest request,
                                            @NonNull CaptureResult partialResult) {
                process(partialResult);
            }

            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                process(result);
            }
        };
        setUpCameraOutputs();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);
            // We set up a CaptureRequest.Builder with the output Surface.
            final CaptureRequest.Builder mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                                        CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL);
                                // Finally, we start displaying the camera preview.
                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, "onConfigureFailed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpCameraOutputs() {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                System.out.println("cam id " + cameraId);
                // We don't use a front facing camera in this sample.
                int facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraId = cameraId;
                    break;
                }
            }
            if (TextUtils.isEmpty(mCameraId)) {
                Log.e(TAG, "empty camera id");
                return;
            }
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Log.e(TAG, "map=null");
                return;
            }
            mPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openCamera(TextureView textureView) {
        if (onCameraListener == null) {
            throw new IllegalArgumentException("onCameraListener not set");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            openCamera1();
        } else {
            openCamera2(textureView);
        }
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera2(TextureView mTextureView) {
        if (mCameraDevice != null) {
            //表示开启过了，不用再开启
            return;
        }

        this.mTextureView = mTextureView;
        initCamera2();

        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //移除opernCamera的提示
            }
            manager.openCamera(mCameraId, mStateCallback, null);
            onCameraListener.onCameraOpenSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            onCameraListener.onCameraOpenError("failed to open camera2,err=" + e.getMessage());
        }
    }

    public void closeCamera() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            closeCamera1();
        } else {
            closeCamera2();
        }
    }

    private void closeCamera1() {
        if (mCamera != null) {
            mCamera.stopFaceDetection();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeCamera2() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

//    public interface OnCameraListener {
//        void onCameraOpenError(String msg);
//
//        void onCameraOpenSuccess();
//
//        void onFaceFound();
//
//        void onFaceNotFound();
//    }
}
