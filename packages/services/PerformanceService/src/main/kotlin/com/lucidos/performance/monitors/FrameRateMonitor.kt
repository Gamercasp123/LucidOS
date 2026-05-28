package com.lucidos.performance.monitors

import android.util.Log
import kotlin.system.measureTimeMillis

/**
 * Monitor for frame rate and rendering performance
 */
class FrameRateMonitor {
    private const val TAG = "FrameRateMonitor"

    private var frameCount = 0
    private var lastTimestamp = System.currentTimeMillis()
    private var currentFPS = 60f
    private val targetFPS = 60f

    fun onFrameRendered() {
        frameCount++
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastTimestamp

        if (elapsed >= 1000) {
            currentFPS = (frameCount * 1000f) / elapsed
            Log.d(TAG, "FPS: ${String.format("%.1f", currentFPS)}")
            frameCount = 0
            lastTimestamp = currentTime
        }
    }

    fun getCurrentFPS(): Float = currentFPS

    fun isFrameDropping(): Boolean {
        return currentFPS < targetFPS * 0.95f // Below 95% of target
    }

    fun isSevereFrameDrop(): Boolean {
        return currentFPS < targetFPS * 0.8f // Below 80% of target
    }

    fun measureFrameTime(block: () -> Unit): Long {
        return measureTimeMillis { block() }
    }

    fun logPerformanceMetrics() {
        Log.d(TAG, """
            Frame Rate Metrics:
            Current FPS: ${String.format("%.1f", currentFPS)}
            Target FPS: $targetFPS
            Frame Dropping: ${isFrameDropping()}
            Severe Drop: ${isSevereFrameDrop()}
        """.trimIndent())
    }
}
