package com.lucidos.lockscreen.ui

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lucidos.lockscreen.R
import com.lucidos.lockscreen.security.CredentialManager

/**
 * Lock Screen Setup Activity - Configure authentication
 */
class LockSetupActivity : AppCompatActivity() {
    private val TAG = "LockSetupActivity"

    private lateinit var credentialManager: CredentialManager
    private var selectedAuthType = CredentialManager.AuthType.PIN_4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_setup)

        Log.d(TAG, "Lock Setup Activity created")

        credentialManager = CredentialManager(this)

        setupUI()
    }

    private fun setupUI() {
        val authTypeGroup = findViewById<RadioGroup>(R.id.auth_type_group)
        val credentialInput = findViewById<EditText>(R.id.credential_input)
        val parentalPinInput = findViewById<EditText>(R.id.parental_pin_input)
        val saveButton = findViewById<Button>(R.id.save_button)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        authTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedAuthType = when (checkedId) {
                R.id.radio_pin_4 -> CredentialManager.AuthType.PIN_4
                R.id.radio_pin_6 -> CredentialManager.AuthType.PIN_6
                R.id.radio_password -> CredentialManager.AuthType.PASSWORD
                else -> CredentialManager.AuthType.PIN_4
            }
            updateInputHint(credentialInput)
        }

        saveButton.setOnClickListener {
            val credential = credentialInput.text.toString()
            val parentalPin = parentalPinInput.text.toString()
            setupAuthentication(credential, parentalPin)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun updateInputHint(input: EditText) {
        when (selectedAuthType) {
            CredentialManager.AuthType.PIN_4 -> {
                input.hint = "Enter 4-digit PIN (0000)"
                input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            CredentialManager.AuthType.PIN_6 -> {
                input.hint = "Enter 6-digit PIN (000000)"
                input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            }
            CredentialManager.AuthType.PASSWORD -> {
                input.hint = "Enter password (16+ chars)"
                input.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            else -> {}
        }
    }

    private fun setupAuthentication(credential: String, parentalPin: String) {
        Log.d(TAG, "Setting up authentication")

        // Validate and set main authentication
        val success = when (selectedAuthType) {
            CredentialManager.AuthType.PIN_4 -> {
                if (credential.length != 4 || !credential.all { it.isDigit() }) {
                    showError("PIN must be exactly 4 digits")
                    false
                } else {
                    credentialManager.setPinAuthentication(credential, 4)
                }
            }
            CredentialManager.AuthType.PIN_6 -> {
                if (credential.length != 6 || !credential.all { it.isDigit() }) {
                    showError("PIN must be exactly 6 digits")
                    false
                } else {
                    credentialManager.setPinAuthentication(credential, 6)
                }
            }
            CredentialManager.AuthType.PASSWORD -> {
                if (credential.length < 16) {
                    showError("Password must be at least 16 characters")
                    false
                } else {
                    credentialManager.setPasswordAuthentication(credential)
                }
            }
            else -> false
        }

        if (!success) return

        // Set parental PIN if provided
        if (parentalPin.isNotEmpty()) {
            if (parentalPin.length != 4 || !parentalPin.all { it.isDigit() }) {
                showError("Parental PIN must be 4 digits")
                return
            }
            credentialManager.setParentalPin(parentalPin)
            Log.d(TAG, "Parental PIN set")
        }

        showSuccess("Lock screen configured successfully")
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
    }
}
