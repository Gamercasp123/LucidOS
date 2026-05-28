import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * Extension function to apply common Android app configuration
 */
fun Project.applyCommonAndroidAppConfig() {
    apply(plugin = "com.android.application")
    apply(plugin = "kotlin-android")

    extensions.configure<com.android.build.gradle.AppExtension>("android") {
        compileSdkVersion(AndroidConfig.compileSdk)
        buildToolsVersion(AndroidConfig.buildToolsVersion)

        defaultConfig {
            minSdk = AndroidConfig.minSdk
            targetSdk = AndroidConfig.targetSdk
            versionCode = AppConfig.versionCode
            versionName = AppConfig.versionName
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = KotlinConfig.jvmTarget
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}

/**
 * Extension function to apply common Android library configuration
 */
fun Project.applyCommonAndroidLibraryConfig() {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")

    extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
        compileSdkVersion(AndroidConfig.compileSdk)
        buildToolsVersion(AndroidConfig.buildToolsVersion)

        defaultConfig {
            minSdk = AndroidConfig.minSdk
            targetSdk = AndroidConfig.targetSdk
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = KotlinConfig.jvmTarget
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
    }
}
