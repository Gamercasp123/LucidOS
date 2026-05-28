plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Android Gradle Plugin
    implementation("com.android.tools.build:gradle:8.2.2")

    // Kotlin Gradle Plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
}
