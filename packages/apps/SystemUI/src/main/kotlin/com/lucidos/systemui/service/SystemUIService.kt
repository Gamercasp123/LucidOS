package com.lucidos.systemui.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Core System UI Service
 *
 * Main service that manages all system UI components and services.
 */
class SystemUIService : Service() {
    private val TAG = "SystemUIService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SystemUI Service created")
        initializeServices()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SystemUI Service started")
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SystemUI Service destroyed")
    }

    private fun initializeServices() {
        // Initialize all system UI services
        Log.d(TAG, "Initializing system UI services")
    }

    private fun createNotification() = android.app.Notification.Builder(this)
        .setContentTitle("LucidOS System UI")
        .setContentText("System UI Service Running")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .build()

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
