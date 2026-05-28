# LucidOS Dependency Management Strategy

**Version**: 1.0
**Last Updated**: May 28, 2026

## Quick Reference: Public API Replacements

When you encounter missing internal Android APIs, use these public alternatives:

### System UI / Status Bar

| Internal | Public Alternative | Example |
|----------|-------------------|---------|
| `com.android.internal.statusbar.*` | `android.app.StatusBarManager` | Via `Context.getSystemService("statusbar")` with reflection |
| Window manager internals | `android.app.WindowManager` | `context.getSystemService(Context.WINDOW_SERVICE)` |
| Display internals | `android.hardware.display.DisplayManager` | `context.getSystemService(Context.DISPLAY_SERVICE)` |

### Connectivity

| Internal | Public Alternative |
|----------|-------------------|
| `com.android.internal.util.State*` | `android.net.ConnectivityManager` |
| Internal WiFi | `android.net.wifi.WifiManager` |
| Internal Bluetooth | `android.bluetooth.BluetoothAdapter` |

### Performance / Power Management

| Internal | Public Alternative |
|----------|-------------------|
| `com.android.internal.power.*` | `android.os.PowerManager` |
| `com.android.internal.app.ProcessState` | `android.app.ActivityManager` |
| Thermal data | `android.os.BatteryManager` + sensors |

### Sensors

| Internal | Public Alternative |
|----------|-------------------|
| Framework sensor HAL | `android.hardware.SensorManager` |
| Sensor internals | `android.hardware.Sensor` + `SensorEventListener` |

## Gradle Configuration by Module Type

### Standard Applications (apps/)

These build as Android Apps using public SDK:

```kotlin
// ✅ CORRECT: packages/apps/SystemUI/build.gradle.kts
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = AndroidConfig.compileSdk
    namespace = "com.lucidos.systemui"

    defaultConfig {
        applicationId = "com.lucidos.systemui"  // Unique ID
        minSdk = AndroidConfig.minSdk
        targetSdk = AndroidConfig.targetSdk
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName
    }
}

dependencies {
    // Public Android APIs only
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.Compose.ui)

    // ❌ NEVER:
    // implementation("android.frameworks.base:framework:1.0")
}
```

### System Services (services/)

These build as Android Libraries (no Application ID):

```kotlin
// ✅ CORRECT: packages/services/ConnectivityService/build.gradle.kts
plugins {
    id("com.android.library")  // Library, not app
    kotlin("android")
}

android {
    compileSdk = AndroidConfig.compileSdk
    namespace = "com.lucidos.connectivity"

    // No defaultConfig.applicationId (libraries don't have IDs)
    defaultConfig {
        minSdk = AndroidConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Public Android APIs
    implementation(Dependencies.AndroidX.core)
}
```

## Dependency Declaration Examples

### Module Dependency (Local Projects)

```kotlin
// app depends on local library
dependencies {
    implementation(project(":packages:services:ConnectivityService"))
    implementation(project(":packages:services:PerformanceService"))
}
```

### External Dependency (Maven Central)

```kotlin
// From buildSrc/Dependencies.kt versions
dependencies {
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.Compose.ui)
    implementation(Dependencies.biometric)
}
```

### Specific Version

```kotlin
// Direct specification (not recommended - use buildSrc instead)
dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
}
```

## Preventing OOM During Dependency Resolution

```properties
# gradle.properties
# Tell Gradle to limit dependency downloads
org.gradle.http.connectionPool.size=50
org.gradle.http.socketTimeout=30000
org.gradle.http.connectionTimeout=10000

# Disable offline mode once (forces clean resolution)
org.gradle.offline=false
```

## Checking Dependencies

### List All Dependencies

```bash
# All modules
gradlew.bat dependencies

# Specific module
gradlew.bat :packages:apps:SystemUI:dependencies

# Only implementation dependencies
gradlew.bat :packages:apps:SystemUI:dependencies --configuration implementation
```

### Find Duplicate Libraries

```bash
# Analyze dependency tree
gradlew.bat dependencyInsight --dependency androidx.core --configuration implementation
```

### Update Dependencies

```bash
# Check for updates
gradlew.bat dependencyUpdates

# Or manually in buildSrc/Dependencies.kt
object Versions {
    const val androidxCore = "1.10.1"  // Update this
    // ...
}
```

## Lock Files

Generate dependency lock file for reproducible builds:

```bash
# Generate lock file
gradlew.bat dependencies --write-locks

# Use lock file (locked mode)
gradlew.bat build --write-locks
```

Creates `gradle.lockfile`:
```
androidx.core:core-ktx:1.10.1=group1,group2
androidx.appcompat:appcompat:1.6.1=group1
# ... all locked versions
```

## Troubleshooting Dependency Issues

### Issue: Conflicting Versions

```
Error: Duplicate class androidx.core.app.ServiceCompat
```

**Solution**:
```bash
# Find the conflict
gradlew.bat dependencyInsight --dependency androidx.core

# Force unified version in build.gradle.kts
dependencies {
    constraints {
        implementation("androidx.core:core-ktx:1.10.1")
    }
}
```

### Issue: Missing Transitive Dependency

```
Error: Could not find com.example:library:1.0
```

**Solution**: Add directly to dependencies
```kotlin
dependencies {
    implementation("com.example:library:1.0")
}
```

### Issue: Version Range Not Found

```
Error: Could not find any matches for androidx.core:core-ktx:1.+
```

**Solution**: Use exact version in buildSrc
```kotlin
object Versions {
    const val androidxCore = "1.10.1"  // Exact, not range
}
```

## Module Dependency Graph

```
apps/SystemUI ─────┬─→ services/ConnectivityService
                   ├─→ services/PerformanceService
                   └─→ services/NetworkService

apps/Launcher ─────→ (no service dependencies)

apps/LockScreen ──→ (only public APIs)

services/* ────────→ (only androidx/android frameworks)
```

## Best Practices

1. **Centralize versions**: Use `buildSrc/Dependencies.kt`
2. **Minimize cross-module deps**: Services are libraries, not apps
3. **Use public APIs**: Never import internal framework classes
4. **Lock dependencies**: Keep `gradle.lockfile` for reproducibility
5. **Run `dependencyInsight`**: Before merging dependency updates
6. **Test locally first**: Build incrementally to catch issues early

---

**For detailed build configuration, see**: [BUILD_GUIDE.md](BUILD_GUIDE.md)
