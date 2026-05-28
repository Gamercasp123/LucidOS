package com.lucidos.launcher.data

/**
 * Data class representing an installed app
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: android.graphics.drawable.Drawable?,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0,
    val lastUpdateTime: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppInfo) return false
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}

/**
 * Data class representing a home screen widget
 */
data class WidgetInfo(
    val widgetId: Int,
    val widgetName: String,
    val packageName: String,
    val isEnabled: Boolean = true,
    val position: Int = 0
)

/**
 * Data class representing home screen grid position
 */
data class GridPosition(
    val row: Int,
    val column: Int
)

/**
 * Data class representing a home screen item
 */
data class HomeScreenItem(
    val id: String,
    val type: ItemType,
    val position: GridPosition,
    val data: Any // AppInfo, WidgetInfo, or custom data
)

enum class ItemType {
    APP,
    WIDGET,
    SHORTCUT,
    FOLDER
}
