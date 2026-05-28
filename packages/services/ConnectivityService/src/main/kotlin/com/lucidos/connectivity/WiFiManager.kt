package com.lucidos.connectivity

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log

class WiFiManager(private val context: Context) {
    private val TAG = "WiFiManager"
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    fun isWiFiEnabled(): Boolean {
        return try {
            wifiManager?.isWifiEnabled ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking WiFi state", e)
            false
        }
    }

    fun setWiFiEnabled(enabled: Boolean) {
        try {
            Log.d(TAG, "Setting WiFi: $enabled")
            wifiManager?.let {
                it.isWifiEnabled = enabled
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set WiFi state", e)
        }
    }
}
