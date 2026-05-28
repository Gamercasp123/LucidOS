package com.lucidos.performance

/**
 * Data class representing performance event
 */
data class PerformanceEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: EventType,
    val severity: Severity,
    val message: String,
    val metadata: Map<String, Any> = emptyMap()
)

enum class EventType {
    MEMORY_CRITICAL,
    MEMORY_WARNING,
    THERMAL_WARNING,
    THERMAL_CRITICAL,
    BATTERY_LOW,
    BATTERY_CRITICAL,
    CPU_HIGH_LOAD,
    FRAME_DROP,
    APP_ANR,
    APP_CRASH,
    OPTIMIZATION_START,
    OPTIMIZATION_END
}

enum class Severity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Data class for system metrics
 */
data class SystemMetrics(
    val memoryUsagePercent: Float,
    val cpuUsagePercent: Float,
    val batteryPercent: Int,
    val temperature: Float,
    val fps: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class for memory info
 */
data class MemoryInfo(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val threshold: Long
) {
    val usagePercent: Float
        get() = (usedMemory.toFloat() / totalMemory.toFloat() * 100)
}
