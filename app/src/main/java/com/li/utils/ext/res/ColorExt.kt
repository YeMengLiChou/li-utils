package com.li.utils.ext.res

import android.graphics.Color
import androidx.annotation.FloatRange

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */

/**
 * 根据当前的颜色设置透明度
 * @param color 颜色
 * @param fraction 透明度 (0 ~ 1)
 * @return 改变透明度后的颜色
 * */
fun Color.alpha(
    color: Int,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float
): Int {
    val red = Color.red(color)
    val blue = Color.blue(color)
    val green = Color.green(color)
    val alpha = (Color.alpha(color) * fraction.toInt())
    return Color.argb(alpha, red, green, blue)
}
