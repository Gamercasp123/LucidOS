plugins {
    kotlin("jvm") version "1.9.0" apply false
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

subprojects {
    apply(plugin = "kotlin")

    kotlin {
        jvmToolchain(11)
    }
}
