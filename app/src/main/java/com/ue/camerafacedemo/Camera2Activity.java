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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;

public class Camera2Activity extends AppCompatActivity {
    private static final String TAG = "Camera2Activity";

    private TextureView mTextureView;
    private CameraHelper mCameraHelper;

    private OldCameraHelper mOldCameraHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera2_basic);

        mOldCameraHelper = new OldCameraHelper(this);

        mTextureView = (TextureView) findViewById(R.id.texture);
//        mTextureView.setVisibility(View.INVISIBLE);
//        mTextureView = new TextureView(this);

//        mCameraHelper = new CameraHelper(Camera2Activity.this);
        /*mCameraHelper.setOnCameraListener(new CameraHelper.OnCameraListener() {
            @Override
            public void onCameraOpenError(String msg) {

            }

            @Override
            public void onCameraOpenSuccess() {

            }

            @Override
            public void onFaceFound() {

            }

            @Override
            public void onFaceNotFound() {

            }
        });*/

        findViewById(R.id.btn_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTextureView.isAvailable()) {
//                    mCameraHelper.openCamera(mTextureView);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mCameraHelper.closeCamera();
        super.onDestroy();
    }
}
