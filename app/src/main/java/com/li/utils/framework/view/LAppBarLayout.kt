package com.li.utils.framework.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.appbar.AppBarLayout

/**
 * 可以获取到滚动偏移量的 [AppBarLayout]
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/09
 */
class LAppBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.appBarLayoutStyle
): AppBarLayout(context, attrs, defStyleAttr) {
    companion object {
        private val TAG = LAppBarLayout::class.simpleName
    }

    interface OnStateChangedListener {
        fun onStateChange(appBarLayout: AppBarLayout, state: State)
    }

    enum class State {
        // 完全展开
        EXPANDED,
        // 完全收缩
        COLLAPSED,
        // 中间态
        INTERMEDIATE
    }

    val isExpanded get() = verticalOffset == 0

    var currentState: State = State.EXPANDED
        private set

    // 当前
    var verticalOffset: Int = 0
        private set

    // 状态监听列表
    private var listeners: MutableList<OnStateChangedListener>? = null

    init {
        // 初始化
        addOnOffsetChangedListener { appBarLayout: AppBarLayout, verticalOffset: Int ->
            this.verticalOffset = verticalOffset
            val changed: Boolean =
                if (verticalOffset == -totalScrollRange && currentState != State.COLLAPSED) {
                    currentState = State.COLLAPSED
                    true
                } else if (verticalOffset == 0 && currentState != State.EXPANDED) {
                    currentState = State.EXPANDED
                    true
                } else if (currentState != State.INTERMEDIATE){
                    currentState = State.INTERMEDIATE
                    true
                } else {
                    false
                }
            // 通知监听器状态发生改变
            if (changed) {
                listeners?.forEach {
                    it.onStateChange(this@LAppBarLayout, currentState)
                }
            }
        }

    }


    fun addOnStateChangedListener(listener: OnStateChangedListener) {
        if (listeners == null) {
            listeners = mutableListOf()
        }
        if (!listeners!!.contains(listener)) {
            this.listeners?.add(listener)
        }
    }

    fun removeStateChangedListener(listener: OnStateChangedListener) {
        this.listeners?.remove(listener)
    }



}