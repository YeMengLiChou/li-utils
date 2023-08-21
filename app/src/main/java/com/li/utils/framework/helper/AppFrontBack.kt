package com.li.utils.framework.helper

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * App 前后台的管理
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */
object AppFrontBack {

    private var startedActivityCount = 0

    private var mCaches = HashMap<String, Application.ActivityLifecycleCallbacks>()

    /**
     * 注册监听
     * @param application
     * @param listener 监听回调
     * @param key 用于 取消注册的 key
     * */
    fun register(
        application: Application,
        listener: AppFrontBackListener,
        key: String
    ) {
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                if (startedActivityCount == 1) listener.onFront()
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount--
                if (startedActivityCount == 0) listener.onBack()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        }

        application.registerActivityLifecycleCallbacks(callback)
        mCaches[key] = callback
    }

    /**
     * 取消注册
     * @param application
     * @param key 回调对应的key
     * */
    fun unregister(
        application: Application,
        key: String
    ) {
        mCaches[key]?.let { application.unregisterActivityLifecycleCallbacks(it) }
    }

    /**
     * App 前后台切换的监听接口
     * */
    interface AppFrontBackListener {
        /**
         * App 第一次进入前台时调用
         * */
        fun onFront()

        /**
         * App 进入后台时调用
         * */
        fun onBack()
    }

}