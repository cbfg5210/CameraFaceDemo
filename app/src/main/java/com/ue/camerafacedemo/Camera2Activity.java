/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ue.camerafacedemo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.ue.camerafacedemo.helper.ICameraHelper;
import com.ue.camerafacedemo.helper.NewCameraHelper;
import com.ue.camerafacedemo.helper.OldCameraHelper;
import com.ue.camerafacedemo.helper.OnCameraListener;

public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Camera2Activity";

    private TextureView mTextureView;
    private ICameraHelper cameraHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera2_basic);

        mTextureView = (TextureView) findViewById(R.id.texture);

        findViewById(R.id.btn_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextureView.isAvailable()) {
//                    mCameraHelper.openCamera(mTextureView);
                }
            }
        });

        initCameraHelper();
    }

    private void initCameraHelper() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            cameraHelper = new OldCameraHelper(this, mTextureView);
        } else {
            cameraHelper = new NewCameraHelper(this, mTextureView);
        }
        cameraHelper.setOnCameraListener(new OnCameraListener() {
            @Override
            public void onCameraOpenError(String msg) {
                ToastUtil.showShort(Camera2Activity.this, "use_camera_error");
            }

            @Override
            public void onFaceFound() {

            }

            @Override
            public void onFaceNotFound() {

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.e(TAG, "cameraHelper=" + cameraHelper);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!CallbackUtils.isActivityValid(Camera2Activity.this)) {
                    return;
                }
                if (cameraHelper == null) {
                    initCameraHelper();
                }
                cameraHelper.openCamera();
            }
        }, 200);
    }

    @Override
    public void onPause() {
        if (cameraHelper != null) {
            cameraHelper.closeCamera();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cameraHelper.closeCamera();
        super.onDestroy();
    }
}
