package com.example.passwordmanager

import android.app.Application
import android.util.Log
import com.google.android.material.color.DynamicColors

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("MainActivity", "hello")
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
