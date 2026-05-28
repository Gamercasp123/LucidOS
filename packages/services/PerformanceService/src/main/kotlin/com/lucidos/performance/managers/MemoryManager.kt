package com.lucidos.performance.managers

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.lucidos.performance.MemoryInfo

/**
 * Manager for memory optimization and management
 */
class MemoryManager(context: Context) {
    private val TAG = "MemoryManager"
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getMemoryInfo(): MemoryInfo {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        return MemoryInfo(
            totalMemory = memInfo.totalMem,
            availableMemory = memInfo.availMem,
            usedMemory = memInfo.totalMem - memInfo.availMem,
            threshold = memInfo.threshold
        )
    }

    fun getMemoryUsagePercent(): Float {
        val memInfo = getMemoryInfo()
        return memInfo.usagePercent
    }

    fun isMemoryLow(): Boolean {
        val memInfo = getMemoryInfo()
        return memInfo.usagePercent > 85f
    }

    fun isCriticalMemory(): Boolean {
        val memInfo = getMemoryInfo()
        return memInfo.usagePercent > 95f
    }

    fun optimizeMemory() {
        Log.d(TAG, "Starting memory optimization")
        System.gc()
        Log.d(TAG, "Memory optimization complete")
    }

    fun clearAppCache() {
        Log.d(TAG, "Clearing app cache")
        // Clear cache implementation
    }

    fun killLowPriorityApps() {
        Log.d(TAG, "Killing low priority apps")
        // Implement app termination logic
    }

    fun logMemoryStats() {
        val memInfo = getMemoryInfo()
        Log.d(TAG, """
            Memory Stats:
            Total: ${formatBytes(memInfo.totalMemory)}
            Available: ${formatBytes(memInfo.availableMemory)}
            Used: ${formatBytes(memInfo.usedMemory)}
            Usage: ${String.format("%.1f", memInfo.usagePercent)}%
        """.trimIndent())
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
