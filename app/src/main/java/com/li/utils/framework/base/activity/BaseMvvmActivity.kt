package com.li.utils.framework.base.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.viewbinding.ViewBinding
import com.li.utils.ext.common.lazyNone
import com.li.utils.framework.base.interfaces.IViewModel
import com.sll.lib_framework.base.activity.BaseBindingActivity

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/03
 */
abstract class BaseMvvmActivity<VB : ViewBinding, VM : ViewModel> : BaseBindingActivity<VB>(), IViewModel<VM> {
    protected val viewModel: VM by lazyNone {
        initViewModel()
    }

    override fun initViewModel(): VM {
        return ViewModelProvider(this, getViewModelFactory())[getViewModelClass().java]
    }

    /**
     * 默认使用 [androidx.lifecycle.SavedStateViewModelFactory] 作为默认 Factory
     *
     * 如果 ViewModel 的构造函数有自定义参数，则需要自己写一个 Factory，然后在这里返回即可
     *
     */
    override fun getViewModelFactory(): ViewModelProvider.Factory = defaultViewModelProviderFactory

    /**
     * 传入 ViewModel 的一些额外信息，例如 Intent
     * */
    override fun getCreationExtras(): CreationExtras = defaultViewModelCreationExtras


}