package com.example.passwordmanager

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import coil.load
import coil.transform.CircleCropTransformation

class FullImageActivity : AppCompatActivity(), SecuritySettingsListener  {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        ScreenshotProtectionHelper.applyScreenshotProtection(this)
        (application as MyApp).addSecuritySettingsListener(this)

        val imageView: ZoomableImageView = findViewById(R.id.fullImageView)
        val closeButton: ImageView = findViewById(R.id.closeButton)

        val transitionName = intent.getStringExtra("transitionName")
        ViewCompat.setTransitionName(imageView, transitionName)

        postponeEnterTransition()

        val imageUri: Uri? = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
        imageUri?.let {
            imageView.load(it) {
                listener(
                    onStart = {
                        // Можно добавить логику при начале загрузки, если нужно
                    },
                    onSuccess = { _, _ ->
                        startPostponedEnterTransition()
                        imageView.setImageLoaded()
                    },
                    onError = { _, _ ->
                        startPostponedEnterTransition()
                    }
                )
            }
        }

        closeButton.setOnClickListener {
            finishAfterTransition()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (application as MyApp).removeSecuritySettingsListener(this)
    }

    override fun onSecuritySettingsChanged() {
        ScreenshotProtectionHelper.applyScreenshotProtection(this)
    }

}