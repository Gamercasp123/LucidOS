package com.lucidos.lockscreen.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import android.util.Log

/**
 * Biometric authentication manager - Fingerprint & Face recognition
 */
class BiometricAuthManager(private val context: Context) {
    private const val TAG = "BiometricAuthManager"
    private val biometricManager = BiometricManager.from(context)

    enum class BiometricType {
        FINGERPRINT,
        FACE,
        IRIS,
        NONE
    }

    data class BiometricStatus(
        val isAvailable: Boolean,
        val isEnrolled: Boolean,
        val type: BiometricType,
        val errorMessage: String = ""
    )

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): BiometricStatus {
        return try {
            val authenticators = Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL

            val status = biometricManager.canAuthenticate(authenticators)

            return when (status) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val type = detectBiometricType()
                    BiometricStatus(true, true, type)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    Log.w(TAG, "No biometric hardware available")
                    BiometricStatus(false, false, BiometricType.NONE, "No biometric hardware")
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    Log.w(TAG, "Biometric hardware unavailable")
                    BiometricStatus(false, false, BiometricType.NONE, "Hardware unavailable")
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Log.w(TAG, "No biometric data enrolled")
                    BiometricStatus(true, false, BiometricType.NONE, "No biometric enrolled")
                }
                else -> BiometricStatus(false, false, BiometricType.NONE, "Unknown error")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking biometric availability", e)
            BiometricStatus(false, false, BiometricType.NONE, e.message ?: "Error checking biometric")
        }
    }

    /**
     * Detect which biometric types are available
     */
    private fun detectBiometricType(): BiometricType {
        return try {
            val canAuthenticateFingerprint = biometricManager.canAuthenticate(
                Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS

            val canAuthenticateFace = biometricManager.canAuthenticate(
                Authenticators.BIOMETRIC_WEAK
            ) == BiometricManager.BIOMETRIC_SUCCESS

            when {
                canAuthenticateFingerprint -> BiometricType.FINGERPRINT
                canAuthenticateFace -> BiometricType.FACE
                else -> BiometricType.NONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting biometric type", e)
            BiometricType.NONE
        }
    }

    /**
     * Show biometric authentication prompt
     */
    fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        description: String = "Place your finger on the sensor",
        callback: BiometricCallback
    ) {
        try {
            Log.d(TAG, "Starting biometric authentication")

            val biometricPrompt = BiometricPrompt(
                activity,
                MainExecutor.INSTANCE,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(TAG, "Biometric authentication succeeded")
                        callback.onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e(TAG, "Biometric authentication error: $errString")
                        callback.onError(errString.toString())
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.w(TAG, "Biometric authentication failed - try again")
                        callback.onFailed()
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setDescription(description)
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting biometric authentication", e)
            callback.onError(e.message ?: "Unknown error")
        }
    }

    interface BiometricCallback {
        fun onSuccess()
        fun onFailed()
        fun onError(message: String)
    }

    fun logBiometricStatus() {
        val status = isBiometricAvailable()
        Log.d(TAG, """
            Biometric Status:
            Available: ${status.isAvailable}
            Enrolled: ${status.isEnrolled}
            Type: ${status.type}
            Error: ${status.errorMessage}
        """.trimIndent())
    }
}
