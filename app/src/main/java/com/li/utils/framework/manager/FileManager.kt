package com.li.utils.framework.manager

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
object FileManager {
    private const val TAG = "FileUtils"

    // 包名
    private var PACKAGE_NAME = "com.sll.serenejourney"

    // 媒体模块根目录
    private val DIR_SAVE_MEDIA_ROOT = Environment.DIRECTORY_PICTURES

    // 媒体模块存储路径
    private val DIR_SAVE_MEDIA: String = "$DIR_SAVE_MEDIA_ROOT/serenejourney"


    // 头像保存路径
    private const val DIR_AVATAR = "/avatar"

    // 图片缓存等保存路径
    private const val DIR_IMAGE = "/image"

    // JPG后缀
    const val SUFFIX_JPG = ".jpg"

    // PNG后缀
    const val SUFFIX_PNG = ".png"

    // MP4后缀
    const val SUFFIX_MP4 = ".mp4"

    // YUV后缀
    const val SUFFIX_YUV = ".yuv"

    // h264后缀
    const val SUFFIX_H264 = ".h264"


    val DEFAULT_SAVE_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}${File.separator}Save"



    /**
     * 配置项
     * */
    object Config {
        /**
         * 设置包名
         * */
        fun setPackageName(packageName: String): Config {
            PACKAGE_NAME = packageName
            return this
        }
    }

    /**
     * 返回 File 对应的
     * @param context
     * @param file
     * */
    // 未设置 FileProvider
