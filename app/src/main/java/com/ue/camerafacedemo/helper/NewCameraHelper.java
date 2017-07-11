package com.ue.camerafacedemo.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.ue.camerafacedemo.CallbackUtils;
import com.ue.camerafacedemo.ToastUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hujiang on 2017/7/7.
 */

public class NewCameraHelper implements ICameraHelper {
    private static final String TAG = NewCameraHelper.class.getSimpleName();
    private Activity context;
    //camera2
    private String mCameraId;
    private TextureView mTextureView;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;

    private CameraDevice.StateCallback mStateCallback;
    private CameraCaptureSession.CaptureCallback mCaptureCallback;
    private CameraManager manager;

    private OnCameraListener onCameraListener;
    private boolean isCameraOpenSuccess = false;

    public NewCameraHelper(Activity context, TextureView mTextureView) {
        this.context = context;
        this.mTextureView = mTextureView;
    }

    public void setOnCameraListener(OnCameraListener onCameraListener) {
        this.onCameraListener = onCameraListener;
    }

    @Override
    public boolean isCameraOpened() {
        return isCameraOpenSuccess;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCameraParams() {
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
                closeCamera();
            }

            @Override
            public void onError(@NonNull CameraDevice cameraDevice, int error) {
                closeCamera();
            }
        };

        mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
            private void process(CaptureResult result) {
                if (!CallbackUtils.isActivityValid(context)) {
                    return;
                }
                Face[] faces = result.get(CaptureResult.STATISTICS_FACES);

                if (faces == null || faces.length <= 0) {
                    Log.e(TAG, "face not found,faces=" + faces);
                    onCameraListener.onFaceNotFound();
                } else {
                    Log.e(TAG, "face found *****");
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
            SurfaceTexture texture = mTextureView.getSurfaceTexture();//null?
            Log.e(TAG, "texture=" + texture + ",size=" + mPreviewSize);
            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);
            // We set up a CaptureRequest.Builder with the output Surface.
            final CaptureRequest.Builder mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {

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
                    }, null);
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
            if (map == null) {git
                Log.e(TAG, "map=null");
                return;
            }
            mPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera() {
        if (onCameraListener == null) {
            throw new IllegalArgumentException("onCameraListener not set");
        }
        if (mCameraDevice != null) {
            //表示开启过了，不用再开启
            return;
        }

        boolean isM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        if (isM) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.showShort(context, "open_camera_permission");
            }

            AndPermission.with(context)
                    .requestCode(100)
                    .permission(Manifest.permission.CAMERA)
                    .rationale(new RationaleListener() {
                        @Override
                        public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                            // 此对话框可以自定义，调用rationale.resume()就可以继续申请。
                            AndPermission.rationaleDialog(context, rationale).show();
                        }
                    })
                    .callback(new PermissionListener() {
                        @Override
                        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                            initAndOpenCamera();
                        }

                        @Override
                        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        }
                    })
                    .start();
        } else {
            initAndOpenCamera();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initAndOpenCamera() {
        initCameraParams();
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //移除以下一句的提示
            }
            manager.openCamera(mCameraId, mStateCallback, null);
            isCameraOpenSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            onCameraListener.onCameraOpenError("failed to open camera2,err=" + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }
}
