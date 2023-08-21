package com.li.utils.starter.dispatcher

import android.os.Looper
import android.os.MessageQueue
import com.li.utils.starter.task.Task
import com.li.utils.starter.task.TaskRunnable
import java.util.*

/**
 * 延迟初始化 利用 IdleHandler 的等待主线程空闲特性，在空闲时才去执行任务
 * */
class DelayInitDispatcher {
    private val mDelayTasks: Queue<Task> = LinkedList()
    private val mIdleHandler = MessageQueue.IdleHandler {
        if (mDelayTasks.size > 0) {
            val task = mDelayTasks.poll()
            if (task != null) {
                TaskRunnable(task).run()
            }
        }
        !mDelayTasks.isEmpty()
    }

    fun addTask(task: Task): DelayInitDispatcher {
        mDelayTasks.add(task)
        return this
    }

    fun start() {
        Looper.myQueue().addIdleHandler(mIdleHandler)
    }
}