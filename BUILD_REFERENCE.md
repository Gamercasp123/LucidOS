# LucidOS Build Configuration Reference Card

**Quick Access**: Commands, configurations, and common tasks

---

## Build Commands

```bash
# Navigate to project
cd C:\Users\Zsolt\OneDrive\Documents\LucidOS

# FULL BUILD
build.bat                          # Clean + Build all modules (~90-110s)
gradlew.bat clean build            # Manual equivalent

# FAST BUILDS
build.bat --debug                  # Debug APKs only (~30-45s)
gradlew.bat assembleDebug          # Manual equivalent

# RELEASE BUILD
build.bat --release                # Optimized + Minified release APKs
gradlew.bat assembleRelease        # Manual equivalent

# PROFILING
build.bat --profile                # Build + Performance analysis
build.bat --clean-cache            # Clean all caches + rebuild

# SINGLE MODULE
gradlew.bat :packages:apps:SystemUI:build
gradlew.bat :packages:services:PerformanceService:build

# DAEMON CONTROL
gradlew.bat --status               # Show running daemon
gradlew.bat --stop                 # Stop daemon (faster than kill)

# DEPENDENCY ANALYSIS
gradlew.bat dependencies           # Show all dependencies
gradlew.bat :packages:apps:SystemUI:dependencies
gradlew.bat dependencyInsight --dependency androidx.core
gradlew.bat dependencyUpdates      # Check for newer versions
```

---

## Configuration Files

### gradle.properties
```properties
# MEMORY (OOM Prevention)
org.gradle.jvmargs=-Xmx5g                          # 5GB heap
org.gradle.jvmargs=-XX:MaxMetaspaceSize=768m      # Kotlin metadata
org.gradle.jvmargs=-XX:+UseG1GC                   # Better GC
org.gradle.jvmargs=-XX:+ParallelRefProcEnabled    # Parallel refs

# PARALLELIZATION (Speed)
org.gradle.parallel=true                          # Parallel tasks
org.gradle.workers.max=7                          # N-1 cores
org.gradle.daemon=true                            # Keep daemon alive
org.gradle.daemon.idletimeout=10800000            # 3 hour timeout

# OPTIMIZATION
org.gradle.caching=true                           # Task output cache
org.gradle.configureondemand=false                # Stable builds

# LOGGING
org.gradle.logging.level=info                     # Build verbosity
org.gradle.console=rich                           # Colored output
```

### build.gradle.kts (Module Template)

**For Apps** (`packages/apps/*/build.gradle.kts`):
```kotlin
plugins {
    id("com.android.application")  // App, not library
    kotlin("android")
}

android {
    namespace = "com.lucidos.XXX"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lucidos.XXX"  // UNIQUE ID
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Use buildSrc for versions!
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.Compose.ui)
}
```

**For Libraries** (`packages/services/*/build.gradle.kts`):
```kotlin
plugins {
    id("com.android.library")      // Library, not app
    kotlin("android")
}

android {
    namespace = "com.lucidos.XXX"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        // NO applicationId (libraries don't have IDs)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(Dependencies.AndroidX.core)
}
```

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- NO PACKAGE ATTRIBUTE! (Defined in build.gradle.kts namespace) -->

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name">

        <!-- MUST have android:exported for exported components -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

---

## Module Structure

```
packages/
├── apps/ (Standard Android Applications)
│   ├── SystemUI/              compileSdk=34, minSdk=33, appId=com.lucidos.systemui
│   ├── Launcher/              compileSdk=34, minSdk=33, appId=com.lucidos.launcher
│   ├── LockScreen/            compileSdk=34, minSdk=33, appId=com.lucidos.lockscreen
│   ├── Settings/              compileSdk=34, minSdk=33, appId=com.lucidos.settings
│   ├── Calculator/            compileSdk=34, minSdk=33, appId=com.lucidos.calculator
│   └── PlayStore/             compileSdk=34, minSdk=33, appId=com.lucidos.playstore
│
└── services/ (Android Libraries - NO applicationId)
    ├── ConnectivityService/   compileSdk=34, minSdk=24, namespace=com.lucidos.connectivity
    ├── NetworkService/        compileSdk=34, minSdk=24, namespace=com.lucidos.network
    ├── PerformanceService/    compileSdk=34, minSdk=33, namespace=com.lucidos.performance
    └── SensorService/         compileSdk=34, minSdk=24, namespace=com.lucidos.sensor
```

---

## Dependency Management (buildSrc)

### Adding New Library

**1. Update buildSrc/Dependencies.kt**:
```kotlin
object Versions {
    const val retrofit = "2.9.0"    // Add version constant
}

object Dependencies {
    object Network {
        val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    }
}
```

**2. Use in module's build.gradle.kts**:
```kotlin
dependencies {
    implementation(Dependencies.Network.retrofit)
}
```

**3. Update everywhere**:
```bash
# All modules using Dependencies.Network.retrofit now get new version
gradlew.bat clean build
```

### Useful Gradle Commands for Dependencies

```bash
# Find version conflicts
gradlew.bat dependencyInsight --dependency androidx.core

# Check for updates
gradlew.bat dependencyUpdates

# Generate lock file (exact reproducible builds)
gradlew.bat dependencies --write-locks

# View dependency tree
gradlew.bat dependencies > deps.txt
```

---

## Common Errors & Quick Fixes

