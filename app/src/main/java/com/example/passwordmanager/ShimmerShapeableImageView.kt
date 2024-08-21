package com.example.passwordmanager

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.imageview.ShapeableImageView

class ShimmerShapeableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    private val shimmerDrawable: Drawable by lazy {
        AppCompatResources.getDrawable(context, R.drawable.shimmer_placeholder)!!
    }

    init {
        setBackgroundResource(R.drawable.shimmer_placeholder)
        startShimmerAnimation()
    }

    fun startShimmerAnimation() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.shimmer_animation)
        Log.d("ShimmerImageView", "Starting shimmer animation")
        startAnimation(animation)
    }

    fun stopShimmerAnimation() {
        clearAnimation()
        Log.d("ShimmerImageView", "Starting stopped animation")
        background = null
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            background = null
            stopShimmerAnimation()
        }
    }
}