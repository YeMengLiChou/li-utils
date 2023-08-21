package com.li.utils.framework.base.interfaces

import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
interface IViewBinding <VB : ViewBinding> {
    /**
     * 初始化 Activity/Fragment 的 ViewBinding
     *
     * @param container Fragment 初始化所需要的
     * */
    fun initViewBinding(container: ViewGroup? = null): VB
}