package com.li.utils.framework.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.li.utils.framework.manager.FileManager
import java.io.File

/**
 * 照相选择的封装
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
class PhotoRequest(private val activity: AppCompatActivity) : Fragment() {

    private var tmpUri: Uri? = null

    // 从相册中选取
    private var fromSelected = false

    // 从相机获取
    private var fromCamera = false

    // 裁剪选项
    private var cropOption: CropOptions? = null

    // 需要裁剪
    private var needCrop = false

    // 相机拿到拍摄的数据的回调
    private var cameraCallback: ((bitmap: Bitmap?, state: Int) -> Unit)? = null

    // 选择图片的数据的回调
    private var selectCallback: ((uri: Uri?, state: Int) -> Unit)? = null

    // 最后拿到数据的回调
    private var buildCallback: ((Uri?, Int) -> Unit)? = null

    // 相机
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        cameraCallback?.invoke(bitmap, if (bitmap == null) State.ERROR_TAKE else State.SUCCESS)
        if (bitmap != null) {
            if (needCrop) {
                val uri = FileManager.saveImage(activity, bitmap, "crop") ?: kotlin.run {
                    buildCallback?.invoke(null, State.ERROR_SAVE)
                    return@registerForActivityResult
                }
                cropLauncher.launch(cropIntent(activity, uri)) // 调用系统裁剪
            } else {
                val uri = FileManager.saveImage(activity, bitmap)
                buildCallback?.invoke(uri, if (uri != null) State.SUCCESS else State.ERROR_SAVE)
            }
        }
    }

    // 选择图片
    private val pickLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val uri = res?.data?.data
            // 选取图片的 uri
            selectCallback?.invoke(uri, if (uri != null) State.SUCCESS else State.ERROR_SELECT)
            if (uri != null) {
                if (needCrop) { // 需要裁剪
                    cropLauncher.launch(cropIntent(activity, uri)) // 调用系统裁剪
                } else {
                    buildCallback?.invoke(uri, State.SUCCESS) // 无需裁剪
                }
            } else {
                buildCallback?.invoke(null, State.ERROR_SELECT) // 选择图片失败
            }
        } else {
            selectCallback?.invoke(null, State.CANCEL_SELECT) // 取消选择
        }
    }

    // 裁剪图片
    private val cropLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { cropData ->
        if (cropData.resultCode == Activity.RESULT_OK) {
            val resUri = cropData?.data?.data
            // TODO 转存到私有目录
            val copy = resUri?.run {
                FileManager.copyUriToInnerStorage(
                    requireContext(),
                    resUri,
                    "${requireContext().getExternalFilesDir(null)}${File.separator}Crop",
                    "crop-${System.currentTimeMillis()}.${cropOption?.outputFormat}"
                ).toUri()
            }
            resUri?.toFile()?.delete() //删除该裁剪文件
            buildCallback?.invoke(copy, if (copy != null) State.SUCCESS else State.ERROR_CROP) // 裁剪回调
        } else if (cropData.resultCode == Activity.RESULT_CANCELED) {
            buildCallback?.invoke(null, State.CANCEL_CROP) // 取消裁剪
        }
    }

    companion object {

        /**
         * 以此开始一个 PhotoRequest
         * @param activity
         * */
        fun with(activity: AppCompatActivity) = PhotoRequest(activity)

        /**
         * 以此开始一个 PhotoRequest
         * @param fragment 宿主是 [AppCompatActivity] 的 fragment
         * */
        fun with(fragment: Fragment) = with(fragment.requireActivity() as AppCompatActivity)
    }

    // 使用 camera 拍照
    fun camera(callback: ((bitmap: Bitmap?, state: Int) -> Unit)? = null): PhotoRequest {
        this.cameraCallback = callback
        this.fromCamera = true
        return this
    }

    /**
     * 裁剪
     * @param option 裁剪的选项
     * */
    fun crop(option: CropOptions): PhotoRequest {
        this.needCrop = true
        this.cropOption = option
        return this
    }

    /**
     * 相册选择图片
     * @param callback 选择图片后的回调
     * */
    fun pick(callback: ((uri: Uri?, state: Int) -> Unit)? = null): PhotoRequest {
        this.fromSelected = true
        this.selectCallback = callback
        return this
    }

    /**
     * 开始
     * @param callback 回调
     * */
    fun build(callback: ((uri: Uri?, state: Int) -> Unit)) {
        this.buildCallback = { uri: Uri?, state: Int ->
            callback(uri, state)
            finish()
        }
        check(!(fromCamera && fromSelected)) {
            "You have called `camera()` and `select()` at the same time!"
        }
        check(fromCamera || fromSelected) {
            "Please call `camera()` or `select()`!"
        }
        activity.supportFragmentManager.commit {
            add(this@PhotoRequest, "photo request")
        }
    }

    /**
     * ActivityResult 只能在 onCreate 之前创建
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 相片拍摄
        if (fromCamera) {
            cameraLauncher.launch(null)
        }
        // 图片选择
        if (fromSelected) {
            pickLauncher.launch(openAlbum())
        }
    }

    private fun finish() {
        activity.supportFragmentManager.commit {
            remove(this@PhotoRequest)
        }
//            tmpUri?.toFile()?.delete()
    }


    /**
     * 获取裁剪的 intent
     * @param context
     * @param uri 需要裁剪图片对应的 uri
     * @return intent
     * */
    private fun cropIntent(context: Context, uri: Uri): Intent {
        val contentUri = Uri.fromFile(FileManager.createTempFile(context, "crop", "jpeg"))
        tmpUri = contentUri // 记录缓存文件，结束时删除
        val intent = Intent("com.android.camera.action.CROP")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0需要临时添加读取Url的权限
            // 添加此属性是为了解决：调用裁剪框时候提示：图片无法加载或者加载图片失败或者无法加载此图片
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.setDataAndType(uri, "image/*")
        // 发送裁剪信号，去掉也能进行裁剪
        intent.putExtra("crop", true)

        if (cropOption?.needAspect == true) {
            // 硬件厂商为华为的，默认是圆形裁剪框，这里让它无法成圆形
            if (Build.MANUFACTURER.contains("HUAWEI")) {
                intent.putExtra("aspectX", 9999)
                intent.putExtra("aspectY", 9998)
            } else {
                // 上述两个属性控制裁剪框的缩放比例。
                // 当用户用手拉伸裁剪框时候，裁剪框会按照上述比例缩放。
                intent.putExtra("aspectX", cropOption?.aspectX ?: 1)
                intent.putExtra("aspectY", cropOption?.aspectY ?: 1)
            }
        }

        // 设置裁剪区域的形状，默认为矩形，也可设置为圆形，可能无效
        intent.putExtra("circleCrop", cropOption?.isCircleCrop ?: false)
        // 设置能否缩放
        intent.putExtra("scale", cropOption?.isScale ?: false)
        // 去黑边
        intent.putExtra("scaleUpIfNeeded", true)
        // 属性控制裁剪完毕，保存的图片的大小格式。
        intent.putExtra("outputX", cropOption?.outputX ?: 400)
        intent.putExtra("outputY", cropOption?.outputY ?: 400)
        // 输出裁剪文件的格式
        intent.putExtra("outputFormat", cropOption?.outputFormat ?: Bitmap.CompressFormat.JPEG.toString())
        // 是否返回裁剪后图片的 Bitmap
        intent.putExtra("return-data", false) // 不返回数据，而只是保存在
        // 设置输出路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
        return intent
    }

    /**
     * 跳转到相册
     * */
    private fun openAlbum(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        // 系统文件访问框架的写法
        //    val intent = Intent()
        //    intent.type = "image/*"
        //    intent.action = "android.intent.action.GET_CONTENT"
        //    intent.addCategory("android.intent.category.OPENABLE")
        return intent
    }

    /**
     * 裁剪选项
     * */
    class CropOptions {
        internal var needAspect = false
        internal var aspectX = 1
        internal var aspectY = 1
        internal var isCircleCrop = false
        internal var isScale = false
        internal var outputX = 400
        internal var outputY = 400
        internal var outputFormat = Bitmap.CompressFormat.JPEG.toString()

        /**
         * 裁剪比例
         * @param x x轴的比例
         * @param y y轴的缩比例
         * */
        fun aspect(x: Int, y: Int): CropOptions {
            this.needAspect = true
            this.aspectX = x
            this.aspectY = y
            return this
        }

        /**
         * 开启圆形裁剪，可能无效
         * */
        fun circleCrop(): CropOptions {
            this.isCircleCrop = true
            return this
        }

        /**
         * 支持图片缩放
         * */
        fun scale(): CropOptions {
            this.isScale = true
            return this
        }

        /**
         * 输出尺寸，像素
         * */
        fun ouput(x: Int, y: Int): CropOptions {
            this.outputX = x
            this.outputY = y
            return this
        }

        /**
         * 输出格式
         * */
        fun format(format: Bitmap.CompressFormat): CropOptions {
            this.outputFormat = format.toString()
            return this
        }
    }

    // 回调的状态
    object State {
        // 成功
        const val SUCCESS = 0

        // 保存失败
        const val ERROR_SAVE = 1

        // 裁剪失败
        const val ERROR_CROP = 2

        // 选择失败
        const val ERROR_SELECT = 3

        // 拍照失败
        const val ERROR_TAKE = 4

        // 取消选择
        const val CANCEL_SELECT = 5

        // 取消拍照
        const val CANCEL_TAKE = 6

        const val CANCEL_CROP = 7
    }
}
