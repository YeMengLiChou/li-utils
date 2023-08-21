package com.li.utils.glide.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/13
 */
object BitmapUtils {

    /**
     * 从 res 中读取
     * @param res
     * @param resId 资源id
     * @param width 所需宽度
     * @param height 所需高度
     * */
    fun decodeBitmapFromResource(
        res: Resources,
        resId: Int,
        width: Int,
        height: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            decodeBitmapFromIns(res.openRawResource(resId), width, height)!!
        }
    }


    /**
     * 从 外部文件 读取
     * @param filePath 外部文件路径
     * @param width 所需宽度
     * @param height 所需高度
     * */
    fun decodeBitmapFromFile(
        filePath: String,
        width: Int,
        height: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)
            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false
            BitmapFactory.decodeFile(filePath, this)
        }
    }

    /**
     * 从输入流中读取
     * @param ins 输入流
     * @param width 所需宽度
     * @param height 所需高度
     * */
    fun decodeBitmapFromIns(
        ins: InputStream,
        width: Int,
        height: Int
    ): Bitmap? {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(ins, null, this)
            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false
            BitmapFactory.decodeStream(ins, null, this)
        }
    }



    /**
     * 从 二进制数组 读取
     * @param ins 输入流
     * @param width 所需宽度
     * @param height 所需高度
     * */
    fun decodeBitmapFromByteArray(
        data: ByteArray,
        width: Int,
        height: Int
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(data, 0, data.size, this)
            inSampleSize = calculateInSampleSize(this, width, height)
            inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(data, 0, data.size, this)
        }
    }



    /**
     * 保存Bitmap到本地
     * @param filePath 保存位置
     * @param bitmap 保存的bitmap
     * @param format 格式
     * @param quality 图片质量 [0, 100]
     * */
    fun writeBitmapToFile(
        filePath: String,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 0,
    ): Boolean {
        kotlin.runCatching {
            val fos = FileOutputStream(File(filePath))
            BufferedOutputStream(fos).apply {
                bitmap.compress(format, quality, this)
                flush()
                close()
            }
        }.onSuccess { return true }
        return false
    }

    /**
     * bitmap转ByteArray
     * */
    fun bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    fun bitmap2Drawable(resources: Resources, bm: Bitmap): Drawable {
        return BitmapDrawable(resources, bm)
    }


    fun drawable2Bitmap(drawable: Drawable): Bitmap? {

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    fun scaleBitmap(bitmap: Bitmap, scale: Float): Bitmap {
        Matrix().apply {
            postScale(scale, scale)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, true)
        }
    }
    fun scaleBitmap(bitmap: Bitmap, scaleX: Float, scaleY:Float): Bitmap {
        Matrix().apply {
            postScale(scaleX, scaleY)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, true)
        }
    }

    fun rotateBitmap(b: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, true)
    }


    suspend fun downloadBitmap(url: String) {

    }

    /**
     * 网络下载图片
     * @param url
     * @return
     */
    fun downLoadBitmap(url: String): Bitmap? {
        var conn: HttpURLConnection? = null
        try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 1
                }
                return BitmapFactory.decodeStream(conn.inputStream, null, options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            conn!!.disconnect()
        }
        return null
    }
    /**
     * 根据 [reqWidth] 和 [reqHeight] 计算最接近的样本大小 inSampleSize
     * */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqWidth || width > reqHeight) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= height && halfWidth / inSampleSize >= width) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


}