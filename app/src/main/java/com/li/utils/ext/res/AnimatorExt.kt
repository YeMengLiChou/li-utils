package com.li.utils.ext.res

import android.animation.ValueAnimator

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */

// =========================== Value Animator =========================

/**
 * Float 类型的 [addUpdateIntListener]，带有偏移量
 * @param update value 当前值， fraction 当前百分比, offset 偏移量
 * */
inline fun ValueAnimator.addUpdateFloatListener(
    crossinline update: (value: Float, fraction: Float, offset: Float) -> Unit
) {
    var pre = 0f
    addUpdateListener {
        (it.animatedValue as Float).let { value ->
            update(value, it.animatedFraction, value - pre)
            pre = value
        }
    }
}

/**
 * Int 类型的 [addUpdateIntListener]，带有偏移量
 * @param update value 当前值， fraction 当前百分比, offset 偏移量
 * */
inline fun ValueAnimator.addUpdateIntListener(crossinline update: (value: Int, fraction: Float, offset: Int) -> Unit) {
    var pre = 0
    addUpdateListener {
        (it.animatedValue as Int).let { value ->
            update(value, it.animatedFraction, value - pre)
            pre = value
        }
    }
}