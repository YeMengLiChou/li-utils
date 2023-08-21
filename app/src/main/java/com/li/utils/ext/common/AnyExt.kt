package com.li.utils.ext.common

/**
 *
 * Any 通用扩展
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */


/**
 * 直接转
 * */
inline fun <reified T> Any.asOrNull(): T? {
    return this as? T
}


inline fun <reified T> Any.`as`(): T {
    return this as T
}