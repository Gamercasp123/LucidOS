package com.lucidos.lockscreen.ui

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lucidos.lockscreen.R
import com.lucidos.lockscreen.security.CredentialManager
import com.lucidos.lockscreen.auth.PinAuthHandler
import com.lucidos.lockscreen.auth.PasswordAuthHandler

/**
 * Lock Screen Activity - Main authentication interface
 */
class LockScreenActivity : AppCompatActivity() {
    private const val TAG = "LockScreenActivity"

    private lateinit var credentialManager: CredentialManager
    private lateinit var pinHandler: PinAuthHandler
    private lateinit var passwordHandler: PasswordAuthHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        Log.d(TAG, "Lock Screen Activity created")

        // Initialize managers
        credentialManager = CredentialManager(this)
        pinHandler = PinAuthHandler(credentialManager)
        passwordHandler = PasswordAuthHandler(credentialManager)

        // Check if lock is enabled
        if (!credentialManager.isAuthenticationSet()) {
            Log.d(TAG, "Lock screen not enabled, unlocking")
            unlockDevice()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        val authType = credentialManager.getAuthType()
        val inputField = findViewById<EditText>(R.id.auth_input)
        val submitButton = findViewById<Button>(R.id.submit_button)
        val statusText = findViewById<TextView>(R.id.status_text)

        when (authType) {
            CredentialManager.AuthType.PIN_4 -> {
                statusText.text = "Enter 4-Digit PIN"
                inputField.hint = "0000"
                inputField.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            CredentialManager.AuthType.PIN_6 -> {
                statusText.text = "Enter 6-Digit PIN"
                inputField.hint = "000000"
                inputField.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            CredentialManager.AuthType.PASSWORD -> {
                statusText.text = "Enter Password"
                inputField.hint = "16+ characters"
                inputField.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> {
                Log.w(TAG, "Unknown auth type")
                unlockDevice()
                return
            }
        }

        submitButton.setOnClickListener {
            val input = inputField.text.toString()
            authenticateUser(input, authType, statusText)
        }
    }

    private fun authenticateUser(
        input: String,
        authType: CredentialManager.AuthType,
        statusText: TextView
    ) {
        val result = when (authType) {
            CredentialManager.AuthType.PIN_4, CredentialManager.AuthType.PIN_6 -> {
                pinHandler.validatePin(input)
            }
            CredentialManager.AuthType.PASSWORD -> {
                passwordHandler.validatePassword(input)
            }
            else -> PinAuthHandler.AuthResult.Invalid("Unknown auth type")
        }

        when (result) {
            PinAuthHandler.AuthResult.Success -> {
                Log.d(TAG, "Authentication successful")
                statusText.text = "Unlocked!"
                unlockDevice()
            }
            PinAuthHandler.AuthResult.LockedOut -> {
                Log.w(TAG, "Account locked")
                statusText.text = "Too many attempts. Try again later."
                statusText.setTextColor(android.graphics.Color.RED)
            }
            is PinAuthHandler.AuthResult.Invalid -> {
                Log.w(TAG, "Invalid authentication: ${result.message}")
                statusText.text = result.message
                statusText.setTextColor(android.graphics.Color.RED)
            }
        }
    }

    private fun unlockDevice() {
        Log.d(TAG, "Device unlocking")
        finish()
    }

    override fun onBackPressed() {
        // Prevent back key from unlocking
        Log.d(TAG, "Back pressed - staying on lock screen")
    }
}
