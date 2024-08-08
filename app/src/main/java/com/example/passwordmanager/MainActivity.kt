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
    private lateinit var preferencesManager: PreferencesManager
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

        preferencesManager = PreferencesManager(this)
        appLifecycleHandler = AppLifecycleHandler(this, preferencesManager)
        application.registerActivityLifecycleCallbacks(appLifecycleHandler)

        checkAuthentication()


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

    override fun onResume() {
        super.onResume()
        checkAuthentication()
    }

    private fun isPinSet(): Boolean {
        val sharedPrefs = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPrefs.contains("PIN")
    }

    private fun checkAuthentication() {
        if (!preferencesManager.isPinSet()) {
            // Если PIN не установлен, переходим к PinSetupActivity
            startActivity(Intent(this, PinSetupActivity::class.java))
            finish()
        } else if (!preferencesManager.isAuthenticated()) {
            // Если PIN установлен, но пользователь не аутентифицирован
            startAuthenticationActivity()
        }
    }

    private fun startAuthenticationActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()  // Завершаем MainActivity, чтобы пользователь не мог вернуться назад без аутентификации
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