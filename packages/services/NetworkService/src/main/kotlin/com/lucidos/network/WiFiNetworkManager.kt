package com.lucidos.network

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.util.Log

/**
 * WiFi network manager with scanning and connection
 */
class WiFiNetworkManager(private val context: Context) {
    private const val TAG = "WiFiNetworkManager"
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    fun startWifiScan() {
        try {
            Log.d(TAG, "Starting WiFi scan")
            wifiManager?.startScan()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start WiFi scan", e)
        }
    }

    fun getAvailableNetworks(): List<String> {
        return try {
            Log.d(TAG, "Retrieving available WiFi networks")
            val results = wifiManager?.scanResults ?: emptyList()
            results.map { it.SSID }.distinct()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi networks", e)
            emptyList()
        }
    }

    fun isWiFiEnabled(): Boolean = wifiManager?.isWifiEnabled ?: false

    fun setWiFiEnabled(enabled: Boolean) {
        try {
            Log.d(TAG, "Setting WiFi: $enabled")
            wifiManager?.isWifiEnabled = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set WiFi state", e)
        }
    }

    fun getWiFiSignalStrength(): Int {
        return try {
            val connectionInfo = wifiManager?.connectionInfo
            val rssi = connectionInfo?.rssi ?: -100
            WifiManager.calculateSignalLevel(rssi, 5)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting signal strength", e)
            0
        }
    }

    fun logWiFiInfo() {
        Log.d(TAG, """
            WiFi Status:
            Enabled: ${isWiFiEnabled()}
            Signal Strength: ${getWiFiSignalStrength()}/5
            Available Networks: ${getAvailableNetworks().size}
        """.trimIndent())
    }
}
