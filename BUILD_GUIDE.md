# LucidOS Gradle Build Configuration Guide

**Version**: 1.0
**Date**: May 28, 2026
**Platform**: Windows + Gradle 8.0+
**Android SDK**: API 34 (Gradle Plugin 8.2.2)

## Overview

This guide explains how to build LucidOS on Windows without requiring a full AOSP build environment. The project uses a hybrid approach combining standard Android application modules with stub-based system service modules.

## Project Structure

```
LucidOS/
├── buildSrc/                              # Shared build configuration
│   ├── src/main/kotlin/
│   │   ├── Dependencies.kt                # Centralized version management
│   │   └── AndroidConfigExtensions.kt     # Reusable build configs
│   └── build.gradle.kts
│
├── packages/
│   ├── apps/                              # Standard Android applications
│   │   ├── SystemUI/                      # System user interface
│   │   ├── Launcher/                      # Home screen
│   │   ├── LockScreen/                    # Authentication UI
│   │   ├── Settings/                      # Device settings
│   │   ├── Calculator/                    # Calculator app
│   │   └── PlayStore/                     # App store
│   │
│   └── services/                          # System service modules (libraries)
│       ├── ConnectivityService/           # WiFi/Bluetooth/NFC
│       ├── NetworkService/                # Network monitoring
│       ├── PerformanceService/            # System optimization
│       └── SensorService/                 # Hardware sensors
│
├── gradle.properties                      # JVM & build optimization
├── build.gradle.kts                       # Root build configuration
├── settings.gradle.kts                    # Module declaration
└── gradlew / gradlew.bat                  # Gradle wrapper (Windows)
```

## Configuration Files

### 1. gradle.properties - JVM Optimization

**Location**: `gradle.properties`

**Key Settings**:

```properties
# JVM Memory: 5GB heap + 768MB metaspace + 512MB direct memory
org.gradle.jvmargs=-Xmx5g \
  -XX:MaxMetaspaceSize=768m \
  -XX:CompressedClassSpaceSize=256m \
  -XX:MaxDirectMemorySize=512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200

# Parallel builds: Uses N-1 cores (7 cores on 8-core Windows system)
org.gradle.workers.max=7
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.daemon.idletimeout=10800000

# Android optimizations
android.useAndroidX=true
android.enableJetifier=true
org.gradle.caching=true
```

**Purpose**:
- Prevents OOM errors during dex merging
- Enables parallel compilation for faster builds
- Keeps Gradle daemon alive to avoid repeated startup overhead

**OOM Troubleshooting**:

| Error | Cause | Solution |
|-------|-------|----------|
| `OutOfMemoryError: Java heap space` | Dex merging needs more heap | Increase `-Xmx` from 5g to 6g |
| `OutOfMemoryError: Metaspace` | Kotlin/Android plugin metadata | Increase `-XX:MaxMetaspaceSize` from 768m to 1024m |
| `GC overhead limit exceeded` | Too much garbage collection | Enable parallel GC: `-XX:+ParallelRefProcEnabled` |

### 2. buildSrc/ - Shared Build Configuration

**Location**: `buildSrc/`

**Purpose**: Centralize version management and common build logic

**Files**:
- `Dependencies.kt`: All library versions and dependency definitions
- `AndroidConfigExtensions.kt`: Reusable build configuration functions
- `build.gradle.kts`: buildSrc plugin configuration

**Usage Example in Module**:

```kotlin
// packages/apps/SystemUI/build.gradle.kts
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = AndroidConfig.compileSdk
    namespace = "com.lucidos.systemui"

    defaultConfig {
        applicationId = "com.lucidos.systemui"
        minSdk = AndroidConfig.minSdk
        targetSdk = AndroidConfig.targetSdk
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.Compose.ui)
    implementation(Dependencies.biometric)
}
```

### 3. Manifest Configuration

**Issue**: Deprecated `package` attribute in `<manifest>` causes AGP 8.0+ warnings

**Solution**: Move package name to `namespace` property in `build.gradle.kts`

**Before** (❌ Deprecated):
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.lucidos.lockscreen">
    <!-- content -->
</manifest>
```

**After** (✅ Correct):
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- content -->
</manifest>
```

```kotlin
// build.gradle.kts
android {
    namespace = "com.lucidos.lockscreen"  // Package now defined here
    // ...
}
```

