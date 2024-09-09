package com.example.passwordmanager

import android.app.Activity
import android.view.WindowManager

object ScreenshotProtectionHelper {
    fun applyScreenshotProtection(activity: Activity) {
        val preferencesManager = PreferencesManager(activity)
        if (preferencesManager.isScreenshotProtectionEnabled()) {
            enableScreenshotProtection(activity)
        } else {
            disableScreenshotProtection(activity)
        }
    }

    private fun enableScreenshotProtection(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun disableScreenshotProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}