package com.li.utils.network.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 *
 * 网络工具
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/02
 */
object NetWorkUtils {
    // 网络可用
    const val PING_STATUS_AVAILABLE = 0

    // 需要认证
    const val PING_STATUS_AUTHENTICATE = 1

    // 网络不可用
    const val PING_STATUS_UNAVAILABLE = 2

    // 检查时出错
    const val PING_STATUS_UNKNOWN = 3

    /** 缓存回调 */
    private val mCachedCallback = HashMap<String, ConnectivityManager.NetworkCallback>()

    /**
     * 判断是否可以联网
     * */
    @SuppressLint("ObsoleteSdkInt")
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= 29) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // 是否能连接互联网
                        && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) // 是否能连接到互联网
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) // wifi
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) // vpn
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) // 移动网络
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) // 以太网
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true // 蓝牙
                else -> false
            }
        } else {
            // deprecated on api 29
            return connectivityManager.activeNetworkInfo?.isConnected ?: return false
        }
    }

    /**
     * 用 ping 来检查是否真正连通，需要放在子线程中运行
     * @return
     * - [PING_STATUS_AVAILABLE] 连通
     * - [PING_STATUS_AUTHENTICATE] 需要认证
     * - [PING_STATUS_UNAVAILABLE] 不连通
     * - [PING_STATUS_UNKNOWN] 出现异常
     * */
    suspend fun isNetworkPingAvailable(): Int {
        val runtime = Runtime.getRuntime()
        try {
            val exitValue = withContext(Dispatchers.IO) {
                runtime.exec("ping -c 3 www.baidu.com").waitFor()
            }
            return exitValue
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return PING_STATUS_UNKNOWN
    }

    /**
     * 判断是否为 wifi
     * */
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= 29) {
            val currentNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
            return caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        } else {
            return connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_WIFI
        }
    }

    /**
     * 判断是否为 移动数据
     * */
    fun isMobile(context: Context): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        if (Build.VERSION.SDK_INT >= 29) {
            val currentNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(currentNetwork)
            return caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        } else {
            return connectivityManager.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
        }
    }

    /**
     * 打开网络设置界面
     */
    fun openNetworkSetting(activity: Activity) {
        val intent =  Intent("/");
        val cm =  ComponentName("com.android.settings",
            "com.android.settings.WirelessSettings");
        intent.component = cm;
        intent.action = "android.intent.action.VIEW";
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 注册网络状态监听,
     * 在 [onResume][android.app.Activity.onResume] 调用
     * @param context
     * @param key 用于取消注册的键值，需与 [unRegisterNetworkStatusListener] 中的 key 保持一致
     * @param callback 网络状态改变时回调
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun registerNetworkStatusListener(
        context: Context, key: String,
        callback: (network: Network, networkCapabilities: NetworkCapabilities) -> Unit
    ) {
        val networkService = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                callback(network, networkCapabilities)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkService.registerDefaultNetworkCallback(networkCallback)
        } else {
            networkService.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
        mCachedCallback[key] = networkCallback
    }

    /**
     * 取消注册网络状态监听, 在 [onPause][android.app.Activity.onPause] 或 [onDestroy][android.app.Activity.onDestroy] 取消
     * @param context
     * @param key 用于取消注册的键值，与 [registerNetworkStatusListener] 中 key 相同
     * */
    private fun unRegisterNetworkStatusListener(context: Context, key: String) {
        val networkService = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        mCachedCallback[key]?.let { networkService.unregisterNetworkCallback(it) }
    }





}


