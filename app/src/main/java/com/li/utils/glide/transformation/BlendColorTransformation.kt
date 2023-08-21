package com.li.utils.glide.transformation

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import com.li.utils.glide.BlendingMode
import com.li.utils.glide.Toolkit
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/18
 */
class BlendColorTransformation(
    val color: Int
): BitmapTransformation() {
    companion object {
        private const val TAG = "BlendColorTransformation"
        private val ID = "com.sll.lib_glide.transformation.$TAG"
        private val ID_BYTES = ID.toByteArray(CHARSET)
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val bitmap = pool[width, height, Bitmap.Config.ARGB_8888]
        bitmap.density = toTransform.density
        bitmap.eraseColor(color)
        Toolkit.blend(BlendingMode.MULTIPLY, bitmap, toTransform)
        pool.put(bitmap)
        return toTransform
    }


    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(color).array())
    }



    override fun equals(other: Any?): Boolean {
        return other is BlendColorTransformation && other.color == this.color
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), Util.hashCode(color))
    }

    override fun toString(): String {
        return "BlendTransformation (color=${color})"
    }
}