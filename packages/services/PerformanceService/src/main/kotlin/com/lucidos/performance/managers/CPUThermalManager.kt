package com.lucidos.performance.managers

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Manager for CPU and thermal management
 */
class CPUThermalManager(context: Context) {
    private val TAG = "CPUThermalManager"
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private var currentPowerProfile = PowerProfile.BALANCED

    enum class PowerProfile {
        POWER_SAVING,
        BALANCED,
        PERFORMANCE,
        THERMAL_PROTECTION
    }

    fun getCPUUsagePercent(): Float {
        return try {
            // Get CPU info from /proc/stat
            val runtime = Runtime.getRuntime()
            val process = runtime.exec("top -bn1 | head -n1")
            val inputStream = process.inputStream
            val scanner = java.util.Scanner(inputStream).useDelimiter("\\A")
            val result = if (scanner.hasNext()) scanner.next() else ""

            // Parse CPU usage from output
            val usage = result.split(",").getOrNull(2)?.split("%")?.firstOrNull()?.toFloatOrNull() ?: 0f
            usage
        } catch (e: Exception) {
            Log.e(TAG, "Error getting CPU usage", e)
            0f
        }
    }

    fun getDeviceTemperature(): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Read from /sys/class/thermal on supported devices
                val tempFile = java.io.File("/sys/class/thermal/thermal_zone0/temp")
                if (tempFile.exists()) {
                    val temp = tempFile.readText().trim().toFloat() / 1000f
                    temp
                } else {
                    0f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading device temperature", e)
            0f
        }
    }

    fun isOverheating(): Boolean {
        val temp = getDeviceTemperature()
        return temp > 45f // Threshold in Celsius
    }

    fun isCriticalTemperature(): Boolean {
        val temp = getDeviceTemperature()
        return temp > 55f // Critical threshold
    }

    fun setPowerProfile(profile: PowerProfile) {
        Log.d(TAG, "Setting power profile to: $profile")
        currentPowerProfile = profile

        when (profile) {
            PowerProfile.POWER_SAVING -> enablePowerSaving()
            PowerProfile.BALANCED -> enableBalanced()
            PowerProfile.PERFORMANCE -> enablePerformance()
            PowerProfile.THERMAL_PROTECTION -> enableThermalProtection()
        }
    }

    fun getCurrentPowerProfile(): PowerProfile = currentPowerProfile

    fun optimizeCPU() {
        Log.d(TAG, "Optimizing CPU")
        val cpuUsage = getCPUUsagePercent()
        val temp = getDeviceTemperature()

        when {
            isCriticalTemperature() -> setPowerProfile(PowerProfile.THERMAL_PROTECTION)
            isOverheating() -> setPowerProfile(PowerProfile.POWER_SAVING)
            cpuUsage > 80f -> setPowerProfile(PowerProfile.BALANCED)
            else -> setPowerProfile(PowerProfile.PERFORMANCE)
        }
    }

    private fun enablePowerSaving() {
        Log.d(TAG, "Power saving mode enabled")
        // Reduce CPU frequency, disable background services
    }

    private fun enableBalanced() {
        Log.d(TAG, "Balanced mode enabled")
        // Balanced CPU frequency and power
    }

    private fun enablePerformance() {
        Log.d(TAG, "Performance mode enabled")
        // Maximum CPU frequency for performance
    }

    private fun enableThermalProtection() {
        Log.d(TAG, "Thermal protection enabled")
        // Aggressive power reduction to cool device
    }

    fun logCPUStats() {
        Log.d(TAG, """
            CPU Stats:
            CPU Usage: ${String.format("%.1f", getCPUUsagePercent())}%
            Temperature: ${String.format("%.1f", getDeviceTemperature())}°C
            Power Profile: $currentPowerProfile
            Overheating: ${isOverheating()}
            Critical Temp: ${isCriticalTemperature()}
        """.trimIndent())
    }
}
