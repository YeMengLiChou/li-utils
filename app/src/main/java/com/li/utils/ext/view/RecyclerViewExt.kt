package com.li.utils.ext.view

import android.view.ViewConfiguration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.li.utils.ext.common.`as`
import kotlin.math.abs

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */


/**
 * 竖直方向下的滑动监听
 * @param onScrolledUp 向上滑触发
 * @param onScrolledDown 向下滑触发
 * @param onScrolledToTop 滑到顶触发
 * @param onScrolledToBottom 滑到低触发
 * */
inline fun RecyclerView.addOnVerticalScrollListener (
    crossinline onScrolledUp: (recyclerView: RecyclerView) -> Unit = {},
    crossinline onScrolledDown: (recyclerView: RecyclerView) -> Unit = {},
    crossinline onScrolledToTop: (recyclerView: RecyclerView) -> Unit = {},
    crossinline onScrolledToBottom: (recyclerView: RecyclerView) -> Unit = {},
) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (!recyclerView.canScrollVertically(-1)) { // 不能继续向上滚动 (手指下拉)
                // 顶部
                onScrolledUp.invoke(recyclerView)
            } else if (!recyclerView.canScrollVertically(1)) {
                // 底部
                onScrolledDown.invoke(recyclerView)
            } else if (dy < 0 && abs(dy) > ViewConfiguration.get(context).scaledTouchSlop) {
                // 往上滑
                onScrolledToTop.invoke(recyclerView)
            } else if (dy > 0 && abs(dy) > ViewConfiguration.get(context).scaledTouchSlop) {
                // 往下滑
                onScrolledToBottom.invoke(recyclerView)
            }
        }
    })
}

/**
 * 方向
 * */
inline val RecyclerView.orientation
    get() = if (layoutManager == null) -1 else layoutManager.run {
        when (this) {
            is GridLayoutManager -> orientation
            is LinearLayoutManager -> orientation
            is StaggeredGridLayoutManager -> orientation
            else -> -1
        }
    }

/**
 * 水平放置
 * @param spanCount 一列的个数
 * */
fun RecyclerView.horizontal(
    spanCount: Int = 0,
): RecyclerView {
    if (orientation != LinearLayoutManager.HORIZONTAL) {
        when (layoutManager) {
            is GridLayoutManager -> {
                (layoutManager as GridLayoutManager).apply {
                    this.spanCount = spanCount
                    this.orientation = GridLayoutManager.HORIZONTAL
                }
            }
            is LinearLayoutManager -> {
                (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.HORIZONTAL
            }
            is StaggeredGridLayoutManager -> {
                (layoutManager as StaggeredGridLayoutManager).apply {
                    this.orientation = StaggeredGridLayoutManager.HORIZONTAL
                    this.spanCount = spanCount
                }
            }
        }
    }
    return this
}

/**
 * 竖直放置
 * @param spanCount 一行的个数
 * */
fun RecyclerView.vertical(
    spanCount: Int = 0,
): RecyclerView {
    if (orientation != LinearLayoutManager.HORIZONTAL) {
        when (layoutManager) {
            is GridLayoutManager -> {
                (layoutManager as GridLayoutManager).apply {
                    this.spanCount = spanCount
                    this.orientation = GridLayoutManager.VERTICAL
                }
            }
            is LinearLayoutManager -> {
                (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.VERTICAL
            }
            is StaggeredGridLayoutManager -> {
                (layoutManager as StaggeredGridLayoutManager).apply {
                    this.orientation = StaggeredGridLayoutManager.VERTICAL
                    this.spanCount = spanCount
                }
            }
        }
    }
    return this
}

/**
 * 设置 [RecyclerView.ItemAnimator]
 * */
fun RecyclerView.itemAnimator(animator: RecyclerView.ItemAnimator): RecyclerView {
    itemAnimator = animator
    return this
}

/**
 * 清除 [RecyclerView.ItemAnimator]
 * */
fun RecyclerView.noItemAnim(): RecyclerView{
    itemAnimator = null
    return this
}



/**
 * 检查是否已经到达顶部
 * */
fun RecyclerView.checkReachTop(): Boolean {
    if (orientation == RecyclerView.VERTICAL) {
        return !this.canScrollVertically(-1)
    } else {
        return !this.canScrollHorizontally(-1)
    }
}


/**
 * 检查是否已经到达底部
 * */
fun RecyclerView.checkReachBottom(): Boolean {
    if (orientation == RecyclerView.VERTICAL) {
        return !this.canScrollVertically(1)
    } else {
        return !this.canScrollHorizontally(1)
    }
}