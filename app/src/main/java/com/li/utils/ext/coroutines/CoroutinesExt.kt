package com.li.utils.ext.coroutines

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 对协程的扩展简写函数
 *
 *
 * @author Gleamrise
 * <br/>Created: 2023/08/21
 */

/**
 * IO线程
 * */
fun launchIO(
    block: suspend CoroutineScope.() -> Unit
) =  CoroutineScope(Dispatchers.IO).launch(block = block)


/**
 * Default线程
 * */
fun launchCpu(
    block: suspend CoroutineScope.() -> Unit
) = CoroutineScope(Dispatchers.Default).launch(block = block)


fun LifecycleOwner.launchIO(
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(Dispatchers.IO, block = block)


fun LifecycleOwner.launchCpu(
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(Dispatchers.Default, block = block)


suspend fun <T> LifecycleOwner.withIO(
    block: suspend CoroutineScope.() -> T
) = withContext(Dispatchers.IO, block = block)


fun <T> LifecycleOwner.asyncIO(
    block: suspend CoroutineScope.() -> T
) = lifecycleScope.async(Dispatchers.IO, block = block)



/**
 * 启动生命周期协程，在 onCreate 后，在 onStop 前调用
 *
 * Created state for a LifecycleOwner. For an [android.app.Activity], this state
 * is reached in two cases:
 *
 *  * after [onCreate][android.app.Activity.onCreate] call;
 *  * **right before** [onStop][android.app.Activity.onStop] call.
 *
 */
fun LifecycleOwner.launchOnCreated(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.CREATED, block)
}



/**
 * 启动生命周期协程，在 onStarted 后，在 onPause 前调用
 *
 * For an [android.app.Activity], this state
 * is reached in two cases:
 *
 *  * after [onStart][android.app.Activity.onStart] call;
 *  * **right before** [onPause][android.app.Activity.onPause] call.
 *
 */
fun LifecycleOwner.launchOnStarted(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.STARTED, block)
}


/**
 * 启动生命周期协程，在 onResumed 后面调用
 *
 * For an [android.app.Activity], this state
 * is reached after [onResume][android.app.Activity.onResume] is called.
 */
fun LifecycleOwner.launchOnResume(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.RESUMED, block)
}


/**
 * 启动生命周期协程，在 onDestroy 前调用
 *
 * After this event, this Lifecycle will not dispatch
 * any more events. For instance, for an [android.app.Activity], this state is reached
 * **right before** Activity's [onDestroy][android.app.Activity.onDestroy] call.
 */
fun LifecycleOwner.launchDestroyed(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.DESTROYED, block)
}


/**
 * 启动生命周期协程，在 onCreate 前调用
 *
 * For an [android.app.Activity], this is
 * the state when it is constructed but has not received
 * [onCreate][android.app.Activity.onCreate] yet.
 */
fun LifecycleOwner.launchOnInitialized(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = lifecycleScope.launch(context) {
    repeatOnLifecycle(Lifecycle.State.INITIALIZED, block)
}