| Error | Symptom | Fix |
|-------|---------|-----|
| **OOM** | Build stops, memory error | Increase heap: `-Xmx6g` in gradle.properties |
| **Manifest merge failed** | Missing android:exported | Add `android:exported="true"` to activity/service |
| **Unresolved reference** | Kotlin compile error | Add import & dependency in build.gradle.kts |
| **Circular dependency** | Build fails, dependency loop | Use search: `gradlew dependencyInsight` |
| **Daemon won't start** | No gradle daemon | Run: `gradlew --stop` then retry |
| **Build cache corrupted** | Gradle cache error | Run: `build.bat --clean-cache` |
| **Package attribute deprecated** | AGP 8.0 warning | Remove from manifest, add namespace in build.gradle.kts |

---

## Windows-Specific Tips

### Enable Long Paths (260+ characters)
```powershell
# Run as Administrator
New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" `
  -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force
```

### Kill Stuck Java Processes
```bash
taskkill /IM java.exe /F
gradlew --stop
```

### Set Temporary Environment Variables
```bash
set JAVA_OPTS=-Xmx6g -XX:MaxMetaspaceSize=1024m
gradlew.bat build
```

### Check Disk Space
```bash
# Need at least 5GB free
dir C:\
```

---

## Performance Targets

| Task | Target | Typical | Status |
|------|--------|---------|--------|
| Full clean build | <120s | 90-110s | ✅ |
| Incremental build | <30s | 15-25s | ✅ |
| DEX merge | <20s | 10-15s | ✅ |
| Memory usage | <6GB | 4-5GB | ✅ |

---

## Development Workflow

```bash
# 1. Start development session
build.bat --clean-cache          # Full build from scratch

# 2. Make code changes
# Edit: packages/apps/SystemUI/src/main/...

# 3. Quick rebuild
gradlew.bat :packages:apps:SystemUI:build

# 4. Install to device
gradlew.bat :packages:apps:SystemUI:installDebug
adb shell am start -n com.lucidos.systemui/.MainActivity

# 5. View logs
adb logcat -s SystemUI

# 6. Repeat steps 2-5 as needed

# 7. Full release build
build.bat --release
```

---

## Directory Structure Diagram

```
LucidOS/
│
├── 📁 buildSrc/                    ← Shared build config
│   └── src/main/kotlin/
│       ├── 📄 Dependencies.kt       ← All versions here
│       └── 📄 AndroidConfigExtensions.kt
│
├── 📁 packages/
│   ├── 📁 apps/                    ← 6 Application modules
│   │   ├── SystemUI/build.gradle.kts → namespace=com.lucidos.systemui
│   │   ├── Launcher/build.gradle.kts → namespace=com.lucidos.launcher
│   │   ├── LockScreen/build.gradle.kts → namespace=com.lucidos.lockscreen
│   │   ├── Settings/build.gradle.kts → namespace=com.lucidos.settings
│   │   ├── Calculator/build.gradle.kts → namespace=com.lucidos.calculator
│   │   └── PlayStore/build.gradle.kts → namespace=com.lucidos.playstore
│   │
│   └── 📁 services/                ← 4 Library modules
│       ├── ConnectivityService/build.gradle.kts → namespace=com.lucidos.connectivity
│       ├── NetworkService/build.gradle.kts → namespace=com.lucidos.network
│       ├── PerformanceService/build.gradle.kts → namespace=com.lucidos.performance
│       └── SensorService/build.gradle.kts → namespace=com.lucidos.sensor
│
├── 📄 gradle.properties            ← JVM & Gradle config (OPTIMIZED)
├── 📄 build.gradle.kts             ← Root build file
├── 📄 settings.gradle.kts          ← Module declarations
│
├── 📄 build.bat                    ← Windows build script (NEW)
├── 📄 BUILD_GUIDE.md              ← Comprehensive guide (NEW)
├── 📄 BUILD_FIX_SUMMARY.md        ← This fix overview (NEW)
├── 📄 DEPENDENCY_STRATEGY.md      ← API mapping (NEW)
└── 📄 TROUBLESHOOTING.md          ← Error solutions (NEW)
```

---

## Key Configuration Values

### Android SDK
```
compileSdk:    34 (Android 14)
minSdk:        24 or 33 (depends on module)
targetSdk:     34 (Android 14)
buildTools:    34.0.0
```

### Kotlin
```
version:       1.9.22
jvmTarget:     17
compilerExt:   1.5.8 (for Compose)
```

### JVM
```
Heap:          5GB
Metaspace:     768MB
GC:            G1GC
Workers:       7 (N-1 cores)
```

---

## Next Steps

1. ✅ Review [BUILD_FIX_SUMMARY.md](BUILD_FIX_SUMMARY.md)
2. ✅ Run: `build.bat`
3. ✅ Check APKs in: `packages/apps/*/build/outputs/apk/`
4. ✅ For errors, see: [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
5. ✅ For details, read: [BUILD_GUIDE.md](BUILD_GUIDE.md)

---

**Quick Links**:
- [Complete Build Guide](BUILD_GUIDE.md)
- [Dependency Strategy](DEPENDENCY_STRATEGY.md)
- [Error Troubleshooting](TROUBLESHOOTING.md)
- [Fix Summary](BUILD_FIX_SUMMARY.md)

**Last Updated**: May 28, 2026
