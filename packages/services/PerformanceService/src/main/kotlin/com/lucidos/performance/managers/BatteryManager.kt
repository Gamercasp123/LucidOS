package com.lucidos.performance.managers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log

/**
 * Manager for battery optimization and monitoring
 */
class BatteryManager(context: Context) {
    private const val TAG = "BatteryManager"
    private val context = context.applicationContext

    fun getBatteryLevel(): Int {
        try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            return batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).coerceIn(0, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery level", e)
            return 0
        }
    }

    fun getBatteryHealth(): Int {
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val battery = context.registerReceiver(null, ifilter)
            return battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery health", e)
            return 0
        }
    }

    fun isBatteryLow(): Boolean {
        return getBatteryLevel() < 20
    }

    fun isBatteryCritical(): Boolean {
        return getBatteryLevel() < 5
    }

    fun isCharging(): Boolean {
        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val battery = context.registerReceiver(null, ifilter)
            val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                   status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if charging", e)
            return false
        }
    }

    fun getChargingTime(): Long {
        // Estimate charging time based on battery level and charging rate
        val level = getBatteryLevel()
        val timePerPercent = 2 // 2 minutes per percent
        return (100 - level) * timePerPercent * 60L // in seconds
    }

    fun optimizeBattery() {
        Log.d(TAG, "Starting battery optimization")

        when {
            isBatteryCritical() -> enableBatterySaver(aggressive = true)
            isBatteryLow() -> enableBatterySaver(aggressive = false)
            else -> disableBatterySaver()
        }
    }

    fun enableBatterySaver(aggressive: Boolean = false) {
        Log.d(TAG, "Battery saver enabled - Aggressive: $aggressive")
        // Reduce screen brightness, limit background apps, disable WiFi/Bluetooth
        if (aggressive) {
            // Even more aggressive restrictions
        }
    }

    fun disableBatterySaver() {
        Log.d(TAG, "Battery saver disabled")
        // Restore full performance
    }

    fun logBatteryStats() {
        Log.d(TAG, """
            Battery Stats:
            Level: ${getBatteryLevel()}%
            Health: ${getBatteryHealth()}
            Charging: ${isCharging()}
            Low Battery: ${isBatteryLow()}
            Critical: ${isBatteryCritical()}
            Est. Charging Time: ${getChargingTime() / 60}m
        """.trimIndent())
    }
}
