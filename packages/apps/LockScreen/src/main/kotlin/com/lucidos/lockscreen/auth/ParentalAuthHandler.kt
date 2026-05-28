package com.lucidos.lockscreen.auth

import android.util.Log
import com.lucidos.lockscreen.security.CredentialManager

/**
 * Parental PIN authentication handler
 */
class ParentalAuthHandler(private val credentialManager: CredentialManager) {
    private const val TAG = "ParentalAuthHandler"
    private var attempts = 0
    private val maxAttempts = 3

    fun validateParentalPin(pin: String): AuthResult {
        attempts++

        if (attempts > maxAttempts) {
            Log.w(TAG, "Parental PIN - Max attempts exceeded")
            return AuthResult.LockedOut
        }

        if (pin.isEmpty()) {
            return AuthResult.Invalid("PIN cannot be empty")
        }

        if (!pin.all { it.isDigit() }) {
            return AuthResult.Invalid("PIN must contain only digits")
        }

        if (pin.length != 4) {
            return AuthResult.Invalid("Parental PIN must be 4 digits")
        }

        return if (credentialManager.verifyParentalPin(pin)) {
            Log.d(TAG, "Parental PIN verified successfully")
            AuthResult.Success
        } else {
            Log.w(TAG, "Parental PIN verification failed - attempt $attempts/$maxAttempts")
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
