package com.example.passwordmanager

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.passwordmanager.R
import com.example.passwordmanager.ViewPagerAdapter
import com.google.android.material.color.DynamicColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.plcoding.biometricauth.BiometricPromptManager

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var appLifecycleHandler: AppLifecycleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "Debug message")
        Log.i("MainActivity", "Info message")
        Log.w("MainActivity", "Warning message")
        Log.e("MainActivity", "Error message")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        biometricPromptManager = BiometricPromptManager(this)

        if (!isPinSet()) {
            startActivity(Intent(this, PinSetupActivity::class.java))
        } else {
            appLifecycleHandler = AppLifecycleHandler(this)
            application.registerActivityLifecycleCallbacks(appLifecycleHandler)
        }


        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("IsFirstRun", true)


        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.password_text)
                1 -> getString(R.string.cards_text)
                else -> ""
            }
        }.attach()
    }

    private fun isPinSet(): Boolean {
        val sharedPrefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPrefs.contains("PIN")
    }

    override fun onDestroy() {
        super.onDestroy()
        appLifecycleHandler?.let {
            application.unregisterActivityLifecycleCallbacks(it)
        }
        Log.d("MainActivity", "onDestroy")
        // Отмена регистрации BroadcastReceiver
    }
}