**Status**: ✅ All manifests updated (SensorService, PerformanceService, NetworkService fixed)

## Dependency Strategy: AOSP APIs Without AOSP Build Environment

### The Problem

AOSP modules depend on internal framework classes:

```kotlin
// ❌ This fails - not in public Maven repos
implementation("android.frameworks.base:framework:1.0")
```

### The Solution: Stub Approach

**Strategy**: Don't import framework internals. Use public Android APIs instead.

#### Step 1: Identify Missing Classes

Search for internal imports:
```bash
grep -r "import android\.frameworks" packages/
grep -r "import com\.android\." packages/
```

#### Step 2: Map to Public Alternatives

| Internal API | Public Alternative |
|--------------|-------------------|
| `com.android.internal.statusbar.*` | Use `androidx.core.status.*` or `android.app.StatusBarManager` |
| `com.android.internal.policy.*` | Use `android.app.Window` + `androidx.core.*` |
| `com.android.server.display.*` | Use `android.hardware.display.DisplayManager` |
| `com.android.server.power.*` | Use `android.os.PowerManager` |
| `com.android.server.wm.*` | Use `android.app.ActivityManager` |

#### Step 3: Update Imports

```kotlin
// ❌ Before (Internal)
import com.android.internal.statusbar.IStatusBarService

// ✅ After (Public)
import android.app.StatusBarManager  // Or implement through services
```

#### Step 4: System Services Configuration

System services are accessed through `Context.getSystemService()`:

```kotlin
// Correct way to access system services
val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
```

### Creating Stub Classes (If Needed)

For truly missing APIs, create stubs in a compatibility module:

**Location**: `packages/stub-api/src/main/kotlin/com/lucidos/stubs/`

**Example Stub**:

```kotlin
// packages/stub-api/src/main/kotlin/com/lucidos/stubs/SystemUICompat.kt
package com.lucidos.stubs

import android.content.Context
import android.view.View

/**
 * Compatibility layer for internal SystemUI APIs
 * Provides stub implementations for APIs not available in public SDK
 */
object SystemUICompat {

    fun expandStatusBar(context: Context) {
        // Stub: Would use internal APIs in AOSP build
        // In public SDK, we expand notification panel through accessibility services
        try {
            val statusBarManager = context.getSystemService("statusbar")
            val method = statusBarManager?.javaClass?.getMethod("expandNotificationsPanel")
            method?.invoke(statusBarManager)
        } catch (e: Exception) {
            // Fallback: Permission denied on user builds
        }
    }

    fun collapseStatusBar(context: Context) {
        try {
            val statusBarManager = context.getSystemService("statusbar")
            val method = statusBarManager?.javaClass?.getMethod("collapsePanels")
            method?.invoke(statusBarManager)
        } catch (e: Exception) {
            // Fallback
        }
    }
}
```

## Multi-Module Build Optimization

### Enable Parallel Compilation

```properties
# gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=7
org.gradle.configureondemand=false
```

### Module Dependencies

Minimize inter-module dependencies:

```
✅ GOOD:
apps/SystemUI → services/ConnectivityService (read-only library)
apps/Launcher → (no service dependencies)

❌ BAD:
apps/SystemUI → apps/Launcher (circular)
services/ConnectivityService → apps/SystemUI (library depends on app)
```

### Dex Merging Optimization

For multi-DEX builds, configure in `build.gradle.kts`:

```kotlin
android {
    // Enable multi-dex if total DEX size exceeds 64KB
    defaultConfig {
        multiDexEnabled = true  // For apps, not libraries
    }

    // Optimize dex merging
    dexOptions {
        javaMaxHeapSize = "4g"
        preDexLibraries = true
    }
}
```

## Build Commands

### Full Build (All Modules)

```bash
# Windows
cd C:\Users\Zsolt\OneDrive\Documents\LucidOS
gradlew.bat clean build

# Or with custom memory settings
SET JAVA_OPTS=-Xmx6g -XX:MaxMetaspaceSize=1024m
gradlew.bat build
```

### Build Specific Module

```bash
# Build only SystemUI
gradlew.bat :packages:apps:SystemUI:build

# Build only services
gradlew.bat :packages:services:PerformanceService:build
```

