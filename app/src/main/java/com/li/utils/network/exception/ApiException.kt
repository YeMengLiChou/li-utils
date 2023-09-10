package com.li.utils.network.exception


/**
 *
 * 接口结果异常类 服务器非200状态，对应的异常
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
open class ApiException(
    val code: Int,
    val msg: String,
    e: Throwable? = null
) : Exception(msg, e) {
    constructor(error: ERROR, e: Throwable? = null) : this(error.code, error.msg, e)
}