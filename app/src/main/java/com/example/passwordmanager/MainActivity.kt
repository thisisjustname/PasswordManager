package com.example.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.color.DynamicColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity(), SecuritySettingsListener  {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var settingsButton: ImageButton
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var appLifecycleHandler: AppLifecycleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferencesManager = PreferencesManager(this)
        biometricPromptManager = BiometricPromptManager(this)
        appLifecycleHandler = AppLifecycleHandler(this, preferencesManager)
        application.registerActivityLifecycleCallbacks(appLifecycleHandler)
        ScreenshotProtectionHelper.applyScreenshotProtection(this)
        (application as MyApp).addSecuritySettingsListener(this)

        setupViewPager()
        setupSettingsButton()
    }

    private fun setAppTheme() {
        if (preferencesManager.useDynamicColors()) {
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            setTheme(R.style.Theme_PasswordManager)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!preferencesManager.isAuthenticated()) {
            startAuthenticationActivity()
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.password_text)
                1 -> getString(R.string.cards_text)
                2 -> getString(R.string.documents_text)
                else -> ""
            }
        }.attach()
    }


    private fun setupSettingsButton() {
        settingsButton = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startAuthenticationActivity() {
        val intent = if (preferencesManager.isPinSet()) {
            Intent(this, AuthenticationActivity::class.java)
        } else {
            Intent(this, PinSetupActivity::class.java)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as MyApp).removeSecuritySettingsListener(this)
        application.unregisterActivityLifecycleCallbacks(appLifecycleHandler)
    }

    override fun onSecuritySettingsChanged() {
        ScreenshotProtectionHelper.applyScreenshotProtection(this)
    }

    private fun setupWindowAnimations() {
        val transition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(ChangeTransform())
            addTransition(ChangeImageTransform())
            duration = 300 // или другое подходящее значение
        }
        window.sharedElementEnterTransition = transition
        window.sharedElementReturnTransition = transition
    }
}