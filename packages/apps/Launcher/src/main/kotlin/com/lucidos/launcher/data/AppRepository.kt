package com.lucidos.launcher.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import java.util.Collections

/**
 * Repository for managing installed applications
 */
class AppRepository(private val context: Context) {
    private const val TAG = "AppRepository"

    private val packageManager: PackageManager = context.packageManager
    private val appList = mutableListOf<AppInfo>()

    fun loadInstalledApps() {
        Log.d(TAG, "Loading installed applications")
        appList.clear()

        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            try {
                val appName = packageManager.getApplicationLabel(packageInfo).toString()
                val appIcon = packageManager.getApplicationIcon(packageInfo.packageName)
                val isSystemApp = (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val appInfo = AppInfo(
                    packageName = packageInfo.packageName,
                    appName = appName,
                    appIcon = appIcon,
                    isSystemApp = isSystemApp,
                    installTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime
                )

                appList.add(appInfo)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading app: ${packageInfo.packageName}", e)
            }
        }

        // Sort apps by name
        Collections.sort(appList) { a, b -> a.appName.compareTo(b.appName) }
        Log.d(TAG, "Loaded ${appList.size} apps")
    }

    fun getAppList(): List<AppInfo> = appList.toList()

    fun getUserApps(): List<AppInfo> = appList.filter { !it.isSystemApp }

    fun getSystemApps(): List<AppInfo> = appList.filter { it.isSystemApp }

    fun searchApps(query: String): List<AppInfo> {
        return appList.filter {
            it.appName.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
        }
    }

    fun getAppByPackage(packageName: String): AppInfo? {
        return appList.firstOrNull { it.packageName == packageName }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return appList.any { it.packageName == packageName }
    }
}
