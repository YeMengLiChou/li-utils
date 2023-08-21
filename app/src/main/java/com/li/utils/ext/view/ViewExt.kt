package com.li.utils.ext.view

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import androidx.annotation.GravityInt
import androidx.annotation.Px
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import com.li.utils.R

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */

// ====================== View 属性 ===========================

/**
 * 设置 View 宽度
 * @param width 要设置宽度
 */
fun View.width(@Px width: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.width = width
    layoutParams = params
    return this
}

/**
 * 设置 View 高度
 * @param height 要设置的高度
 */
fun View.height(@Px height: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.height = height
    layoutParams = params
    return this
}


/**
 * 设置 View 高度，限制在 min 和 max 范围之内
 * @param height 目标高度 px
 * @param minHeight 最小高度
 * @param maxHeight 最大高度
 */
fun View.heightLimited(
    height: Int,
    minHeight: Int,
    maxHeight: Int
): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    when {
        height < minHeight -> params.height = minHeight
        height > maxHeight -> params.height = maxHeight
        else -> params.height = height
    }
    layoutParams = params
    return this
}

/**
 * 设置 View 宽度，限制在 min 和 max 范围之内
 * @param width 目标宽度 px
 * @param minWidth 最小宽度
 * @param maxWidth 最大宽度
 */
fun View.widthLimited(width: Int, minWidth: Int, maxWidth: Int): View {
    val params = layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    when {
        width < minWidth -> params.width = minWidth
        width > maxWidth -> params.width = maxWidth
        else -> params.width = width
    }
    layoutParams = params
    return this
}

/**
 * 包含了 margin 的宽度
 * */
inline val View.marginWidth: Int get() = this.width + marginLeft + marginRight

/**
 * 包含了 margin 的高度
 * */
inline val View.marginHeight: Int get() = height + marginTop + marginBottom

/**
 * 设置 View 的 margin 单位px
 * @param left 默认保留原来的
 * @param top 默认是保留原来的
 * @param right 默认是保留原来的
 * @param bottom 默认是保留原来的
 */
fun View.margin(
    left: Int = Int.MAX_VALUE,
    top: Int = Int.MAX_VALUE,
    right: Int = Int.MAX_VALUE,
    bottom: Int = Int.MAX_VALUE
): View {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    if (left != Int.MAX_VALUE) params.leftMargin = left
    if (top != Int.MAX_VALUE) params.topMargin = top
    if (right != Int.MAX_VALUE) params.rightMargin = right
    if (bottom != Int.MAX_VALUE) params.bottomMargin = bottom
    layoutParams = params
    return this
}

/**
 * 相对于 Window 的位置 (x, y)
 * */
inline val View.locationInWindow: Pair<Int, Int>
    get() {
        val location = IntArray(2)
        this.getLocationInWindow(location)
        return Pair(location[0], location[1])
    }

/**
 * 相对于手机屏幕的位置 (x, y)
 * */
inline val View.locationOnScreen: Pair<Int, Int>
    get() {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return Pair(location[0], location[1])
    }

/**
 * 设置可见性为 [View.GONE]
 * */
fun View.gone() {
    visibility = View.GONE
}

/**
 * 设置可见性为 [View.VISIBLE]
 * */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * 设置可见性为 [View.INVISIBLE]
 * */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * 判断可见性为 [View.GONE]
 * */
inline val View.isGone: Boolean get() = visibility == View.GONE

/**
 * 判断可见性为 [View.VISIBLE]
 * */
inline val View.isVisible: Boolean get() = visibility == View.VISIBLE

/**
 * 判断可见性为 [View.INVISIBLE]
 * */
inline val View.isInvisible: Boolean get() = visibility == View.INVISIBLE

/**
 * 切换可见性 [[View.GONE], [View.VISIBLE]]
 * */
fun View.toggleVisibility() {
    visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
}

/**
 * Layout 的 Gravity
 * */
inline val View.layoutGravity: Int
    get() = when (val lp = layoutParams) {
        is FrameLayout.LayoutParams -> lp.gravity
        is LinearLayout.LayoutParams -> lp.gravity
        else -> Gravity.NO_GRAVITY
    }

/**
 * 设置 Gravity
 * */
fun View.layoutGravity(@GravityInt gravity: Int): View {
    when (val lp = layoutParams) {
        is FrameLayout.LayoutParams -> lp.gravity = gravity
        is LinearLayout.LayoutParams -> lp.gravity = gravity
        else -> return this
    }
    invalidate()
    return this
}

/**
 *  所有子 View
 *  */
inline val ViewGroup.children
    get() = (0 until childCount).map { getChildAt(it) }



/**
 * 设置View圆角
 */
fun View.setClipViewCornerRadius(radius: Int) {
    if (radius > 0) {
        this.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height ?: 0, radius.toFloat())
            }
        }
        this.clipToOutline = true
    } else {
        this.clipToOutline = false
    }
}

/**
 * 设置View顶部圆角
 */
fun View.setClipViewCornerTopRadius(radius: Int) {
    if (radius > 0) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, 0, view?.width ?: 0, view?.height?.plus(radius) ?: 0, radius.toFloat())
            }
        }
        clipToOutline = true
    } else {
        clipToOutline = false
    }
}

