package com.example.passwordmanager

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageView: ZoomableImageView = findViewById(R.id.fullImageView)
        val closeButton: ImageView = findViewById(R.id.closeButton)

        val transitionName = intent.getStringExtra("transitionName")
        ViewCompat.setTransitionName(imageView, transitionName)

        postponeEnterTransition()

        val imageUri: Uri? = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
        imageUri?.let {
            Glide.with(this)
                .load(it)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition()
                        imageView.setImageLoaded()
                        return false
                    }
                })
                .into(imageView)
        }

        closeButton.setOnClickListener {
            finishAfterTransition()
        }
    }
}