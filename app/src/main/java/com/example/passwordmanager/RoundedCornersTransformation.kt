package com.example.passwordmanager

import android.graphics.*
import androidx.annotation.Px
import coil.size.Size
import coil.transform.Transformation

/**
 * A [Transformation] that rounds the corners of the image without cropping.
 *
 * @param topLeft The radius for the top left corner.
 * @param topRight The radius for the top right corner.
 * @param bottomLeft The radius for the bottom left corner.
 * @param bottomRight The radius for the bottom right corner.
 */
class RoundedCornersTransformation(
    @Px private val topLeft: Float = 0f,
    @Px private val topRight: Float = 0f,
    @Px private val bottomLeft: Float = 0f,
    @Px private val bottomRight: Float = 0f
) : Transformation {

    constructor(@Px radius: Float) : this(radius, radius, radius, radius)

    init {
        require(topLeft >= 0 && topRight >= 0 && bottomLeft >= 0 && bottomRight >= 0) { "All radii must be >= 0." }
    }

    override val cacheKey: String
        get() = "${RoundedCornersTransformation::class.java.name}-$topLeft,$topRight,$bottomLeft,$bottomRight"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val output = Bitmap.createBitmap(input.width, input.height, input.config)

        Canvas(output).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            paint.shader = BitmapShader(input, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val radii = floatArrayOf(topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft)
            val rect = RectF(0f, 0f, input.width.toFloat(), input.height.toFloat())
            val path = Path().apply { addRoundRect(rect, radii, Path.Direction.CW) }
            drawPath(path, paint)
        }

        return output
    }
}