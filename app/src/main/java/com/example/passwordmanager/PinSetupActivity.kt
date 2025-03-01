package com.example.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PinSetupActivity : AppCompatActivity() {
    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var confirmPasswordView: com.google.android.material.textfield.TextInputLayout
    private var isConfirmationStep = false
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_setup)
        preferencesManager = PreferencesManager(this)

        pinEditText = findViewById(R.id.pinEditText)
        confirmPinEditText = findViewById(R.id.confirmPinEditText)
        submitButton = findViewById(R.id.submitButton)
        confirmPasswordView = findViewById(R.id.textFieldTest)

        confirmPasswordView.visibility = View.GONE

        submitButton.setOnClickListener {
            if (!isConfirmationStep) {
                if (pinEditText.text.length == 6) {
                    isConfirmationStep = true
                    pinEditText.isEnabled = false
                    confirmPasswordView.visibility = View.VISIBLE
                    submitButton.text = getString(R.string.сonfirm_pin)
                } else {
                    Toast.makeText(this, getString(R.string.app_pin_length), Toast.LENGTH_SHORT).show()
                }
            } else {
                if (pinEditText.text.toString() == confirmPinEditText.text.toString()) {
                    savePin(pinEditText.text.toString())
                    finish()
                } else {
                    Toast.makeText(this, getString(R.string.pins_not_match), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePin(pin: String) {
        preferencesManager.savePin(pin)
        preferencesManager.setAuthenticated(true)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}