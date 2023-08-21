package com.li.utils.network.interceptor

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 用于添加请求头
 *
 * @author Gleamrise
 * <br/>Created: 2023/07/17
 */
class AppendHeadersInterceptor(
    val headers: Headers
): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newBuilder = request.newBuilder()

        // 添加请求头
        headers.forEach {
            newBuilder.addHeader(it.first, it.second)
        }

        return chain.proceed(newBuilder.build())
    }
}