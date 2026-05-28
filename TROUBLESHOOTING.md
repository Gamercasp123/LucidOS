# LucidOS Build Troubleshooting Guide

**Version**: 1.0
**Last Updated**: May 28, 2026
**Platform**: Windows 10/11 + Gradle 8.0+

## Quick Start Troubleshooting

```bash
# If build fails, try these steps in order:

# 1. Clean everything and rebuild
build.bat --clean-cache

# 2. Check for common errors
gradlew.bat build --info 2>&1 | findstr /i "error failed"

# 3. Profile the build to find bottlenecks
build.bat --profile

# 4. Build just one module to isolate issues
gradlew.bat :packages:apps:SystemUI:build --info
```

---

## Error Categories & Solutions

### Category 1: OutOfMemory (OOM) Errors

#### Error: `OutOfMemoryError: Java heap space`

**Symptom**:
```
OutOfMemoryError: Java heap space
    at com.android.dex.Dex.<init>(Dex.java:...)
    at com.android.dx.command.dxc.Main.main(Main.java:...)
```

**Causes**:
- Multi-DEX merging during application build
- Too many dependencies
- Insufficient JVM heap

**Solutions** (in order):

1. **Increase heap size**:
   ```properties
   # gradle.properties
   org.gradle.jvmargs=-Xmx6g  # Increase from 5g to 6g
   ```

2. **Enable multi-dex explicitly**:
   ```kotlin
   // build.gradle.kts (app module)
   android {
       defaultConfig {
           multiDexEnabled = true
       }
   }
   ```

3. **Enable minification to reduce DEX size**:
   ```kotlin
   buildTypes {
       debug {
           isMinifyEnabled = true
           proguardFiles(getDefaultProguardFile("proguard-android.txt"))
       }
   }
   ```

4. **Reduce dependencies**:
   ```bash
   # Analyze dependency tree
   gradlew.bat :packages:apps:SystemUI:dependencies | findstr "architecture"
   # Remove unnecessary transitive dependencies
   ```

#### Error: `OutOfMemoryError: Metaspace`

**Symptom**:
```
OutOfMemoryError: Metaspace
    at jdk.internal.misc.Unsafe.allocateMemory(Native Method)
```

**Causes**:
- Kotlin compiler needs more metadata space
- Android Gradle Plugin loading large libraries
- Multiple AnnotationProcessor passes

**Solution**:
```properties
# gradle.properties
org.gradle.jvmargs=-Xmx5g \
  -XX:MaxMetaspaceSize=1024m  # Increase from 768m
```

#### Error: `OutOfMemoryError: GC overhead limit exceeded`

**Symptom**:
```
OutOfMemoryError: GC overhead limit exceeded
```

**Causes**:
- Garbage collector running constantly with insufficient memory
- Memory leaks in build plugins
- Circular dependency chains

**Solutions**:

1. **Enable parallel GC**:
   ```properties
   org.gradle.jvmargs=-Xmx5g \
     -XX:+UseG1GC \
     -XX:+ParallelRefProcEnabled
   ```

2. **Check for circular dependencies**:
   ```bash
   gradlew.bat :packages:apps:SystemUI:dependencies
   # Look for cycles: A → B → C → A
   ```

---

### Category 2: Compilation Errors

#### Error: `Compilation failed for package com.lucidos.systemui`

**Symptom**:
```
e: error: unresolved reference: ...
e: ...is not defined
```

**Causes**:
- Missing import statements
- Undefined classes or functions
- Module not compiled first (dependency order)

**Solutions**:

1. **Check imports in the error file**:
   ```bash
   # Find the file with the error
   gradlew.bat :packages:apps:SystemUI:build --info | findstr "error:"
   ```

2. **Verify module dependencies**:
   ```kotlin
   // build.gradle.kts
   dependencies {
       implementation(project(":packages:services:ConnectivityService"))
       // Make sure service module is listed
   }
   ```

3. **Rebuild in dependency order**:
   ```bash
   # Build services first
   gradlew.bat :packages:services:PerformanceService:build
   gradlew.bat :packages:services:ConnectivityService:build
   # Then apps
   gradlew.bat :packages:apps:SystemUI:build
   ```

#### Error: `Type mismatch: inferred type is ... but Context was expected`

**Symptom**:
```
e: Type mismatch: inferred type is `Unit` but `Context` was expected
```

**Cause**: Kotlin type inference issue in lambda or function call

**Solution**:
```kotlin
// ❌ Wrong
val context: Context = someFunction { /* ... */ }

// ✅ Correct
val context: Context = someFunction({ /* ... */ })
// Or specify type explicitly
val result = someFunction { x: String -> x.length }
```

---

### Category 3: Manifest Errors

#### Error: `Manifest merger failed with multiple errors`

