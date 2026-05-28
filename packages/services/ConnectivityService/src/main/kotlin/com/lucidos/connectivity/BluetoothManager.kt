package com.lucidos.connectivity

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log

class BluetoothManager(private val context: Context) {
    private val TAG = "BluetoothManager"
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun isBluetoothEnabled(): Boolean {
        return try {
            adapter?.isEnabled ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Bluetooth state", e)
            false
        }
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        try {
            Log.d(TAG, "Setting Bluetooth: $enabled")
            adapter?.let {
                if (enabled && !it.isEnabled) it.enable()
                if (!enabled && it.isEnabled) it.disable()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set Bluetooth state", e)
        }
    }
}
