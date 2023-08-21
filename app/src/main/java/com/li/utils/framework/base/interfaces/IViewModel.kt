package com.li.utils.framework.base.interfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
interface IViewModel<VM : ViewModel> {

    /**
     * 指定与 Activity/Fragment 相关联的 ViewModel
     * */
    fun getViewModelClass(): KClass<VM>

    /**
     * 创建 ViewModel 的 Factory，如果有依赖项，则自定义 Factory
     * */
    fun getViewModelFactory(): ViewModelProvider.Factory

    fun getCreationExtras(): CreationExtras

    fun initViewModel(): VM
}