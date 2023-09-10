package com.li.utils.framework.base.fragment

import androidx.fragment.app.Fragment

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
abstract class BaseFragment: Fragment() {

    /**
     * 检查是否已经与 activity attached
     * */
    fun checkAttachedActivity(): Boolean {
        return activity != null
    }


}