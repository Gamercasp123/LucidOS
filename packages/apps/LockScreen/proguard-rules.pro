-keep class com.lucidos.lockscreen.** { *; }
-keepclassmembers class com.lucidos.lockscreen.** { *; }
-keep interface com.lucidos.lockscreen.** { *; }

# Security - Keep credential handling
-keep class com.lucidos.lockscreen.security.** { *; }
-keep class com.lucidos.lockscreen.auth.** { *; }

# Android Framework
-keep class android.** { *; }

# Kotlin
-keep class kotlin.** { *; }

# AndroidX
-keep class androidx.** { *; }
