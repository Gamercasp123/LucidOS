package com.lucidos.launcher

import android.app.Application
import android.util.Log
import com.lucidos.launcher.data.AppRepository
import com.lucidos.launcher.data.PreferencesManager

/**
 * LucidOS Launcher Application
 *
 * Main application class for the Launcher component.
 * Manages global launcher state and initialization.
 */
class LauncherApplication : Application() {
    companion object {
        private const val TAG = "LauncherApplication"
        lateinit var instance: LauncherApplication
            private set
    }

    lateinit var appRepository: AppRepository
        private set
    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d(TAG, "Initializing LucidOS Launcher")

        // Initialize managers
        preferencesManager = PreferencesManager(this)
        appRepository = AppRepository(this)

        // Load initial data
        appRepository.loadInstalledApps()
    }
}
