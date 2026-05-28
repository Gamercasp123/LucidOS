package com.lucidos.lockscreen.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lucidos.lockscreen.ui.LockScreenActivity

/**
 * Boot Receiver - Starts lock screen service on device boot
 */
class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed - initializing lock screen")

            val lockScreenIntent = Intent(context, LockScreenActivity::class.java)
            lockScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(lockScreenIntent)
        }
    }
}
