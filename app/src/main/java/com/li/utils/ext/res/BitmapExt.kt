package com.li.utils.ext.res

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.li.utils.glide.Range2d
import com.li.utils.glide.Toolkit
import com.li.utils.glide.util.BitmapUtils
import java.io.File

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */


/**
 * 保存为本地文件
 * */
fun Bitmap.toFile(
    filePath: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 0,
): Boolean {
    return BitmapUtils.writeBitmapToFile(filePath, this, format, quality)
}

fun Bitmap.toByteArray(): ByteArray {
    return BitmapUtils.bitmap2Bytes(this)
}

fun Bitmap.scale(scale: Float) = BitmapUtils.scaleBitmap(this, scale)

fun Bitmap.scale(scaleX: Float, scaleY: Float) = BitmapUtils.scaleBitmap(this, scaleX, scaleY)

fun Bitmap.rotate(degree: Float) = BitmapUtils.rotateBitmap(this, degree)
/**
 * 高斯模糊
 * @param radius 模糊半径 [1, 25]
 * @param restriction 限制模糊范围
 * */
fun Bitmap.blue(radius: Int = 5, restriction: Range2d? = null): Bitmap {
    return Toolkit.blur(this, radius, restriction)
}


fun Drawable.toBitmap(
    width: Int,
    height: Int
): Bitmap? {
    return BitmapUtils.drawable2Bitmap(this)
}

fun ByteArray.toBitmap(width: Int, height: Int): Bitmap {
    return BitmapUtils.decodeBitmapFromByteArray(this, width, height)
}

fun File.toBitmap(width: Int, height: Int): Bitmap {
    return BitmapUtils.decodeBitmapFromFile(this.path, width, height)
}
