/**
 * LucidOS Build Configuration
 * Centralized version and dependency management
 */

object AndroidConfig {
    const val compileSdk = 34
    const val minSdk = 23
    const val targetSdk = 34
    const val buildToolsVersion = "34.0.0"
}

object AppConfig {
    const val applicationId = "com.lucidos"
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object KotlinConfig {
    const val version = "1.9.22"
    const val jvmTarget = "17"
}

object Versions {
    // Android Core
    const val androidxCore = "1.10.1"
    const val androidxAppCompat = "1.6.1"
    const val androidxActivity = "1.7.2"
    const val androidxFragment = "1.6.1"

    // Material Design
    const val material = "1.9.0"

    // Jetpack Compose
    const val compose = "1.5.0"
    const val composeMaterial3 = "1.1.1"
    const val composeFoundation = "1.5.0"
    const val composeCompilerExtension = "1.5.8"

    // Lifecycle
    const val lifecycle = "2.6.1"

    // Security & Biometric
    const val securityCrypto = "1.1.0-alpha06"
    const val biometric = "1.1.0"

    // Network
    const val retrofit = "2.9.0"
    const val okhttp = "4.11.0"

    // Database
    const val room = "2.5.2"

    // Testing
    const val junit = "4.13.2"
    const val androidxTestExt = "1.1.5"
    const val espresso = "3.5.1"
}

object Dependencies {
    // Android Core
    object AndroidX {
        val core = "androidx.core:core-ktx:${Versions.androidxCore}"
        val appCompat = "androidx.appcompat:appcompat:${Versions.androidxAppCompat}"
        val activity = "androidx.activity:activity-ktx:${Versions.androidxActivity}"
        val fragment = "androidx.fragment:fragment-ktx:${Versions.androidxFragment}"
    }

    // Material Design
    val material = "com.google.android.material:material:${Versions.material}"

    // Jetpack Compose
    object Compose {
        val ui = "androidx.compose.ui:ui:${Versions.compose}"
        val material3 = "androidx.compose.material3:material3:${Versions.composeMaterial3}"
        val foundation = "androidx.compose.foundation:foundation:${Versions.composeFoundation}"
        val runtimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}"
    }

    // Lifecycle
    object Lifecycle {
        val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
        val viewmodel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    }

    // Security & Biometric
    val securityCrypto = "androidx.security:security-crypto:${Versions.securityCrypto}"
    val biometric = "androidx.biometric:biometric:${Versions.biometric}"

    // Network
    object Network {
        val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    }

    // Database
    object Room {
        val runtime = "androidx.room:room-runtime:${Versions.room}"
        val compiler = "androidx.room:room-compiler:${Versions.room}"
    }

    // Testing
    val junit = "junit:junit:${Versions.junit}"
    val androidxTestExt = "androidx.test.ext:junit:${Versions.androidxTestExt}"
    val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
}
