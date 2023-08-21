package com.li.utils.framework.base.viewholder

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/07/18
 */
open class BaseBindViewHolder<VB : ViewBinding>(
    val binding: VB
) : BaseViewHolder(binding.root)