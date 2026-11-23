/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blackberry.settings;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings;

/**
 * Single source of truth for the keyboard touchpad power state, shared by the
 * settings page and the Quick Settings tile.
 *
 * The persistent vendor property is what init uses to power the capacitive
 * keyboard on or off, so the state survives reboots. The global setting only
 * mirrors it so observers (the tile, the settings page) get a change URI.
 */
final class KeyboardTouchpad {
    static final String SETTING_ENABLED = "keyboard_touchpad_enabled";
    private static final String PROP_DISABLED = "persist.vendor.touchkeypad.disabled";

    private KeyboardTouchpad() {
    }

    static boolean isEnabled() {
        return !SystemProperties.getBoolean(PROP_DISABLED, false);
    }

    static void setEnabled(Context context, boolean enabled) {
        SystemProperties.set(PROP_DISABLED, enabled ? "0" : "1");
        Settings.Global.putInt(context.getContentResolver(), SETTING_ENABLED, enabled ? 1 : 0);
    }

    static Uri getObservedUri() {
        return Settings.Global.getUriFor(SETTING_ENABLED);
    }
}
