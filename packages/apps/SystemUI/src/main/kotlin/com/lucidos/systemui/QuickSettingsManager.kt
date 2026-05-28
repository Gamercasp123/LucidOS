package com.lucidos.systemui

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.lucidos.connectivity.WiFiManager
import com.lucidos.connectivity.BluetoothManager
import com.lucidos.connectivity.NfcManager

/**
 * Manager for Quick Settings system component
 */
object QuickSettingsManager {
    private const val TAG = "QuickSettingsManager"
    private var wifiManager: WiFiManager? = null
    private var bluetoothManager: BluetoothManager? = null
    private var nfcManager: NfcManager? = null
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        Log.d(TAG, "Initializing Quick Settings")
        val appCtx = context.applicationContext
        applicationContext = appCtx
        wifiManager = WiFiManager(appCtx)
        bluetoothManager = BluetoothManager(appCtx)
        nfcManager = NfcManager(appCtx)
    }

    fun toggleWiFi(enabled: Boolean) {
        Log.d(TAG, "WiFi: ${if (enabled) "ON" else "OFF"}")
        wifiManager?.setWiFiEnabled(enabled)
    }

    fun toggleBluetooth(enabled: Boolean) {
        Log.d(TAG, "Bluetooth: ${if (enabled) "ON" else "OFF"}")
        bluetoothManager?.setBluetoothEnabled(enabled)
    }

    fun toggleNfc(enabled: Boolean) {
        Log.d(TAG, "NFC: ${if (enabled) "ON" else "OFF"}")
        nfcManager?.setNfcEnabled(enabled)
    }

    fun isWiFiEnabled(): Boolean = wifiManager?.isWiFiEnabled() ?: false
    fun isBluetoothEnabled(): Boolean = bluetoothManager?.isBluetoothEnabled() ?: false
    fun isNfcEnabled(): Boolean = nfcManager?.isNfcEnabled() ?: false

    fun setScreenBrightness(brightness: Int) {
        Log.d(TAG, "Screen brightness: $brightness%")
        val ctx = applicationContext ?: return
        try {
            // Brightness values scale from 0 to 255
            val brightnessVal = (brightness * 255) / 100
            Settings.System.putInt(
                ctx.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessVal
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen brightness", e)
        }
    }

    fun toggleAirplaneMode(enabled: Boolean) {
        Log.d(TAG, "Airplane mode: ${if (enabled) "ON" else "OFF"}")
        val ctx = applicationContext ?: return
        try {
            Settings.Global.putInt(
                ctx.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                if (enabled) 1 else 0
            )
            ctx.sendBroadcast(Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", enabled)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle airplane mode", e)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        Log.d(TAG, "Dark mode: ${if (enabled) "ON" else "OFF"}")
        val ctx = applicationContext ?: return
        try {
            val uiModeManager = ctx.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            uiModeManager?.nightMode = if (enabled) {
                UiModeManager.MODE_NIGHT_YES
            } else {
                UiModeManager.MODE_NIGHT_NO
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle dark mode", e)
        }
    }
}
