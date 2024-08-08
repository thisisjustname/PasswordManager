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
}