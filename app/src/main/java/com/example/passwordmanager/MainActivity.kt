package com.example.passwordmanager

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var biometricPromptManager: BiometricPromptManager
    private lateinit var appLifecycleHandler: AppLifecycleHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupWindowAnimations()

        preferencesManager = PreferencesManager(this)
        biometricPromptManager = BiometricPromptManager(this)
        appLifecycleHandler = AppLifecycleHandler(this, preferencesManager)
        application.registerActivityLifecycleCallbacks(appLifecycleHandler)

        setupViewPager()
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
        application.unregisterActivityLifecycleCallbacks(appLifecycleHandler)
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