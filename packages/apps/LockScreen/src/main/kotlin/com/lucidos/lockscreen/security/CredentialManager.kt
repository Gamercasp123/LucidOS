package com.lucidos.lockscreen.security

import android.content.Context
import android.util.Log
import java.security.MessageDigest

/**
 * Secure credential storage and validation
 */
class CredentialManager(private val context: Context) {
    private val TAG = "CredentialManager"
    private val prefs = context.getSharedPreferences("lock_creds", Context.MODE_PRIVATE)

    enum class AuthType {
        PIN_4,      // 4-digit PIN
        PIN_6,      // 6-digit PIN
        PASSWORD,   // 16+ character password
        PATTERN,    // Visual pattern
        NONE        // No lock
    }

    data class Credential(
        val type: AuthType,
        val hash: String,
        val createdTime: Long
    )

    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error hashing password", e)
            ""
        }
    }

    /**
     * Set PIN authentication (4 or 6 digits)
     */
    fun setPinAuthentication(pin: String, digits: Int = 4): Boolean {
        return try {
            if (pin.length != digits || !pin.all { it.isDigit() }) {
                Log.e(TAG, "Invalid PIN format. Must be $digits digits")
                return false
            }

            val authType = if (digits == 4) AuthType.PIN_4 else AuthType.PIN_6
            val hash = hashPassword(pin)

            prefs.edit().apply {
                putString("auth_type", authType.name)
                putString("auth_hash", hash)
                putLong("auth_created", System.currentTimeMillis())
            }.apply()

            Log.d(TAG, "PIN authentication set ($digits digits)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting PIN", e)
            false
        }
    }

    /**
     * Set password authentication (16+ characters)
     */
    fun setPasswordAuthentication(password: String): Boolean {
        return try {
            if (password.length < 16) {
                Log.e(TAG, "Password must be at least 16 characters")
                return false
            }

            val hash = hashPassword(password)

            prefs.edit().apply {
                putString("auth_type", AuthType.PASSWORD.name)
                putString("auth_hash", hash)
                putLong("auth_created", System.currentTimeMillis())
            }.apply()

            Log.d(TAG, "Password authentication set")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting password", e)
            false
        }
    }

    /**
     * Set parental PIN (different from main password)
     */
    fun setParentalPin(pin: String): Boolean {
        return try {
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                Log.e(TAG, "Parental PIN must be 4 digits")
                return false
            }

            val hash = hashPassword(pin)
            prefs.edit().putString("parental_hash", hash).apply()

            Log.d(TAG, "Parental PIN set")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting parental PIN", e)
            false
        }
    }

    /**
     * Verify authentication
     */
    fun verifyAuthentication(input: String): Boolean {
        return try {
            val storedHash = prefs.getString("auth_hash", "") ?: return false
            val inputHash = hashPassword(input)
            inputHash == storedHash
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying authentication", e)
            false
        }
    }

    /**
     * Verify parental PIN
     */
    fun verifyParentalPin(input: String): Boolean {
        return try {
            val storedHash = prefs.getString("parental_hash", "") ?: return false
            val inputHash = hashPassword(input)
            inputHash == storedHash
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying parental PIN", e)
            false
        }
    }

    /**
     * Get current authentication type
     */
    fun getAuthType(): AuthType {
        val typeStr = prefs.getString("auth_type", AuthType.NONE.name) ?: AuthType.NONE.name
        return try {
            AuthType.valueOf(typeStr)
        } catch (e: Exception) {
            AuthType.NONE
        }
    }

    /**
     * Check if authentication is set
     */
    fun isAuthenticationSet(): Boolean {
        return getAuthType() != AuthType.NONE
    }

    /**
     * Clear all authentication
     */
    fun clearAuthentication() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Authentication cleared")
    }

    /**
     * Disable lock screen
     */
    fun disableLockScreen() {
        prefs.edit().putString("auth_type", AuthType.NONE.name).apply()
        Log.d(TAG, "Lock screen disabled")
    }

    /**
     * Get creation time of authentication
     */
    fun getCreationTime(): Long {
        return prefs.getLong("auth_created", 0L)
    }

    /**
     * Check if parental PIN is set
     */
    fun hasParentalPin(): Boolean {
        return prefs.contains("parental_hash")
    }
}
