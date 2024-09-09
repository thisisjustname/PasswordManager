package com.example.passwordmanager

import android.app.Activity
import android.content.Intent
import com.google.android.material.color.DynamicColors

object ThemeHelper {
    fun applyTheme(activity: Activity) {
        val preferencesManager = PreferencesManager(activity)
        if (preferencesManager.useDynamicColors()) {
            DynamicColors.applyToActivityIfAvailable(activity)
        } else {
            activity.setTheme(R.style.Theme_PasswordManager)
        }
    }

    fun changeTheme(activity: Activity) {
        val preferencesManager = PreferencesManager(activity)
        preferencesManager.setUseDynamicColors(!preferencesManager.useDynamicColors())

        // Перезапускаем все активности для применения новой темы
        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finishAffinity()
    }
}

interface ThemeChangeListener {
    fun onThemeChanged()
}