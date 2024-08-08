package com.example.passwordmanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.plcoding.biometricauth.BiometricPromptManager
import kotlinx.coroutines.launch

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var pinEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var useBiometricsButton: Button
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        preferencesManager = PreferencesManager(this)

        if (!preferencesManager.isPinSet()) {
            // Если PIN не установлен, переходим к PinSetupActivity
            startActivity(Intent(this, PinSetupActivity::class.java))
            finish()
            return
        }

        pinEditText = findViewById(R.id.pinEditText)
        submitButton = findViewById(R.id.submitButton)
        useBiometricsButton = findViewById(R.id.useBiometricsButton)

        biometricPromptManager = BiometricPromptManager(this)

        submitButton.setOnClickListener {
            if (checkPin(pinEditText.text.toString())) {
                proceedToMainActivity()
            } else {
                Toast.makeText(this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show()
            }
        }

        useBiometricsButton.setOnClickListener {
            biometricPromptManager.showBiometricPrompt(
                title = "Authentication Required",
                description = "Use your biometric credential to authenticate"
            )
        }

        // Наблюдаем за результатами биометрической аутентификации
        lifecycleScope.launch {
            biometricPromptManager.promptResults.collect { result ->
                when (result) {
                    is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> proceedToMainActivity()
                    is BiometricPromptManager.BiometricResult.AuthenticationError ->
                        Toast.makeText(this@AuthenticationActivity, "Authentication failed: ${result.error}", Toast.LENGTH_SHORT).show()
                    else -> {} // Обработайте другие случаи по необходимости
                }
            }
        }
    }

    private fun checkPin(enteredPin: String): Boolean {
        return enteredPin == preferencesManager.getPin()
    }

    private fun proceedToMainActivity() {
        preferencesManager.setAuthenticated(true)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}