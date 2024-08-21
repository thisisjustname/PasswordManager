package com.example.passwordmanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class AppLifecycleHandler(private val context: Context, private val preferencesManager: PreferencesManager) : Application.ActivityLifecycleCallbacks {
    private var appInBackground = false
    private var lastPausedTime: Long = 0
    private val AUTH_TIMEOUT = 3 * 60 * 1000 // 3 minutes in milliseconds

    private val handler = Handler(Looper.getMainLooper())
    private var authRunnable: Runnable? = null

    override fun onActivityResumed(activity: Activity) {
        if (appInBackground) {
            appInBackground = false
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPausedTime > AUTH_TIMEOUT) {
                preferencesManager.setAuthenticated(false)
                if (activity !is AuthenticationActivity && activity !is PinSetupActivity) {
                    startAuthenticationActivity(activity)
                }
            }
        }
        authRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onActivityPaused(activity: Activity) {
        lastPausedTime = System.currentTimeMillis()
        authRunnable = Runnable {
            appInBackground = true
            preferencesManager.setAuthenticated(false)
        }
        handler.postDelayed(authRunnable!!, AUTH_TIMEOUT.toLong())
    }

    override fun onActivityStopped(activity: Activity) {
        if (!activity.isChangingConfigurations) {
            appInBackground = true
        }
    }

    private fun startAuthenticationActivity(activity: Activity) {
        val intent = if (preferencesManager.isPinSet()) {
            Intent(context, AuthenticationActivity::class.java)
        } else {
            Intent(context, PinSetupActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    // Implement other ActivityLifecycleCallbacks methods
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}