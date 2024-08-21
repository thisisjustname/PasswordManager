package com.example.passwordmanager

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener

class ZoomableImageView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs), View.OnTouchListener {
    private val matrix = Matrix()
    private var scale = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector
    private var minScale = 1.0f
    private var last = PointF()
    private var start = PointF()
    private var mode = NONE
    private var isImageLoaded = false
    private var initialScale = 1.0f

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
        this.setOnTouchListener(this)
        scaleType = ScaleType.MATRIX
    }

    fun setImageLoaded() {
        isImageLoaded = true
        post { setupInitialScale() }
    }

    private fun setupInitialScale() {
        if (!isImageLoaded) return
        val drawable = drawable ?: return

        val drawableWidth = drawable.intrinsicWidth.toFloat()
        val drawableHeight = drawable.intrinsicHeight.toFloat()
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val widthRatio = viewWidth / drawableWidth
        val heightRatio = viewHeight / drawableHeight
        initialScale = min(widthRatio, heightRatio)

        minScale = initialScale
        scale = initialScale

        matrix.reset()
        matrix.postScale(scale, scale)

        val redundantXSpace = viewWidth - (scale * drawableWidth)
        val redundantYSpace = viewHeight - (scale * drawableHeight)
        matrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)

        imageMatrix = matrix
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false

        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val currentPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                last.set(currentPoint)
                start.set(last)
                mode = DRAG
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG && scale > minScale) {
                    val dx = currentPoint.x - last.x
                    val dy = currentPoint.y - last.y
                    matrix.postTranslate(dx, dy)
                    fixTranslation()
                    last.set(currentPoint.x, currentPoint.y)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
        }

        imageMatrix = matrix
        invalidate()
        return true
    }

    private fun fixTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        val currentScale = values[Matrix.MSCALE_X]
        val scaledWidth = drawable.intrinsicWidth * currentScale
        val scaledHeight = drawable.intrinsicHeight * currentScale
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        val fixTransX = getFixTranslation(transX, viewWidth, scaledWidth)
        val fixTransY = getFixTranslation(transY, viewHeight, scaledHeight)

        if (fixTransX != 0f || fixTransY != 0f) {
            matrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            // Если изображение меньше или равно размеру view, центрируем его
            return (viewSize - contentSize) / 2 - trans
        } else {
            // Если изображение больше view, ограничиваем его перемещение
            val minTrans = viewSize - contentSize
            val maxTrans = 0f
            return when {
                trans < minTrans -> minTrans - trans
                trans > maxTrans -> maxTrans - trans
                else -> 0f
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isImageLoaded) {
            post { setupInitialScale() }
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val previousScale = scale
            scale *= scaleFactor

            if (scale in minScale..10f) {
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                fixTranslation()
            } else {
                scale = previousScale
            }

            imageMatrix = matrix
            invalidate()
            return true
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val newScale = if (scale > minScale) minScale else minScale * 2
            val scaleFactor = newScale / scale
            matrix.postScale(scaleFactor, scaleFactor, e.x, e.y)
            scale = newScale
            fixTranslation()
            imageMatrix = matrix
            invalidate()
            return true
        }
    }
}