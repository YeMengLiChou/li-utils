package com.li.utils.framework.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.hardware.input.InputManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.abs

/**
 *
 * SystemBar 的工具类，获取其高度、隐藏、显示
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/11
 */
@SuppressLint("ObsoleteSdkInt")
object SystemBarUtils {
    private const val TAG = "SystemBarUtils"
    // 未初始化的标识
    private const val NOT_INITIALIZED = -1

    // 是否已经初始化
    @Volatile
    private var initialized = false

    private lateinit var application: Application

    // =============== 设备信息 ===================

    // 状态栏可见高度
    private var mStatusBarHeight: Int = NOT_INITIALIZED

    // 状态栏高度（即使设置为不可见）
    private var mStatusBarIgnoreHeight: Int = NOT_INITIALIZED

    // 导航栏可见高度
    private var mNavigationBarHeight: Int = NOT_INITIALIZED

    // 导航栏高度（即使设置为不可见）
    private var mNavigationBarIgnoreHeight: Int = NOT_INITIALIZED


    fun init(application: Application) {
        SystemBarUtils.application = application
        initialized = true
        Log.i(TAG, "$TAG init")
    }


    // 检查初始化
    private fun checkInitialized() {
        check(initialized) {
            "You need initialize SystemBarUtils first!"
        }
    }

    @RequiresApi(21)
    private fun Activity.getInsetsCompat(typeMask: Int) = ViewCompat.getRootWindowInsets(window.decorView)?.getInsets(typeMask)

    @RequiresApi(21)
    private fun Activity.getInsetsIgnoreCompat(typeMask: Int) = ViewCompat.getRootWindowInsets(window.decorView)?.getInsets(typeMask)

    interface HeightValueCallback {
        fun height(height: Int)
    }

