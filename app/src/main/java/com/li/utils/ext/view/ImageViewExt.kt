package com.li.utils.ext.view

import android.widget.ImageView

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */

/**
 * ImageView 位移其 src
 * @param dx
 * @param dy
 * */
fun ImageView.translateSrc(dx: Float, dy: Float): ImageView {
    this.imageMatrix.setTranslate(dx, dy)
    return this
}