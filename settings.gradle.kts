rootProject.name = "LucidOS"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Include modules
include(":packages:apps:SystemUI")
include(":packages:apps:Launcher")
include(":packages:apps:LockScreen")
include(":packages:apps:Settings")
include(":packages:apps:Calculator")
include(":packages:apps:PlayStore")
include(":packages:services:PerformanceService")
include(":packages:services:ConnectivityService")
include(":packages:services:NetworkService")
include(":packages:services:SensorService")
// include(":packages:services:SystemUI")
// include(":frameworks:base")