    // =======================  StatusBar ↓ =========================
    /**
     * 设置沉浸式状态，透明状态栏
     */
    fun immersiveStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4 (19)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        setStatusBar(activity, false, Color.TRANSPARENT, true)
    }

    /**
     * 设置状态栏
     * @param activity
     * @param darkContent 黑字
     * @param backgroundColor 状态栏背景色
     * @param immersive 沉浸效果,页面布局延伸到状态栏里面
     * */
    @RequiresApi(21)
    fun setStatusBar(
        activity: Activity,
        darkContent: Boolean,
        @ColorInt backgroundColor: Int = Color.WHITE,
        immersive: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 11 (30)
            activity.window.apply {
                // 设置沉浸式
                if (immersive) WindowCompat.setDecorFitsSystemWindows(this, false)
                // 设置前景色（文字颜色）
                WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightStatusBars = !darkContent
                // 设置背景颜色
                statusBarColor = backgroundColor
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5 (21)
            // 为了兼容不同的平台，不同的系统版本，让 statusBarColor都能生效，需要为 window 增加一个flag
            // FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS 请求系统绘制状态栏背景色，但是不能与 FLAG_TRANSLUCENT_STATUS 同时出现
            activity.window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = backgroundColor
            }
            // 设置浅色能力，需要6.0以上的能力
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6 (23)
                activity.window.decorView.apply {
                    visibility =
                        if (darkContent) // 白底黑字
                        // SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 能使状态栏字体颜色变黑色，背景变白色
                        visibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                        else
                        // 深色主题，黑体白字 需要除去SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            visibility.and(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
                }
            }
            // 沉浸式设置
            if (immersive) {
                activity.window.decorView.apply {
                    visibility = visibility
                        // View.SYSTEM_UI_FLAG_FULLSCREEN：能使页面延伸到状态栏里面，但是状态栏的图标(信号，时间)也看不见了，即全屏模式
                        .or(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
                        // SYSTEM_UI_FLAG_LAYOUT_STABLE 恢复状态栏图标
                        .or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                }
            }
        }
    }


    /**
     * 获取状态栏高度
     * @param activity
     * @param isIgnoreVisibility 是否忽略可见性，状态栏不可见时设置为false会返回0
     * */
    fun getStatusBarHeight(
        activity: Activity,
        isIgnoreVisibility: Boolean = false
    ): Int {
        checkInitialized()
        if (isIgnoreVisibility) {
            if (mStatusBarIgnoreHeight == NOT_INITIALIZED) {
                mStatusBarIgnoreHeight =
                    if (Build.VERSION.SDK_INT >= 21) {
                        getStatusBarHeightInsets(activity, true)
                            .takeIf { it != 0 }
                            ?: getStatusBarHeightReflect()
                    } else {
                        getStatusBarHeightReflect()
                    }
            }
            return mStatusBarIgnoreHeight
        } else {
            if (mStatusBarHeight == NOT_INITIALIZED) {
                mStatusBarHeight =
                    if (Build.VERSION.SDK_INT >= 21) {
                        getStatusBarHeightInsets(activity, false)
                    } else {
                        getStatusBarHeightReflect()
                    }
            }
            return mStatusBarHeight
        }
    }

    /**
     * 判断状态栏是否可见
     * */
    @RequiresApi(21)
    fun checkStatusBarVisible(activity: Activity): Boolean {
        return ViewCompat.getRootWindowInsets(activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.statusBars()) ?: true
    }

    /**
     * 设置状态栏可见性
     * @param activity
     * @param visible
     * */
    @RequiresApi(21)
    fun setStatusBarVisibility(activity: Activity, visible: Boolean) {
        if (visible) {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).show(WindowInsetsCompat.Type.statusBars())
        } else {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    /**
     * 回调方式获取状态栏高度
     */
    fun getStatusBarHeight(activity: Activity, callback: HeightValueCallback) {
        getStatusBarHeight(activity.window.decorView, callback)
    }


    /**
     * 回调方式获取状态栏高度
     */
    fun getStatusBarHeight(view: View, callback: HeightValueCallback) {
        if (view.isAttachedToWindow) {
            val windowInsets: WindowInsetsCompat = ViewCompat.getRootWindowInsets(view)!!
            val height: Int = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            callback.height(if (height > 0) height else getStatusBarHeightReflect())
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val windowInsets: WindowInsetsCompat = ViewCompat.getRootWindowInsets(v)!!
                    val height: Int = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                    callback.height(if (height > 0) height else getStatusBarHeightReflect())
                }
                override fun onViewDetachedFromWindow(v: View) { }
            })
        }
    }

    /**
     * 使用 WindowInsets 获取状态栏高度
     * */
    private fun getStatusBarHeightInsets(activity: Activity, ignoreVisibility: Boolean): Int {
        if (ignoreVisibility) {
            return activity.getInsetsIgnoreCompat(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        }
        return activity.getInsetsCompat(WindowInsetsCompat.Type.statusBars())?.top ?: 0
    }

    /**
     * 使用反射获取导航栏高度
     * */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeightReflect(): Int {
        val resourceId =
            application.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId != 0) return application.resources.getDimensionPixelSize(resourceId)
        else return 0
    }

    // ===================== NavigationBar ==================================


    /**
     * 底部导航栏的沉浸式
     */
    fun immersiveNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 4.4 (19)
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        setNavigationBar(activity, false, Color.TRANSPARENT, true)
    }

    /**
     * 设置导航栏
     * @param activity
     * @param darkContent 黑字
     * @param backgroundColor 导航栏背景色
     * @param immersive 沉浸效果,页面布局延伸到导航栏里面
     * */
    @RequiresApi(21)
    fun setNavigationBar (
        activity: Activity,
        darkContent: Boolean,
        @ColorInt backgroundColor: Int = Color.WHITE,
        immersive: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 11 (30)
            activity.window.apply {
                // 设置沉浸式
                if (immersive) WindowCompat.setDecorFitsSystemWindows(this, false)
                // 设置前景色（文字颜色）
                WindowCompat.getInsetsController(activity.window, activity.window.decorView).isAppearanceLightNavigationBars = !darkContent
                // 设置背景颜色
                navigationBarColor = backgroundColor
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                navigationBarColor = backgroundColor
            }
            // 设置浅色能力，需要6.0以上的能力
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6 (23)
                activity.window.decorView.apply {
                    visibility =
                        if (darkContent) // 白底黑字
                        // SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 能使状态栏字体颜色变黑色，背景变白色
                            visibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                        else
                        // 深色主题，黑体白字 需要除去SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            visibility.and(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv())
                }
            }
            // 沉浸式设置
            if (immersive) {
                activity.window.decorView.apply {
                    visibility = visibility
                        .or(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                        // SYSTEM_UI_FLAG_LAYOUT_STABLE 恢复状态栏图标
                        .or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                }
            }

        }
    }


    /**
     * 获取导航栏高度
     * @param activity
     * @param isIgnoreVisibility 是否忽略可见性，状态栏不可见时设置为false会返回0
     * */
    fun getNavigationBarHeight(
        activity: Activity,
        isIgnoreVisibility: Boolean = false
    ): Int {
        checkInitialized()
        if (isIgnoreVisibility) {
            if (mNavigationBarIgnoreHeight == NOT_INITIALIZED) {
                mNavigationBarIgnoreHeight =
                    if (Build.VERSION.SDK_INT >= 21) {
                        getNavigationBarHeightInsets(activity, true)
                            .takeIf { it != 0 }
                            ?: getStatusBarHeightReflect()
                    } else {
                        getNavigationBarHeightReflect()
                    }
            }
            return mNavigationBarIgnoreHeight
        } else {
            if (mNavigationBarHeight == NOT_INITIALIZED) {
                mNavigationBarHeight =
                    if (Build.VERSION.SDK_INT >= 21) {
                        getNavigationBarHeightInsets(activity, false)
                    } else {
                        getNavigationBarHeightReflect()

                    }
            }
            return mNavigationBarHeight
        }
    }

    /**
     * 获取底部导航栏的高度
     */
    fun getNavigationBarHeight(activity: Activity, callback: HeightValueCallback) {
        getNavigationBarHeight(activity.window.decorView, callback)
    }

    /**
     * 回调获取底部导航栏的高度
     */
    fun getNavigationBarHeight(view: View, callback: HeightValueCallback) {
        if (view.isAttachedToWindow) {
            val windowInsets: WindowInsetsCompat = ViewCompat.getRootWindowInsets(view)!!
            val height: Int = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).top
            callback.height(if (height > 0) height else getNavigationBarHeightReflect())

        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    val windowInsets: WindowInsetsCompat = ViewCompat.getRootWindowInsets(v)!!
                    val height: Int = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).top
                    callback.height(if (height > 0) height else getNavigationBarHeightReflect())
                }
                override fun onViewDetachedFromWindow(v: View) { }
            })
        }
    }



    /**
     * 判断导航栏是否可见
     * */
    @RequiresApi(21)
    fun checkNavigationBarVisible(activity: Activity): Boolean {
        return ViewCompat.getRootWindowInsets(activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.navigationBars()) ?: true
    }

    /**
     * 设置导航栏可见性
     * @param activity
     * @param visible
     * */
    @RequiresApi(21)
    fun setNavigationBarVisibility(activity: Activity, visible: Boolean) {
        if (visible) {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        } else {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                // 能够通过滑动它出来
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }


    /**
     * 使用 WindowInsets 获取导航栏栏高度
     * */
    private fun getNavigationBarHeightInsets(activity: Activity, ignoreVisibility: Boolean): Int {
        if (ignoreVisibility) {
            return activity.getInsetsIgnoreCompat(WindowInsetsCompat.Type.navigationBars())?.top ?: 0
        }
        return activity.getInsetsCompat(WindowInsetsCompat.Type.navigationBars())?.top ?: 0
    }

    /**
     * 使用反射获取导航栏高度
     * */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getNavigationBarHeightReflect(): Int {
        runCatching {
            val res = application.resources
                res.getIdentifier("navigation_bar_height", "dimen", "android")
                .takeIf { it > 0 }
                ?.let { return it }
                ?: return 0
        }.onFailure { it.printStackTrace() }
        return 0
    }

    // ========================= ime =======================================

    /**
     * 获取软键盘高度，需要显示状态下才能获取
     * */
    fun getImeHeight(activity: Activity): Int {
        return activity.getInsetsCompat(WindowInsetsCompat.Type.ime())?.top?.let { abs(it) } ?: 0
    }

    /**
     * 判断软键盘是否可见
     * */
    @RequiresApi(21)
    fun checkImeVisible(view: View): Boolean {
        return ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
    }

    /**
     * 设置软键盘可见性
     * @param activity
     * @param visible
     * */
    @RequiresApi(21)
    fun setImeVisibility(activity: Activity, visible: Boolean) {
        if (visible) {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView.rootView).show(WindowInsetsCompat.Type.ime())
        } else {
            WindowCompat.getInsetsController(activity.window, activity.window.decorView.rootView).hide(WindowInsetsCompat.Type.ime())
        }
    }

    /**
     * 设置软键盘可见性
     * @param activity
     * @param visible
     * */
    fun showIme(activity: Activity, view: View) {
        view.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        } else {
            WindowInsetsControllerCompat(activity.window, view).show(WindowInsetsCompat.Type.ime())
        }
    }
    /**
     * 设置软键盘可见性
     * @param activity
     * @param visible
     * */
    fun hideIme(activity: Activity, view: View) {
//        view.clearFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.windowInsetsController?.hide(WindowInsetsCompat.Type.ime())
        } else {
            WindowInsetsControllerCompat(activity.window, view).hide(WindowInsetsCompat.Type.ime())
        }
    }

    // ======================== other ===============================

    fun setFullScreen(activity: Activity) {

    }
}