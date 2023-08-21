package com.li.utils.framework.manager

import android.app.Application
import android.widget.Toast
import kotlin.math.abs
import kotlin.system.exitProcess

/**
 *
 * App 全局管理
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
object AppManager {

    private const val TAG = "AppManager"

    var isDebug = false

    lateinit var application: Application
        private set

    /**
     * 上一次返回的时间
     * */
    private var lastPressedTime = -1L

    /**
     * 按下返回事件的间隔时间
     * */
    private var pressTimeInterval = 1000L


    @JvmStatic
    fun init(application: Application) {
        AppManager.application = application
    }

    fun init(application: Application, isDebug: Boolean) {
        AppManager.application = application
        AppManager.isDebug = isDebug
    }

    /**
     * 设置时间间隔
     * */
    fun setPressTimeInterval(interval: Long) {
        pressTimeInterval = interval
    }

    /**
     * 需要按下两次返回的时间间隔小于指定间隔，才能退出
     * */
    fun exit(time: Long) {
        if (abs(time - lastPressedTime) >= pressTimeInterval) {
            lastPressedTime = time
            Toast.makeText(application, "再返回一次退出", Toast.LENGTH_SHORT).show()
        } else {
            exitProcess(0)
        }
    }



}