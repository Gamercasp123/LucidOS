package com.lucidos.connectivity

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Connectivity helper service (lightweight)
 */
class ConnectivityService : Service() {
    companion object {
        private const val TAG = "ConnectivityService"
    }

    private lateinit var wifiManager: WiFiManager
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var nfcManager: NfcManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Connectivity Service created")
        wifiManager = WiFiManager(this)
        bluetoothManager = BluetoothManager(this)
        nfcManager = NfcManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Connectivity Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Connectivity Service destroyed")
    }

    fun getWiFiManager(): WiFiManager = wifiManager
    fun getBluetoothManager(): BluetoothManager = bluetoothManager
    fun getNfcManager(): NfcManager = nfcManager
}
