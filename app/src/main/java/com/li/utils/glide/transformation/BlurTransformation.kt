package com.li.utils.glide.transformation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.bumptech.glide.load.Key.CHARSET
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.util.Util
import com.li.utils.glide.Toolkit
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * 实现模糊转换的 Glide Transformation
 *
 * Created: 2023/07/15
 * @author Gleamrise
 */
class BlurTransformation @JvmOverloads constructor(
    private val radius: Int = MAX_RADIUS,
    private val sampling: Int = DEFAULT_DOWN_SAMPLING
) : BitmapTransformation() {

    companion object {
        private const val VERSION = 1
        private const val ID = "com.li.utils.glide.transformation.BlurTransformation.$VERSION"
        private val ID_BYTES = ID.toByteArray(CHARSET)
        private const val MAX_RADIUS = 25
        // 默认采样大小
        private const val DEFAULT_DOWN_SAMPLING = 1
    }

    init {
        check(radius in 1..MAX_RADIUS) {
            "The `radius` must be in [1, 25]!"
        }
    }

    override fun transform(
        context: Context,
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val scaledWidth = width / sampling
        val scaledHeight = height / sampling
        var bitmap = pool[scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888]
        setCanvasBitmapDensity(toTransform, bitmap)
        // 新 canvas 进行缩放
        val canvas = Canvas(bitmap)
        canvas.scale(1f / sampling, 1f / sampling)
        val paint = Paint()
        paint.flags = Paint.FILTER_BITMAP_FLAG
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        // 调用 toolkit 的相应方法进行模糊处理
        bitmap = Toolkit.blur(bitmap, radius)
        return bitmap
    }

    override fun toString(): String {
        return "BlurTransformation(radius=$radius, sampling=$sampling)"
    }

    override fun equals(other: Any?): Boolean {
        return other is BlurTransformation && other.radius == radius && other.sampling == sampling
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), Util.hashCode(radius, Util.hashCode(sampling)))
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        val radiusData = ByteBuffer.allocate(4).putInt(radius).array()
        messageDigest.update(radiusData)
        val sampling = ByteBuffer.allocate(4).putInt(sampling).array()
        messageDigest.update(sampling)
    }


}