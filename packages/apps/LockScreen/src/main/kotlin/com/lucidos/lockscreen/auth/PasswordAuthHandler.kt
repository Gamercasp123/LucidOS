package com.lucidos.lockscreen.auth

import android.util.Log
import com.lucidos.lockscreen.security.CredentialManager

/**
 * Password authentication handler (16+ characters)
 */
class PasswordAuthHandler(private val credentialManager: CredentialManager) {
    private const val TAG = "PasswordAuthHandler"
    private var attempts = 0
    private val maxAttempts = 5

    fun validatePassword(password: String): AuthResult {
        attempts++

        if (attempts > maxAttempts) {
            Log.w(TAG, "Max attempts exceeded")
            return AuthResult.LockedOut
        }

        if (password.isEmpty()) {
            return AuthResult.Invalid("Password cannot be empty")
        }

        if (password.length < 16) {
            return AuthResult.Invalid("Password must be at least 16 characters")
        }

        return if (credentialManager.verifyAuthentication(password)) {
            Log.d(TAG, "Password verified successfully")
            AuthResult.Success
        } else {
            Log.w(TAG, "Password verification failed - attempt $attempts/$maxAttempts")
            AuthResult.Invalid("Incorrect password - ${maxAttempts - attempts} attempts remaining")
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
