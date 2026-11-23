/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.blackberry.settings;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/**
 * Quick Settings tile that powers the capacitive keyboard touchpad on or off.
 * Long-pressing the tile opens the BlackBerry Keyboard settings page through
 * the QS_TILE_PREFERENCES intent filter of DeviceSettingsActivity.
 */
public class KeyboardTouchpadTileService extends TileService {

    private final ContentObserver mObserver = new ContentObserver(
            new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            updateTile();
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();
        getContentResolver().registerContentObserver(
                KeyboardTouchpad.getObservedUri(), false, mObserver);
        updateTile();
    }

    @Override
    public void onStopListening() {
        getContentResolver().unregisterContentObserver(mObserver);
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        KeyboardTouchpad.setEnabled(this, !KeyboardTouchpad.isEnabled());
        updateTile();
    }

    private void updateTile() {
        final Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        final boolean enabled = KeyboardTouchpad.isEnabled();
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setSubtitle(getString(enabled ? R.string.tile_state_on : R.string.tile_state_off));
        tile.updateTile();
    }
}
