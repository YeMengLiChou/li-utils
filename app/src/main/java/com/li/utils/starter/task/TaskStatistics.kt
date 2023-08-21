package com.li.utils.starter.task

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/07/18
 */
object TaskStatistics {

    private var mCostTime = 0L

    val costTime: Long
        get() = mCostTime

    @Volatile
    private var mCurrentSituation = ""

    private var mBeans = mutableListOf<TaskStatisticsBean>()

    private var mTaskDoneCount = AtomicInteger()

    var currentSituation: String
        get() = mCurrentSituation
        set(value) {
            Log.i("TaskStatistics", mCurrentSituation)
            mCurrentSituation = value
            initStatistics()
        }

    fun markTaskDone() {
        mTaskDoneCount.getAndIncrement()
    }

    fun initStatistics() {
        val bean = TaskStatisticsBean(mCurrentSituation, mTaskDoneCount.get())
        mBeans.add(bean)
        mTaskDoneCount = AtomicInteger(0)
    }

}