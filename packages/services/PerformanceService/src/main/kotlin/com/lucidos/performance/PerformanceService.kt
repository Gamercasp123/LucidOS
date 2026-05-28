package com.lucidos.performance

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.lucidos.performance.managers.BatteryManager
import com.lucidos.performance.managers.CPUThermalManager
import com.lucidos.performance.managers.MemoryManager
import com.lucidos.performance.monitors.FrameRateMonitor
import com.lucidos.performance.monitors.SystemResourceMonitor

/**
 * LucidOS Performance Service
 *
 * Core background service for system performance optimization.
 * Monitors memory, CPU, thermal, battery, and frame rate.
 * Automatically adjusts system parameters for optimal performance.
 */
class PerformanceService : Service() {
    companion object {
        private const val TAG = "PerformanceService"
        private const val MONITORING_INTERVAL = 5000L // 5 seconds
        private const val NOTIFICATION_ID = 2
    }

    private lateinit var memoryManager: MemoryManager
    private lateinit var cpuThermalManager: CPUThermalManager
    private lateinit var batteryManager: BatteryManager
    private lateinit var frameRateMonitor: FrameRateMonitor
    private lateinit var systemResourceMonitor: SystemResourceMonitor

    private val handler = Handler(Looper.getMainLooper())
    private val monitoringRunnable = object : Runnable {
        override fun run() {
            performMonitoring()
            handler.postDelayed(this, MONITORING_INTERVAL)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Performance Service created")
        initializeManagers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Performance Service started")
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Performance Service destroyed")
        stopMonitoring()
    }

    private fun initializeManagers() {
        Log.d(TAG, "Initializing performance managers")
        memoryManager = MemoryManager(this)
        cpuThermalManager = CPUThermalManager(this)
        batteryManager = BatteryManager(this)
        frameRateMonitor = FrameRateMonitor()
        systemResourceMonitor = SystemResourceMonitor(
            memoryManager,
            cpuThermalManager,
            batteryManager,
            frameRateMonitor
        )
    }

    private fun startMonitoring() {
        Log.d(TAG, "Starting performance monitoring")
        handler.post(monitoringRunnable)
    }

    private fun stopMonitoring() {
        Log.d(TAG, "Stopping performance monitoring")
        handler.removeCallbacks(monitoringRunnable)
    }

    private fun performMonitoring() {
        try {
            // Collect metrics
            val metrics = systemResourceMonitor.getSystemMetrics()
            val health = systemResourceMonitor.getHealthStatus()

            // Log metrics
            Log.d(TAG, "Health: ${health.score}/100 - ${health.status}")

            // Perform optimizations based on conditions
            when {
                memoryManager.isCriticalMemory() -> {
                    Log.w(TAG, "Critical memory usage detected")
                    memoryManager.killLowPriorityApps()
                }
                memoryManager.isMemoryLow() -> {
                    Log.w(TAG, "Low memory warning")
                    memoryManager.optimizeMemory()
                }
            }

            // CPU/Thermal optimization
            when {
                cpuThermalManager.isCriticalTemperature() -> {
                    Log.e(TAG, "Critical temperature - activating thermal protection")
                    cpuThermalManager.setPowerProfile(CPUThermalManager.PowerProfile.THERMAL_PROTECTION)
                }
                cpuThermalManager.isOverheating() -> {
                    Log.w(TAG, "Device overheating - switching to power saving")
                    cpuThermalManager.setPowerProfile(CPUThermalManager.PowerProfile.POWER_SAVING)
                }
            }

            // Battery optimization
            batteryManager.optimizeBattery()

            // Log detailed stats periodically
            if (System.currentTimeMillis() % 30000 == 0L) {
                memoryManager.logMemoryStats()
                cpuThermalManager.logCPUStats()
                batteryManager.logBatteryStats()
                frameRateMonitor.logPerformanceMetrics()
                systemResourceMonitor.logSystemMetrics()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during performance monitoring", e)
        }
    }

    private fun createNotification() = android.app.Notification.Builder(this)
        .setContentTitle("LucidOS Performance")
        .setContentText("Optimizing system performance")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(android.app.Notification.PRIORITY_LOW)
        .build()
}
