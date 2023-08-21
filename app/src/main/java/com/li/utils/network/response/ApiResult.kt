package com.li.utils.network.response

/**
 * 请求结果密封类
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
sealed class ApiResult <out T> {

    val isSuccess get() =  this is Success

    val isError get() = this is Error

    val isRetry get() = this is Retry

    val isLoading get() = this is Loading

    /** 请求成功时 */
    data class Success<out R>(val value: R) : ApiResult<R>()

    /** 请求发生异常时 */
    data class Error(val throwable: Throwable) : ApiResult<Nothing>()

    /** 请求重试时 */
    data class Retry(val attempt: Int): ApiResult<Int>()

    /** 请求进行时 */
    object Loading: ApiResult<Nothing>()

    /**
     * 请求 **成功** 时的回调
     * @param event 对该请求结果的处理
     * @return 该对象，链式调用
     * ```
     * result.onSuccess { value ->
     *     // handle with the value
     * }
     * ```
     * */
    inline fun onSuccess(crossinline event: (value: T) -> Unit): ApiResult<T> {
        if (this is Success) {
            // 此时已经自动转为 Success 类型
            event.invoke(this.value)
        }
        return this
    }


    /**
     * 请求 **失败** 时的回调
     * @param event 对该请求结果的处理
     * @return 该对象，链式调用
     * ```
     * result.onError { throwable ->
     *     // handle with the throwable
     * }
     * ```
     * */
    inline fun onError(event: (e: Throwable) -> Unit): ApiResult<T> {
        if (this is Error) {
            // 此时已经自动转为 Error 类型
            event.invoke(this.throwable)
        }
        return this
    }

    /**
     * 请求 **重试** 时的回调
     * @param event 对该重试的处理
     * ```
     * result.onLoading {
     *     // handle with the loading
     * }
     * ```
     * */
    inline fun onRetry(event: (attempt: Int) -> Unit): ApiResult<T> {
        if (this is Retry) {
            // 此时已经自动转为 Retry 类型
            event.invoke(this.attempt)
        }
        return this
    }

    /**
     * 请求 **加载** 时的回调
     * @param event 对加载的处理
     * ```
     * result.onLoading {
     *     // handle with the loading
     * }
     * ```
     * */
    inline fun onLoading(event: () -> Unit): ApiResult<T> {
        if (this is Loading) {
            // 此时已经自动转为 Loading 类型
            event.invoke()
        }
        return this
    }
}




