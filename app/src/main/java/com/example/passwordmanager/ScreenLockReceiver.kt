package com.example.passwordmanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.plcoding.biometricauth.BiometricPromptManager

// BroadcastReceiver для отслеживания блокировки экрана
class ScreenLockReceiver(private val biometricPromptManager: BiometricPromptManager) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            biometricPromptManager.showBiometricPrompt(
                title = "Authentication Required",
                description = "Please authenticate to continue"
            )
        }
    }
}