/**
 * 设置View底部圆角
 */
fun View.setClipViewCornerBottomRadius(radius: Int) {
    if (radius > 0) {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, -radius, view?.width ?: 0, view?.height ?: 0, radius.toFloat())
            }
        }
        clipToOutline = true
    } else {
        clipToOutline = false
    }
}




// ============================= View Click ==============================

/**
 * 点击立即响应 [action]
 * @param action
 * */
fun View.click(action: (view: View) -> Unit) = setOnClickListener(action)


/**
 * 长按立即响应 [action]
 * @param action
 * */
fun View.longClick(action: (view: View) -> Boolean) = setOnLongClickListener(action)


/**
 * 第一次点击后 [wait] ms 时间段内的点击事件不响应
 * */
fun View.throttleClick(wait: Long = 200, action: ((View) -> Unit)) {
    setOnClickListener(throttleClickImpl(wait, {}, {}, action = action))
}


/**
 * 第一次点击后 [wait] ms 时间段内的点击事件不响应
 * @param wait
 * @param onStart 刚开始点击时的动作
 * @param onEnd 间隔结束时的动作
 * @param action 点击执行的动作
 * */
fun View.throttleClick(
    wait: Long = 200,
    onStart: ((View) -> Unit)? = null,
    onEnd: ((View) -> Unit)? = null,
    action: ((View) -> Unit)
) {
    setOnClickListener(throttleClickImpl(wait, onStart ?: {}, onEnd ?: {}, action))
}


/**
 * [wait]ms 时间段内的点击事件只响应最后一次
 * @param wait 时间段
 * @param action
 * */
fun View.debounceClick(wait: Long = 200, action: (View) -> Unit) {
    setOnClickListener(debounceClickImpl(wait, action))
}



/**
 * 第一次点击后 [wait] ms 时间段内的点击事件不响应
 * @param wait 间隔时间
 * @param action
 * */

internal inline fun throttleClickImpl (
    wait: Long,
    crossinline onStart: (View) -> Unit,
    crossinline onEnd: (View) -> Unit,
    crossinline action: ((View) -> Unit)
): View.OnClickListener {
    return View.OnClickListener { v ->
        // 当前点击时间
        val current = SystemClock.uptimeMillis()
        // 上一次点击时间
        val lastClickTime = (v.getTag(R.id.utils_click_time_stamp) as? Long) ?: 0
        if (current - lastClickTime > wait) {
            onStart(v)
            v.setTag(R.id.utils_click_time_stamp, current)
            action(v)
            v.postDelayed(wait) {
                onEnd(v)
            }
        }
    }
}

/**
 * [wait]ms 时间段内的点击事件只响应最后一次
 * @param wait 时间段
 * @param action
 * */
internal fun debounceClickImpl(
    wait: Long,
    action: ((View) -> Unit)
): View.OnClickListener {
    return View.OnClickListener { v ->
        // 前一次设置的点击事件
        var preAction = v.getTag(R.id.utils_click_debounce_action) as? DebounceAction
        // 第一次设置
        if (preAction == null) {
            preAction = DebounceAction(v, action = action)
            v.setTag(R.id.utils_click_debounce_action, preAction)
        } else {
            preAction.action = action
        }
        // 移除上一次还没有执行的
        v.removeCallbacks(preAction)
        // 添加新的
        v.postDelayed(preAction, wait)
    }
}

/**
 * 简单封装
 * */
internal class DebounceAction(val view: View, var action: ((View) -> Unit)) : Runnable {
    override fun run() {
        if (view.isAttachedToWindow) {
            action(view)
        }
    }
}


// =========================== View  其他 ===============================

/**
 * 获取 View 的截图, 支持获取整个RecyclerView列表的长截图
 * 注意：调用该方法时，请确保View已经测量完毕，如果宽高为0，则将抛出异常
 */
fun View.toBitmap(): Bitmap {
    if (measuredWidth == 0 || measuredHeight == 0) {
        throw RuntimeException("Make sure the view $this has measured! The width or height must not be 0!")
    }
    return when (this) {
        is ListView -> {
            this.smoothScrollToPosition(0)
            this.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val bmp = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            //draw default bg, otherwise will be black
            if (background != null) {
                background.setBounds(0, 0, width, measuredHeight)
                background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            this.draw(canvas)
            //恢复高度
            this.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
            )
            bmp //return
        }

        is RecyclerView -> {
            this.scrollToPosition(0)
            this.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            val bmp = Bitmap.createBitmap(width, measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            //draw default bg, otherwise will be black
            if (background != null) {
                background.setBounds(0, 0, width, measuredHeight)
                background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            this.draw(canvas)
            //恢复高度
            this.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST)
            )
            bmp //return
        }

        else -> {
            val screenshot =
                Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_4444)
            val canvas = Canvas(screenshot)
            if (background != null) {
                background.setBounds(0, 0, width, measuredHeight)
                background.draw(canvas)
            } else {
                canvas.drawColor(Color.WHITE)
            }
            draw(canvas)// 将 view 画到画布上
            screenshot //return
        }
    }
}


