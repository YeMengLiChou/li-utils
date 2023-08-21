package com.li.utils.network.exception

import java.io.IOException

/**
 * 无网络连接异常
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
class NoNetworkException(
    val code: Int,
    val msg: String,
    e: Throwable? = null
): IOException(e) {
    companion object {
        private val TAG = NoNetworkException::class.simpleName
    }

    constructor(error: ERROR, e: Throwable? = null) : this(error.code, error.msg, e)
}