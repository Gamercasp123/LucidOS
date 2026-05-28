package com.lucidos.launcher.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manager for launcher preferences and settings
 */
class PreferencesManager(context: Context) {
    private companion object {
        const val TAG = "PreferencesManager"
        const val PREFS_NAME = "lucidos_launcher_prefs"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Preferences keys
    private object Keys {
        const val GRID_ROWS = "grid_rows"
        const val GRID_COLUMNS = "grid_columns"
        const val SHOW_LABELS = "show_labels"
        const val ICON_SIZE = "icon_size"
        const val DARK_MODE = "dark_mode"
        const val ANIMATION_ENABLED = "animation_enabled"
        const val DEFAULT_SEARCH = "default_search"
    }

    // Grid settings
    fun getGridRows(): Int = preferences.getInt(Keys.GRID_ROWS, 5)
    fun setGridRows(rows: Int) = preferences.edit().putInt(Keys.GRID_ROWS, rows).apply()

    fun getGridColumns(): Int = preferences.getInt(Keys.GRID_COLUMNS, 4)
    fun setGridColumns(columns: Int) = preferences.edit().putInt(Keys.GRID_COLUMNS, columns).apply()

    // UI Settings
    fun isShowLabels(): Boolean = preferences.getBoolean(Keys.SHOW_LABELS, true)
    fun setShowLabels(show: Boolean) = preferences.edit().putBoolean(Keys.SHOW_LABELS, show).apply()

    fun getIconSize(): Int = preferences.getInt(Keys.ICON_SIZE, 72)
    fun setIconSize(size: Int) = preferences.edit().putInt(Keys.ICON_SIZE, size).apply()

    fun isDarkMode(): Boolean = preferences.getBoolean(Keys.DARK_MODE, false)
    fun setDarkMode(enabled: Boolean) = preferences.edit().putBoolean(Keys.DARK_MODE, enabled).apply()

    fun isAnimationEnabled(): Boolean = preferences.getBoolean(Keys.ANIMATION_ENABLED, true)
    fun setAnimationEnabled(enabled: Boolean) =
        preferences.edit().putBoolean(Keys.ANIMATION_ENABLED, enabled).apply()

    // Search Settings
    fun getDefaultSearchEngine(): String =
        preferences.getString(Keys.DEFAULT_SEARCH, "google") ?: "google"
    fun setDefaultSearchEngine(engine: String) =
        preferences.edit().putString(Keys.DEFAULT_SEARCH, engine).apply()

    fun resetToDefaults() {
        Log.d(TAG, "Resetting preferences to defaults")
        preferences.edit().clear().apply()
    }
}
