package com.li.utils.ext.res

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.drawable.DrawableCompat

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */


/**
 * 改变 Drawable 的颜色为 color
 * （适合单色图标变色）
 * @param color 颜色
 * */
fun Drawable.tint(@ColorInt color: Int): Drawable {
    val wrappedDrawable = DrawableCompat.wrap(this)
    DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color))
    return wrappedDrawable
}

/**
 *  将 Drawable 和指定颜色进行混合，如果设置了 [tint][android.graphics.drawable.Drawable.tint]，会让tint失效
 * - 配合灰色可以使图片变暗
 * @param color 颜色
 * */
fun Drawable.filter(
    @ColorInt color: Int
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            color,
            BlendModeCompat.MULTIPLY
        )
    } else {
        this.setColorFilter(
            color,
            PorterDuff.Mode.MULTIPLY
        )
    }
}

/**
 *  将 Drawable 和指定颜色进行混合，如果设置了 [tint][android.graphics.drawable.Drawable.tint]，会让tint失效
 * - 配合灰色可以使图片变暗
 *
 * @param colorString 颜色字符串 #xxxxxx
 * */
fun Drawable.filter(
    colorString: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            Color.parseColor(colorString),
            BlendModeCompat.MULTIPLY
        )
    } else {
        this.setColorFilter(
            Color.parseColor(colorString),
            PorterDuff.Mode.MULTIPLY
        )
    }
}