**Symptom**:
```
Manifest merger failed with multiple errors, see logs
ERROR: Activity ... exported without android:permission
```

**Causes**:
- Missing `android:exported` attribute
- Permission conflicts between manifests
- Namespace mismatch

**Solutions**:

1. **Add android:exported to exported components**:
   ```xml
   <activity
       android:name=".ui.LockScreenActivity"
       android:exported="true"  <!-- ADD THIS -->
       android:launchMode="singleTop">
   </activity>
   ```

2. **Verify namespace matches build.gradle.kts**:
   ```bash
   # Get namespace from build.gradle.kts
   findstr "namespace" packages/apps/SystemUI/build.gradle.kts
   # Should match internal package structure
   ```

3. **Check AndroidManifest.xml for package attribute** (deprecated):
   ```xml
   <!-- ❌ OLD (AGP 8.0+ warns) -->
   <manifest package="com.lucidos.systemui">

   <!-- ✅ NEW (AGP 7.0+) -->
   <manifest>
   <!-- Package now defined in build.gradle.kts: namespace = "..." -->
   ```

#### Error: `Class ... is referenced but not provided by AndroidManifest.xml`

**Symptom**:
```
Class com.lucidos.systemui.ui.SystemUIActivity is referenced but not provided by AndroidManifest.xml
```

**Causes**:
- Service/Activity not declared in manifest
- Typo in class name or package name
- Class doesn't exist

**Solution**:
```xml
<application>
    <activity
        android:name=".ui.SystemUIActivity"  <!-- Must match kotlin package structure -->
        android:exported="true" />
</application>
```

---

### Category 4: Gradle Build System Errors

#### Error: `Could not determine artifacts for ... : File does not exist`

**Symptom**:
```
Could not determine artifacts for ...
Could not find any matches for ...
```

**Causes**:
- Maven repository not available (network issue)
- Dependency version doesn't exist
- Typo in dependency coordinates

**Solutions**:

1. **Check internet connection and repositories**:
   ```bash
   # Test access to Maven Central
   ping maven-central.storage.googleapis.com
   ```

2. **Verify dependency version exists**:
   ```bash
   # Check buildSrc/Dependencies.kt for correct versions
   findstr "androidxCore" buildSrc/src/main/kotlin/Dependencies.kt
   ```

3. **Clear Gradle cache**:
   ```bash
   gradlew.bat --stop  # Stop daemon
   build.bat --clean-cache
   ```

#### Error: `Build cache is corrupted`

**Symptom**:
```
Build cache directory ... is invalid
No cached version of ... available for offline mode
```

**Solution**:
```bash
# Clear cache completely
rmdir /s /q %USERPROFILE%\.gradle\caches
rmdir /s /q .gradle
gradlew.bat build
```

#### Error: `Circular dependency between projects`

**Symptom**:
```
Circular dependency: project A -> project B -> project A
```

**Causes**:
- App module depends on another app
- Library has circular reference
- Bidirectional project dependencies

**Solution**:

Check dependency graph:
```bash
gradlew.bat dependencies --write-locks
# View build/reports/lock-file/lock-file-report.txt
```

Fix by removing circular reference:
```kotlin
// ❌ WRONG: apps/SystemUI/build.gradle.kts
dependencies {
    implementation(project(":packages:apps:Launcher"))  // App depending on app!
}

// ✅ CORRECT: Extract to service library
// Create: packages/services/UICommon/build.gradle.kts
// Then both apps can depend on it
dependencies {
    implementation(project(":packages:services:UICommon"))
}
```

---

### Category 5: Kotlin Compiler Errors

#### Error: `Unresolved reference: ...`

**Symptom**:
```
e: ...Kotlin: Unresolved reference: 'BiometricPrompt'
```

**Causes**:
- Missing import statement
- Dependency not included in build.gradle.kts
- Typo in class name

**Solution**:

1. **Add missing dependency**:
   ```kotlin
   // build.gradle.kts
   dependencies {
       implementation("androidx.biometric:biometric:1.1.0")
   }
   ```

2. **Add import statement**:
   ```kotlin
   import androidx.biometric.BiometricPrompt
   ```

3. **Rebuild**:
   ```bash
   gradlew.bat clean :packages:apps:LockScreen:build
   ```

#### Error: `Symbol is inaccessible due to internal restriction`

**Symptom**:
```
e: Symbol 'someInternalClass' is inaccessible due to 'internal' modifier
```

**Cause**: Trying to use an internal API (only usable within the module)

**Solution**:
```kotlin
// ❌ Wrong: Internal APIs are hidden
import android.frameworks.internal.something

// ✅ Right: Use public APIs
import android.app.SystemServiceManager
```

---

### Category 6: Build Daemon Issues (Windows-Specific)

#### Error: `Could not create Gradle daemon process`

