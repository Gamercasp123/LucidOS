package com.lucidos.lockscreen.auth

import android.util.Log
import com.lucidos.lockscreen.security.CredentialManager

/**
 * PIN authentication handler (4 or 6 digits)
 */
class PinAuthHandler(private val credentialManager: CredentialManager) {
    private const val TAG = "PinAuthHandler"
    private var attempts = 0
    private val maxAttempts = 5

    fun validatePin(pin: String): AuthResult {
        attempts++

        if (attempts > maxAttempts) {
            Log.w(TAG, "Max attempts exceeded")
            return AuthResult.LockedOut
        }

        if (pin.isEmpty()) {
            return AuthResult.Invalid("PIN cannot be empty")
        }

        if (!pin.all { it.isDigit() }) {
            return AuthResult.Invalid("PIN must contain only digits")
        }

        val authType = credentialManager.getAuthType()
        val expectedLength = when (authType) {
            CredentialManager.AuthType.PIN_4 -> 4
            CredentialManager.AuthType.PIN_6 -> 6
            else -> return AuthResult.Invalid("PIN auth not configured")
        }

        if (pin.length != expectedLength) {
            return AuthResult.Invalid("PIN must be $expectedLength digits")
        }

        return if (credentialManager.verifyAuthentication(pin)) {
            Log.d(TAG, "PIN verified successfully")
            AuthResult.Success
        } else {
            Log.w(TAG, "PIN verification failed - attempt $attempts/$maxAttempts")
            AuthResult.Invalid("Incorrect PIN - ${maxAttempts - attempts} attempts remaining")
        }
    }

    fun getAttemptsRemaining(): Int = maxAttempts - attempts

    fun resetAttempts() {
        attempts = 0
    }

    sealed class AuthResult {
        object Success : AuthResult()
        object LockedOut : AuthResult()
        data class Invalid(val message: String) : AuthResult()
    }
}
