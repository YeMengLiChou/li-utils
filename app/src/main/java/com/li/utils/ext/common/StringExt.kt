package com.li.utils.ext.common

import android.os.Build
import android.text.Html
import android.text.Spanned

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */


/**
 * 转化为 html 标记
 * */
fun String.fromHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}