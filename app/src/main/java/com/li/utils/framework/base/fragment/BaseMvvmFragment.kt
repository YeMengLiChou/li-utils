package com.sll.lib_framework.base.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.viewbinding.ViewBinding
import com.li.utils.ext.common.lazyNone
import com.li.utils.framework.base.interfaces.IViewModel

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
abstract class BaseMvvmFragment <VB : ViewBinding, VM: ViewModel> : BaseBindingFragment<VB>(), IViewModel<VM> {

    // 实例化对应的 ViewModel
    protected val viewModel: VM by lazyNone {
        initViewModel()
    }

    /**
     * 默认使用 [androidx.lifecycle.SavedStateViewModelFactory] 作为默认 Factory
     */
    override fun getViewModelFactory(): ViewModelProvider.Factory = defaultViewModelProviderFactory

    /**
     * 传入 ViewModel 的一些额外信息，例如 Intent
     * */
    override fun getCreationExtras(): CreationExtras = defaultViewModelCreationExtras

    /**
     * 该方法获取的是与 Activity 相同实例的 ViewModel。如果需要重新维护，则重写该方法
     * */
    override fun initViewModel(): VM {
        return ViewModelProvider(requireActivity(), getViewModelFactory())[getViewModelClass().java]
    }

}