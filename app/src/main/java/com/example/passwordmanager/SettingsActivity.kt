package com.example.passwordmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView

interface SecuritySettingsListener {
    fun onSecuritySettingsChanged()
}

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchCompat
    private lateinit var screenshotProtectionSwitch: SwitchCompat
    private lateinit var themeDescription: TextView
    private lateinit var screenshotProtectionDescription: TextView
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferencesManager = PreferencesManager(this)

        themeSwitch = findViewById(R.id.themeSwitch)
        screenshotProtectionSwitch = findViewById(R.id.screenshotProtectionSwitch)
        themeDescription = findViewById(R.id.themeDescription)
        screenshotProtectionDescription = findViewById(R.id.screenshotProtectionDescription)

        setupThemeSwitch()
        setupScreenshotProtectionSwitch()

        ScreenshotProtectionHelper.applyScreenshotProtection(this)
    }

    private fun setupThemeSwitch() {
        val useDynamicColors = preferencesManager.useDynamicColors()
        themeSwitch.isChecked = useDynamicColors
        updateThemeDescription(useDynamicColors)

        themeSwitch.setOnCheckedChangeListener { _, _ ->
            ThemeHelper.changeTheme(this)
        }
    }

    private fun setupScreenshotProtectionSwitch() {
        val isScreenshotProtectionEnabled = preferencesManager.isScreenshotProtectionEnabled()
        screenshotProtectionSwitch.isChecked = isScreenshotProtectionEnabled
        updateScreenshotProtectionDescription(isScreenshotProtectionEnabled)

        screenshotProtectionSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setScreenshotProtection(isChecked)
            updateScreenshotProtectionDescription(isChecked)
            ScreenshotProtectionHelper.applyScreenshotProtection(this)
            (application as MyApp).notifySecuritySettingsChanged()
        }
    }

    private fun updateThemeDescription(useDynamicColors: Boolean) {
        themeDescription.text = if (useDynamicColors) {
            getString(R.string.dynamic_colors_description)
        } else {
            getString(R.string.custom_theme_description)
        }
    }



    private fun updateScreenshotProtectionDescription(isEnabled: Boolean) {
        screenshotProtectionDescription.text = if (isEnabled) {
            getString(R.string.screenshot_protection_enabled_description)
        } else {
            getString(R.string.screenshot_protection_disabled_description)
        }
    }
}