package com.li.utils.network.exception


import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 网络请求的同意处理
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
object ExceptionHandler {

    /**
     * 拦截器
     * */
    interface ExceptionInterceptor {
        /**
         * 拦截
         * @param e
         * @return 如果处理了返回 [ApiException]，否则返回 null
         * */
        fun intercept(e: Throwable): ApiException?
    }

    private var interceptor: ExceptionInterceptor? = null

    /**
     * 设置拦截器，
     * */
    fun setInterceptor(interceptor: ExceptionInterceptor) {
        ExceptionHandler.interceptor = interceptor
    }

    fun handle(e: Throwable): ApiException {
        val intercepted = interceptor?.intercept(e)
        if (intercepted != null) return intercepted
        val exception: ApiException =
        when(e) {
            is ApiException -> {
                e
            }
            is NoNetworkException -> {
                ApiException(ERROR.NETWORK_ERROR, e)
            }
            is HttpException -> {
                when(e.code()) {
                    400 -> ApiException(ERROR.BAD_REQUEST, e)
                    401 -> ApiException(ERROR.UNAUTHORIZED, e)
                    403 -> ApiException(ERROR.FORBIDDEN, e)
                    404 -> ApiException(ERROR.NOT_FOUND, e)
                    500 -> ApiException(ERROR.SERVER_ERROR, e)
                    501 -> ApiException(ERROR.NOT_IMPLEMENTED, e)
                    502 -> ApiException(ERROR.SERVICE_UNAVAILABLE, e)
                    else -> ApiException(e.code(), e.message(), e)
                }
            }
            is JSONException,
            is JsonDataException,
            is JsonEncodingException -> ApiException(ERROR.PARSE_ERROR, e)
            is ConnectException -> ApiException(ERROR.NETWORK_ERROR, e)
            is SSLException -> ApiException(ERROR.SSL_ERROR, e)
            is SocketException,
            is SocketTimeoutException -> ApiException(ERROR.TIMEOUT_ERROR, e)
            is UnknownHostException -> ApiException(ERROR.UNKNOWN_HOST, e)
            else -> {
                e.message
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { ApiException(1000, it, e) }
                    ?: ApiException(ERROR.UNKNOWN, e)
            }
        }
        return exception
    }
}