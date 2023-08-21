package com.li.utils.ext.common


/**
 * 默认的 lazy 是线程同步型，如果不需要在多线程情况下使用，那么需要避免锁带来的性能消耗
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/09
 */


/**
 * 懒加载，不做任何线程安全操作 [LazyThreadSafetyMode.NONE]
 * */
inline fun <T> lazyNone(crossinline initializer: () -> T): Lazy<T> = lazy(mode = LazyThreadSafetyMode.NONE) { initializer() }

/**
 * 懒加载，允许多线程同时执行，第一个初始的值作为最终值 [LazyThreadSafetyMode.PUBLICATION]
 * */
inline fun <T> lazyPublication(crossinline initializer: () -> T): Lazy<T> = lazy(mode = LazyThreadSafetyMode.PUBLICATION) { initializer() }
