package com.example.passwordmanager

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.passwordmanager.R
import com.example.passwordmanager.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.plcoding.biometricauth.BiometricPromptManager

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var screenLockReceiver: ScreenLockReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricPromptManager = BiometricPromptManager(this)
        screenLockReceiver = ScreenLockReceiver(biometricPromptManager)

        // Регистрация BroadcastReceiver
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenLockReceiver, filter)

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("IsFirstRun", true)

        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Пароли"
                1 -> "Карты"
                2 -> "Документы"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации BroadcastReceiver
        unregisterReceiver(screenLockReceiver)
    }
}