package com.lucidos.lockscreen.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Lock Screen Service - Manages lock screen state
 */
class LockScreenService : Service() {
    companion object {
        private const val TAG = "LockScreenService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Lock Screen Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Lock Screen Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Lock Screen Service destroyed")
    }
}
