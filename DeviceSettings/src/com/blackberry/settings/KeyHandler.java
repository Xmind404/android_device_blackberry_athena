/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blackberry.settings;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;

/**
 * Device key handler for the KEY2 Convenience key (gpio-keys scancode 550, KEYCODE_ASSIST).
 *
 * Loaded by PhoneWindowManager in system_server through config_deviceKeyHandlerLibs, so it only
 * uses framework APIs. The action is chosen on the BlackBerry Keyboard settings page
 * ({@link #SETTING_CONVENIENCE_KEY_ACTION}). "assistant" leaves the key to the stock ASSIST
 * handling (the default assistant, e.g. Gemini with Google apps installed); the other actions
 * consume both key events and run on the initial press.
 */
public class KeyHandler implements DeviceKeyHandler {
    private static final String TAG = "BlackBerryKeyHandler";

    static final String SETTING_CONVENIENCE_KEY_ACTION = "convenience_key_action";
    static final String ACTION_ASSISTANT = "assistant";
    static final String ACTION_CAMERA = "camera";
    static final String ACTION_TORCH = "torch";
    static final String ACTION_NOTHING = "nothing";

    private static final int SCANCODE_CONVENIENCE = 550;

    private final Context mContext;
    private final CameraManager mCameraManager;
    private final PowerManager mPowerManager;
    private final KeyguardManager mKeyguardManager;
    private String mTorchCameraId;
    private boolean mTorchEnabled;

    public KeyHandler(Context context) {
        mContext = context;
        mCameraManager = context.getSystemService(CameraManager.class);
        mPowerManager = context.getSystemService(PowerManager.class);
        mKeyguardManager = context.getSystemService(KeyguardManager.class);
        if (mCameraManager != null) {
            mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                @Override
                public void onTorchModeChanged(String cameraId, boolean enabled) {
                    if (cameraId.equals(mTorchCameraId)) {
                        mTorchEnabled = enabled;
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        }
    }

    @Override
    public KeyEvent handleKeyEvent(KeyEvent event) {
        if (event.getScanCode() != SCANCODE_CONVENIENCE) {
            return event;
        }

        final String action = getAction();
        if (ACTION_ASSISTANT.equals(action)) {
            return event;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            switch (action) {
                case ACTION_CAMERA:
                    launchCamera();
                    break;
                case ACTION_TORCH:
                    toggleTorch();
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    private String getAction() {
        final String action = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                SETTING_CONVENIENCE_KEY_ACTION, UserHandle.USER_CURRENT);
        return action != null ? action : ACTION_ASSISTANT;
    }

    private void launchCamera() {
        mPowerManager.wakeUp(SystemClock.uptimeMillis(), PowerManager.WAKE_REASON_GESTURE,
                "com.blackberry.settings:CONVENIENCE_KEY");
        final boolean locked = mKeyguardManager != null && mKeyguardManager.isKeyguardLocked();
        final Intent intent = new Intent(locked
                ? MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE
                : MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unable to launch the camera", e);
        }
    }

    private void toggleTorch() {
        final String cameraId = getTorchCameraId();
        if (cameraId == null) {
            return;
        }
        try {
            mCameraManager.setTorchMode(cameraId, !mTorchEnabled);
        } catch (CameraAccessException | IllegalArgumentException e) {
            Log.w(TAG, "Unable to toggle the torch", e);
        }
    }

    private String getTorchCameraId() {
        if (mTorchCameraId != null || mCameraManager == null) {
            return mTorchCameraId;
        }
        try {
            for (String id : mCameraManager.getCameraIdList()) {
                final CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(id);
                final Boolean flash = characteristics.get(
                        CameraCharacteristics.FLASH_INFO_AVAILABLE);
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(flash) && facing != null
                        && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mTorchCameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Log.w(TAG, "Unable to find the torch camera", e);
        }
        return mTorchCameraId;
    }
}
