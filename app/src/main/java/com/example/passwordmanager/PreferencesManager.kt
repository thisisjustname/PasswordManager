package com.example.passwordmanager

import android.content.Context

class PreferencesManager(context: Context) {
    private val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    fun setAuthenticated(authenticated: Boolean) {
        prefs.edit().putBoolean("is_authenticated", authenticated).apply()
    }

    fun isAuthenticated(): Boolean {
        return prefs.getBoolean("is_authenticated", false)
    }

    fun savePin(pin: String) {
        prefs.edit().putString("user_pin", pin).apply()
    }

    fun getPin(): String? {
        return prefs.getString("user_pin", null)
    }

    fun isPinSet(): Boolean {
        return getPin() != null
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    fun setUseDynamicColors(useDynamic: Boolean) {
        prefs.edit().putBoolean("use_dynamic_colors", useDynamic).apply()
    }

    fun useDynamicColors(): Boolean {
        return prefs.getBoolean("use_dynamic_colors", true) // По умолчанию используем динамические цвета
    }

    fun setScreenshotProtection(enabled: Boolean) {
        prefs.edit().putBoolean("screenshot_protection", enabled).apply()
    }

    fun isScreenshotProtectionEnabled(): Boolean {
        return prefs.getBoolean("screenshot_protection", false) // По умолчанию выключено
    }
}