package com.lucidos.launcher.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Boot Receiver
 *
 * Handles device boot completion events.
 */
class BootReceiver : BroadcastReceiver() {
    private companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed")
            startLauncherService(context)
        }
    }

    private fun startLauncherService(context: Context) {
        Log.d(TAG, "Starting launcher service after boot")
        val serviceIntent = Intent(context, com.lucidos.launcher.service.LauncherService::class.java)
        context.startService(serviceIntent)
    }
}