//    fun getUriFromFile(context: Context, file: File): Uri {
//        // 转换为uri
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            // 适配Android 7.0文件权限，通过FileProvider创建一个content类型的Uri
//            FileProvider.getUriForFile(
//                context,
//                "$PACKAGE_NAME.fileProvider",
//                file
//            )
//        } else {
//            Uri.fromFile(file)
//        }
//    }
//
//    /**
//     * 返回 [path] 对应的 Uri
//     * */
//    fun getUriFromPath(context: Context, path: String?): Uri? {
//        return path?.let { getUriFromFile(context, File(it)) }
//    }


    /**
     * 选择一个外部文件
     * @return intent
     * */
    fun pickExternalFile(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            Intent.createChooser(this, "选择文件")
        }
    }

    /**
     * 将外部文件的 uri 复制到私有目录
     *
     * Android 11开始只能在私有目录或者共享目录创建文件
     * @param context
     * @param srcUri 源文件的uri
     * @param dstFolder 私有目录的文件夹名称
     * */
    fun copyUriToInnerStorage(context: Context, srcUri: Uri, dstFolder: String, dstFile: String): File {
        val folder = File(dstFolder)
        val exist = if (!folder.exists()) folder.mkdirs() else true
        val file = File(dstFolder, dstFile)
        if (exist) {
            if (file.exists()) file.delete()
            context.contentResolver.openInputStream(srcUri).use {
                try {
                    val fileOutputStream = FileOutputStream(file)
                    val buffer = ByteArray(1024)
                    var readCount = it?.read(buffer) ?: -1
                    while (readCount >= 0) {
                        fileOutputStream.write(buffer, 0, readCount)
                        readCount = it?.read(buffer) ?: -1
                    }
                    fileOutputStream.flush()
                    fileOutputStream.fd.sync()
                    fileOutputStream.close()
                } catch (e: IOException) {
                    Log.e(TAG, "copyUriToInnerStorage: $e")
                }
            }
        }
        return file
    }


    // =========================== 文件大小相关 ============================

    // 获取文件大小
    // Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/
    // 目录，一般放一些长时间保存的数据
    // Context.getExternalCacheDir() -->
    // SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据

    /**
     * 获取指定文件夹/文件的大小
     * @param file
     * @return 文件大小
     * */
    fun getFileSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            val size2: Int
            if (fileList != null) {
                size2 = fileList.size
                for (i in 0 until size2) {
                    // 如果下面还有文件
                    size = if (fileList[i].isDirectory) {
                        size + getFileSize(fileList[i])
                    } else {
                        size + fileList[i].length()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }


    /**
     * 格式化单位，保留两位小数的最大单位
     *
     * @param size
     * @return
     */
    fun getFormatSizeString(size: Double): String {
        val kiloByte = size / 1024
        if (kiloByte < 1) {
            return if (kiloByte == 0.0) {
                "0KB"
            } else size.toString() + "KB"
        }
        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(kiloByte.toString())
            return result1.setScale(2, RoundingMode.HALF_UP).toPlainString() + "KB"
        }
        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(megaByte.toString())
            return result2.setScale(2, RoundingMode.HALF_UP).toPlainString() + "MB"
        }
        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(gigaByte.toString())
            return result3.setScale(2, RoundingMode.HALF_UP).toPlainString() + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        return (result4.setScale(2, RoundingMode.HALF_UP).toPlainString() + "TB")
    }


    // =========================== 删除文件 =================================


    /**
     * 删除空文件夹
     * @param path 文件夹地址
     * @return 是否删除成功，true为删除成功
     */
    private fun deleteFolder(path: String): Boolean {
        return try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            false
        }
    }

    /**
     * 递归删除目录下的所有文件
     * @param path 文件夹目录
     * @return 是否删除
     */
    fun deleteAllFiles(path: String): Boolean {
        val file = File(path)
        // 文件夹不存在
        if (!file.exists()) {
            return false
        }
        // file不是文件夹
        if (!file.isDirectory) {
            return false
        }
        // 文件夹下面的文件
        val tempList = file.list() ?: return false
        // 成功删除的标志
        var flag = false
        var temp: File?
        for (i in tempList.indices) {
            temp = if (path.endsWith(File.separator)) {
                File(path + tempList[i])
            } else {
                File(path + File.separator + tempList[i])
            }
            // 文件删除
            if (temp.isFile) {
                temp.delete()
            }
            if (temp.isDirectory) {
                // 先删除文件夹里面的文件
                deleteAllFiles(path + "/" + tempList[i])
                // 再删除空文件夹
                deleteFolder(path + "/" + tempList[i])
                flag = true
            }
        }
        return flag
    }


    // =========================== 图片相关 =================================

    /**
     * 保存图片到本地
     * @param context
     * @param bitmap
     * @param prefix 文件名前缀
     * @param format 保存格式
     * @return 保存后的uri，空为保存失败
     * */
    fun saveImage(
        context: Context,
        bitmap: Bitmap,
        prefix: String = "",
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        savePath: String = DEFAULT_SAVE_PATH,
    ): Uri? {
        // 后缀名
        val suffix = format.toString()
        // 文件名
        val filename = "$prefix-${System.currentTimeMillis()}.$suffix"

        val values = ContentValues().apply {
            // 文件类型
            put(MediaStore.Images.Media.MIME_TYPE, "image/$suffix")
            // 文件名称
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            // 存放路径
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 及以上不再使用 DATA 字段，使用相对路径
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Save")
            } else {
                // Android 10 以下没有分区存储
                put(
                    MediaStore.MediaColumns.DATA,
                    "${(savePath)}${File.separator}${prefix}-${System.currentTimeMillis()}.$suffix"
                )
            }
        }
        // 插入
        val insertUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        // 输出
        insertUri?.let {
            context.contentResolver.openOutputStream(it).use { ops ->
                bitmap.compress(format, 100, ops)
            }
        }
        return insertUri
    }


    /**
     * 创建一个临时文件，适配 Android 10（Q）的分区存储
     * @param context
     * @param prefix 文件前缀
     * @param suffix 文件后缀
     * @return 对应文件
     * */
    fun createTempFile(context: Context, prefix: String = "", suffix: String): File {
        val tmpFile = File(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            else
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp-${prefix}-${System.currentTimeMillis()}.${suffix}"
        )
        try {
            if (tmpFile.exists()) {
                tmpFile.delete()
            }
            tmpFile.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return tmpFile
    }


    /**
     * 获取应用总缓存大小
     *
     * @param context
     * @param dirPaths 额外的缓存目录
     * @return
     */
    fun getTotalCacheSize(context: Context, vararg dirPaths: String?): String {
        return try {
            var cacheSize: Long = 0 // = FileUtils.getFolderSize(context.getCacheDir());
            if (Environment.getExternalStorageState() ==
                Environment.MEDIA_MOUNTED
            ) {
                val file = context.externalCacheDir
                cacheSize += if (file == null) 0 else getFileSize(file)
                for (dirPath in dirPaths) {
                    dirPath?.let {
                        val current = File(dirPath)
                        cacheSize += getFileSize(current)
                    }
                }
            }
            getFormatSizeString(cacheSize.toDouble())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            "0KB"
        }
    }

    /**
     * 获取缓存目录
     * @param context
     */
    fun getDiskCacheDir(context: Context): String? {
        val cachePath: String? =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                context.externalCacheDir?.path
            } else {
                context.cacheDir.path
            }
        return cachePath
    }


}