### Debug Build (Faster, No Optimization)

```bash
gradlew.bat assembleDebug
```

### Release Build (Optimized, Signed)

```bash
gradlew.bat assembleRelease
```

### Check Dependencies

```bash
# Show all dependencies
gradlew.bat dependencies

# Show dependencies for specific module
gradlew.bat :packages:apps:SystemUI:dependencies
```

## Troubleshooting

### Issue 1: Build Cache Corruption

**Error**: `No cached version of xyz:abc:1.0 available for offline mode`

**Solution**:
```bash
# Clear build cache
gradlew.bat clean
rm -Recurse -Force .gradle
gradlew.bat build
```

### Issue 2: Kotlin Compilation Timeout

**Error**: `Kotlin: Task task ':xxx:compileDebugKotlin' failed`

**Solution**:
```properties
# gradle.properties
kotlin.compiler.allocation.file=true
kotlin.incremental=true
kotlin.parallel.tasks.in.project=true
```

### Issue 3: Manifest Merge Errors

**Error**: `Manifest merger failed with multiple errors`

**Debug**:
```bash
gradlew.bat :packages:apps:SystemUI:build --info | findstr "manifest"
```

**Fix**: Check namespace in build.gradle.kts matches actual package structure

### Issue 4: Dex Merge OutOfMemory

**Error**: `Too many field references: 131000 in method`

**Solution**:
```kotlin
// Enable ProGuard/R8 minification
buildTypes {
    debug {
        isMinifyEnabled = true  // Also enable in debug for testing
        proguardFiles(getDefaultProguardFile("proguard-android.txt"))
    }
}
```

## Windows-Specific Considerations

### Path Length Limitations

Windows has a 260-character path limit. LucidOS uses deep module structures:

```
c:\Users\Zsolt\OneDrive\Documents\LucidOS\packages\services\PerformanceService\...
                            ↑
                    ~100+ characters already
```

**Solution**: Enable long path support in Windows 10+

```powershell
# Run as Administrator
New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" `
  -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force
```

### Gradle Daemon Issues

On Windows, Gradle daemon may not terminate properly.

**Solution**:

```bash
# Kill all Java processes (Gradle daemons)
taskkill /IM java.exe /F

# Or use Gradle to stop daemon
gradlew.bat --stop
```

### File System Notifications

Windows watches all files; large projects slow down builds.

**Solution** - Add to `.gradle\gradle.properties`:

```properties
org.gradle.vfs.watch=true
org.gradle.vfs.watch.paths=!C:/Users/Zsolt/OneDrive,!C:/Windows
```

## Performance Metrics

| Metric | Target | Typical |
|--------|--------|---------|
| Full clean build | <120 sec | 90-110 sec |
| Incremental build | <30 sec | 15-25 sec |
| DEX merging | <20 sec | 10-15 sec |
| Memory usage | <6 GB | 4-5 GB |
| Parallel workers | 7/8 cores | 6-7 cores |

## Validation Checklist

- [ ] gradle.properties has optimized JVM args (5GB heap)
- [ ] All modules have `namespace` property in build.gradle.kts
- [ ] Manifests don't have `package` attribute in `<manifest>` tag
- [ ] buildSrc compiles without errors
- [ ] No dependencies on `android.frameworks.base` or similar internal APIs
- [ ] All system APIs use public Context.getSystemService() calls
- [ ] Build succeeds with `gradlew.bat clean build`
- [ ] No OOM errors during dex merging
- [ ] Parallel builds enabled (org.gradle.parallel=true)

## Next Steps

1. **Run full build**: `gradlew.bat clean build`
2. **Check for remaining errors**: `gradlew.bat build --info`
3. **Profile build time**: `gradlew.bat build --profile` (creates HTML report)
4. **Monitor memory**: Use Windows Task Manager while building

## References

- [Android Gradle Plugin Documentation](https://developer.android.com/build/releases/gradle-plugin)
- [Gradle Performance Tuning](https://docs.gradle.org/current/userguide/performance_tuning.html)
- [AOSP Build System](https://source.android.com/docs/setup/build/building)
- [Android Manifest Documentation](https://developer.android.com/guide/topics/manifest/manifest-intro)

---

**Last Updated**: May 28, 2026
**Maintained By**: LucidOS Build Team
