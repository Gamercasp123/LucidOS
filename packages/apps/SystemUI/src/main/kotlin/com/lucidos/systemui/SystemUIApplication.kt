package com.lucidos.systemui

import android.app.Application

/**
 * LucidOS System UI Application
 *
 * Main application class for the System UI component.
 * Handles initialization of core system UI services.
 */
class SystemUIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeSystemUI()
    }

    private fun initializeSystemUI() {
        // Initialize status bar
        StatusBarManager.initialize(this)

        // Initialize notification center
        NotificationCenterManager.initialize(this)

        // Initialize quick settings
        QuickSettingsManager.initialize(this)

        // Initialize navigation bar
        NavigationBarManager.initialize(this)
    }
}
