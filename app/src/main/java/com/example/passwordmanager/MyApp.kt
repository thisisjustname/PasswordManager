package com.example.passwordmanager

import android.app.Application
import android.util.Log

class MyApp : Application() {
    private val securitySettingsListeners = mutableListOf<SecuritySettingsListener>()

    override fun onCreate() {
        super.onCreate()
        Log.d("MainActivity", "hello")
    }

    fun addSecuritySettingsListener(listener: SecuritySettingsListener) {
        securitySettingsListeners.add(listener)
    }

    fun removeSecuritySettingsListener(listener: SecuritySettingsListener) {
        securitySettingsListeners.remove(listener)
    }

    fun notifySecuritySettingsChanged() {
        securitySettingsListeners.forEach { it.onSecuritySettingsChanged() }
    }
}