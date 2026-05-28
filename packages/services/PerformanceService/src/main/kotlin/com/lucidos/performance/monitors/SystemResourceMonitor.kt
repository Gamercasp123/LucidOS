package com.lucidos.performance.monitors

import android.util.Log
import com.lucidos.performance.SystemMetrics

/**
 * Monitor for overall system resource usage
 */
class SystemResourceMonitor(
    private val memoryManager: com.lucidos.performance.managers.MemoryManager,
    private val cpuThermalManager: com.lucidos.performance.managers.CPUThermalManager,
    private val batteryManager: com.lucidos.performance.managers.BatteryManager,
    private val frameRateMonitor: FrameRateMonitor
) {
    private const val TAG = "SystemResourceMonitor"

    fun getSystemMetrics(): SystemMetrics {
        return SystemMetrics(
            memoryUsagePercent = memoryManager.getMemoryUsagePercent(),
            cpuUsagePercent = cpuThermalManager.getCPUUsagePercent(),
            batteryPercent = batteryManager.getBatteryLevel(),
            temperature = cpuThermalManager.getDeviceTemperature(),
            fps = frameRateMonitor.getCurrentFPS()
        )
    }

    fun getHealthStatus(): HealthStatus {
        val metrics = getSystemMetrics()

        var score = 100

        // Memory health
        score -= when {
            metrics.memoryUsagePercent > 95 -> 30
            metrics.memoryUsagePercent > 85 -> 15
            metrics.memoryUsagePercent > 70 -> 5
            else -> 0
        }

        // CPU health
        score -= when {
            metrics.cpuUsagePercent > 90 -> 20
            metrics.cpuUsagePercent > 70 -> 10
            else -> 0
        }

        // Temperature health
        score -= when {
            metrics.temperature > 55 -> 25
            metrics.temperature > 45 -> 10
            else -> 0
        }

        // Battery health
        score -= when {
            metrics.batteryPercent < 5 -> 20
            metrics.batteryPercent < 20 -> 10
            else -> 0
        }

        // Frame rate health
        score -= when {
            metrics.fps < 30 -> 25
            metrics.fps < 50 -> 10
            else -> 0
        }

        val status = when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Fair"
            score >= 20 -> "Poor"
            else -> "Critical"
        }

        return HealthStatus(score.coerceIn(0, 100), status)
    }

    fun logSystemMetrics() {
        val metrics = getSystemMetrics()
        val health = getHealthStatus()

        Log.d(TAG, """
            System Metrics:
            Memory: ${String.format("%.1f", metrics.memoryUsagePercent)}%
            CPU: ${String.format("%.1f", metrics.cpuUsagePercent)}%
            Battery: ${metrics.batteryPercent}%
            Temperature: ${String.format("%.1f", metrics.temperature)}°C
            FPS: ${String.format("%.1f", metrics.fps)}
            Health Score: ${health.score}/100 (${health.status})
        """.trimIndent())
    }

    data class HealthStatus(val score: Int, val status: String)
}