**Symptom**:
```
Could not create Gradle daemon process. Exited with 1
```

**Causes**:
- Gradle daemon already running
- File locks preventing new daemon
- Insufficient system resources

**Solutions**:

1. **Stop existing daemon**:
   ```bash
   gradlew.bat --stop
   taskkill /IM java.exe /F  # Force kill Java processes
   ```

2. **Disable daemon temporarily**:
   ```bash
   gradlew.bat build -Dorg.gradle.daemon=false
   ```

3. **Clear daemon state**:
   ```bash
   rmdir /s /q %USERPROFILE%\.gradle\daemon
   gradlew.bat build
   ```

---

### Category 7: DEX & Bytecode Errors

#### Error: `Cannot fit requested classes in a single dex file`

**Symptom**:
```
cannot fit requested classes in a single dex file (# members: 70000 > 65536)
```

**Causes**:
- Too many classes/methods (>65K method limit per DEX)
- Unnecessary dependencies
- Not using ProGuard/R8 minification

**Solutions**:

1. **Enable ProGuard/R8 minification**:
   ```kotlin
   buildTypes {
       debug {
           isMinifyEnabled = true
           proguardFiles(
               getDefaultProguardFile("proguard-android.txt"),
               "proguard-rules.pro"
           )
       }
   }
   ```

2. **Use multi-DEX**:
   ```kotlin
   android {
       defaultConfig {
           multiDexEnabled = true
       }
   }

   dependencies {
       implementation("androidx.multidex:multidex:2.0.1")
   }
   ```

3. **Remove unused dependencies**:
   ```bash
   gradlew.bat dependencyInsight --dependency androidx
   # Remove any unused androidx libraries
   ```

---

### Category 8: Permission & Security Errors

#### Error: `Permission 'android.permission.DISABLE_KEYGUARD' denied`

**Symptom**:
```
SecurityException: Permission 'android.permission.DISABLE_KEYGUARD' denied
```

**Causes**:
- Permission not declared in AndroidManifest.xml
- Runtime permission not granted (Android 6.0+)
- SignatureLevel permission

**Solution**:

1. **Declare in manifest**:
   ```xml
   <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
   ```

2. **For runtime permissions (dangerous/signature)**:
   ```kotlin
   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
       if (ContextCompat.checkSelfPermission(
           this,
           Manifest.permission.USE_BIOMETRIC
       ) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(
               this,
               arrayOf(Manifest.permission.USE_BIOMETRIC),
               REQUEST_CODE
           )
       }
   }
   ```

---

## Debug Logging

### Enable Verbose Logging

```bash
# Show all build steps
gradlew.bat build --info

# Show even more detail
gradlew.bat build --debug

# Save to file for analysis
gradlew.bat build --info > build.log 2>&1
findstr /i "error warning" build.log
```

### Check Specific Module

```bash
# Verbose output for one app
gradlew.bat :packages:apps:LockScreen:assembleDebug --info

# Check only compilation errors
gradlew.bat :packages:apps:LockScreen:compileDebugKotlin --info 2>&1 | findstr /i "error"
```

### Profile Build Performance

```bash
# Generate performance profile
gradlew.bat build --profile

# HTML report at: build/reports/profile/profile-TIMESTAMP.html
```

---

## Performance Profiling

### Check Build Time by Phase

```bash
# 1. Identify slow tasks
gradlew.bat build --profile

# 2. Check task execution time
gradlew.bat build --build-cache --info | findstr /i "compiling\|merging\|dex"

# 3. Compare incremental vs clean
gradlew.bat build  # Incremental (should be <30s)
gradlew.bat clean build  # Clean (should be <120s)
```

### Memory Usage During Build

```bash
# Monitor via Windows Task Manager while building
# OR use:
gradlew.bat build -Xmx7g  # Temporarily increase heap for profiling
```

---

## Validation Checklist

Before declaring build fixed:

- [ ] `gradlew.bat clean build` succeeds without errors
- [ ] No OOM warnings in build output
- [ ] No manifest merge errors
- [ ] APK files generated in `packages/apps/*/build/outputs/apk/`
- [ ] All 6 app modules compile successfully
- [ ] All 4 service modules compile successfully
- [ ] No circular dependencies detected
- [ ] Build time < 120 seconds (clean) or < 30 seconds (incremental)

---

## Getting Help

1. **Check logs**: `gradlew.bat build --info > build-error.log`
2. **Search error code**: Search Google for exact error message
3. **Check dependencies**: `gradlew.bat :packages:apps:SystemUI:dependencies`
4. **Profile build**: `gradlew.bat build --profile`
5. **Rebuild from clean**: `build.bat --clean-cache`

---

**Last Updated**: May 28, 2026
**For build configuration details, see**: [BUILD_GUIDE.md](BUILD_GUIDE.md)
