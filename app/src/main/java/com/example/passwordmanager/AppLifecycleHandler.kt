package com.example.passwordmanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.plcoding.biometricauth.BiometricPromptManager

class AppLifecycleHandler(private val context: Context) : Application.ActivityLifecycleCallbacks {
    private var appInBackground = false

    override fun onActivityResumed(activity: Activity) {
        if (appInBackground && activity !is AuthenticationActivity) {
            appInBackground = false
            // Приложение вернулось с фона, запускаем аутентификацию
            val intent = Intent(context, AuthenticationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (!activity.isChangingConfigurations) {
            appInBackground = true
        }
    }

    // Реализуйте остальные методы интерфейса ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}