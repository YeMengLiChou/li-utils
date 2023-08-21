package com.li.utils.starter.task

import android.os.Looper
import android.os.Process
import android.os.Trace
import android.util.Log
import com.li.utils.framework.manager.AppManager
import com.li.utils.starter.dispatcher.TaskDispatcher


/**
 * 执行 [Task] 的 [Runnable]
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/07/18
 */
class TaskRunnable : Runnable {
    /** 需要执行的任务 */
    private var mTask: Task

    /** 调度器 */
    private var mTaskDispatcher: TaskDispatcher? = null

    constructor(task: Task) {
        mTask = task
    }

    constructor(task: Task, dispatcher: TaskDispatcher) {
        mTask = task
        mTaskDispatcher = dispatcher
    }


    override fun run() {
        val taskName = mTask::class.simpleName.toString()
        Trace.beginSection(taskName)
        Log.i("TaskRunnable", "--->TaskRunnable start: ${mTask::class.simpleName.toString()}")
        Process.setThreadPriority(mTask.priority())
        var startTime = System.currentTimeMillis()

        // 等待前面的 Task 执行
        mTask.isWaiting = true
        mTask.waitToSatisfy()
        val waitTime = System.currentTimeMillis() - startTime

        // 开始执行
        startTime = System.currentTimeMillis()
        mTask.isRunning = true
        mTask.run()

        // 执行后续指定的 runnable
        mTask.tailRunnable?.run()

        if (!mTask.needCall() || !mTask.runOnMainThread()) {
            printTaskLog(startTime, waitTime)
            TaskStatistics.markTaskDone()
            mTask.isFinished = true
            mTaskDispatcher?.apply {
                notifyNext(mTask)
                markTaskDone(mTask)
            }
            Log.i("TaskRunnable", "<---TaskRunnable finish: ${mTask::class.simpleName.toString()}")
        }
        Trace.endSection()
    }

    /**
     * 打印出来Task执行的日志
     *
     * @param startTime
     * @param waitTime
     */
    private fun printTaskLog(startTime: Long, waitTime: Long) {
        val runTime = System.currentTimeMillis() - startTime
        if (AppManager.isDebug) {
            Log.i(
                "TaskRunnable",
                mTask.javaClass.simpleName + "\n\t| Wait:" + waitTime + " ms | Run:"
                        + runTime + " ms | IsMain：" + (Looper.getMainLooper() == Looper.myLooper())
                        + " | NeedWait: " + (mTask.needWait() || Looper.getMainLooper() == Looper.myLooper())
                        + " | ThreadId: " + Thread.currentThread().id
                        + " | ThreadName: " + Thread.currentThread().name
                        + " | Situation: " + TaskStatistics.currentSituation
            )
        }
    }
}