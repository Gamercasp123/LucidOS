package com.lucidos.lockscreen.auth

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.lucidos.lockscreen.security.BiometricAuthManager

/**
 * Biometric authentication handler - Fingerprint and Face recognition
 */
class BiometricAuthHandler(
    private val biometricManager: BiometricAuthManager
) {
    private const val TAG = "BiometricAuthHandler"
    private var lastAttemptTime = 0L
    private val debounceMillis = 500L

    fun startBiometricAuthentication(
        activity: FragmentActivity,
        callback: BiometricAuthCallback
    ) {
        // Debounce rapid calls
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < debounceMillis) {
            Log.w(TAG, "Biometric request too frequent, debouncing")
            return
        }
        lastAttemptTime = currentTime

        val status = biometricManager.isBiometricAvailable()

        if (!status.isAvailable) {
            Log.e(TAG, "Biometric not available: ${status.errorMessage}")
            callback.onError("Biometric authentication not available")
            return
        }

        if (!status.isEnrolled) {
            Log.e(TAG, "No biometric enrolled")
            callback.onError("No biometric data enrolled on this device")
            return
        }

        Log.d(TAG, "Starting biometric authentication (${status.type})")

        val authDescription = when (status.type) {
            BiometricAuthManager.BiometricType.FINGERPRINT -> "Place your finger on the sensor"
            BiometricAuthManager.BiometricType.FACE -> "Look at the camera"
            BiometricAuthManager.BiometricType.IRIS -> "Look at the iris scanner"
            BiometricAuthManager.BiometricType.NONE -> "Authenticate with biometric"
        }

        biometricManager.authenticateWithBiometric(
            activity,
            title = "Unlock Device",
            description = authDescription,
            callback = object : BiometricAuthManager.BiometricCallback {
                override fun onSuccess() {
                    Log.d(TAG, "Biometric authentication successful")
                    callback.onSuccess()
                }

                override fun onFailed() {
                    Log.w(TAG, "Biometric authentication failed - try again")
                    callback.onFailed("Biometric not recognized, try again")
                }

                override fun onError(message: String) {
                    Log.e(TAG, "Biometric error: $message")
                    callback.onError(message)
                }
            }
        )
    }

    interface BiometricAuthCallback {
        fun onSuccess()
        fun onFailed(message: String)
        fun onError(message: String)
    }
}
