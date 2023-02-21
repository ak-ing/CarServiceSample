package com.txznet.sdk.base

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import com.txznet.sdk.SdkAppGlobal
import com.txznet.sdk.listener.IServiceConnectListener
import com.txznet.sdk.util.CLASS_TAG
import com.txznet.sdk.util.RemoteHelper
import com.txznet.sdk.util.SdkLogUtils
import com.txznet.sdk.util.logV
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by Rick on 2023-02-03  14:38.
 * Description: 把Client端对Service的绑定、重连、线程切换等细节隐藏到SDK中并封装，
 * 使用时只需要继承BaseConnectManager并传入Service的包名、类名和期望的断线重连时间即可。
 */
abstract class BaseConnectManager<T : IInterface> {

    private val TAG = SdkLogUtils.TAG_SDK + CLASS_TAG

    private var mProxy: T? = null
    private var mApplication: Application = SdkAppGlobal.context
    private var mServiceListener: IServiceConnectListener? = null
    private val mChildThread: Handler
    private val mMainThread: Handler
    private val mTaskQueue = LinkedBlockingQueue<Runnable>()
    private val mBindServiceTask = ::bindService
    private var mRetryTimeMill: Long = DEFAULT_RETRY_TIME
    private val mServiceConnection: ServiceConnection

    open fun init(application: Application) {
        SdkAppGlobal.init(application)
        bindService()
    }

    init {
        val thread = HandlerThread(THREAD_NAME)
        thread.start()
        mChildThread = Handler(thread.looper)
        mMainThread = Handler(Looper.getMainLooper())
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                logV(TAG, "[onServiceConnected]")
                mProxy = asInterface(service)
                RemoteHelper.tryExec { service?.linkToDeath(mDeathRecipient, 0) }
                onBindSuccess()
                mServiceListener?.onServiceConnected()
                handlerTask()
                mChildThread.removeCallbacks(mBindServiceTask)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                logV(TAG, "[onServiceDisconnected]")
                mProxy = null
                mServiceListener?.onServiceDisconnected()
            }
        }
        bindService()
    }

    private val mDeathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            logV(TAG, "[binderDied]")
            onBinderDied()
            mServiceListener?.onBinderDied()
            mProxy?.asBinder()?.unlinkToDeath(this, 0)
            mProxy?.let { mProxy = null }

            attemptToRebindService()
        }
    }

    private fun bindService() {
        if (mProxy == null) {
            logV(TAG, "[bindService] start")
            val name = ComponentName(getServicePkgName(), getServiceClassName())
            val intent = Intent()
            getServiceAction()?.let { intent.setAction(it) }
            intent.component = name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mApplication.startForegroundService(intent)
            } else {
                mApplication.startService(intent)
            }
            val connected = mApplication.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
            logV(TAG, "[bindService] connected result： $connected")
            mServiceListener?.onBindServiceResult(connected)
            if (!connected) {
                attemptToRebindService()
            }
        } else {
            logV(TAG, "[bindService] not need");
        }
    }


    protected open fun attemptToRebindService() {
        logV(TAG, "[attemptToRebindService]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mChildThread.hasCallbacks(mBindServiceTask)) return
        }
        mChildThread.postDelayed(mBindServiceTask, mRetryTimeMill)
    }

    private fun handlerTask() {
        var task: Runnable?
        while (mTaskQueue.poll().also { task = it } != null) {
            logV(TAG, "[handleTask] poll task form task queue")
            mChildThread.post(task!!)
        }
    }

    fun isServiceConnected(): Boolean = isServiceConnected(false)

    fun isServiceConnected(tryConnect: Boolean): Boolean {
        logV(TAG, "[isServiceConnected] tryConnect $tryConnect;isConnected ${mProxy != null}")
        if (mProxy == null && tryConnect) {
            attemptToRebindService()
        }
        return mProxy != null
    }

    fun release() {
        logV(TAG, "[release]")
        mProxy?.let {
            it.asBinder().unlinkToDeath(mDeathRecipient, 0)
            mProxy = null
            mApplication.unbindService(mServiceConnection)
            mServiceListener?.onUnbindService()
        }
    }

    fun setSateListener(listener: IServiceConnectListener) {
        logV(TAG, "[setSateListener] $listener")
        mServiceListener = listener
    }

    fun removeStateListener() {
        logV(TAG, "[removeStateListener]")
        mServiceListener = null
    }

    fun getMainHandler(): Handler = mMainThread

    protected fun getProxy(): T {
        mProxy?.let { return it }
        error("[getProxy] mProxy is null!")
    }

    protected fun getTaskQueue(): LinkedBlockingQueue<Runnable> = mTaskQueue

    protected fun getServiceAction(): String? {
        return null
    }

    protected fun setRetryBindTimeMill(timeMill: Long) {
        mRetryTimeMill = timeMill
    }

    protected fun tryExec(block: RemoteHelper.RemoteVoidFunction) {
        RemoteHelper.tryExec {
            if (isServiceConnected(true)) {
                logV(TAG, "[tryExec] call()")
                block.call()
            } else {
                logV(TAG, "[tryExec] offer")
                getTaskQueue().offer(block::call)
            }
        }
    }


    protected abstract fun getServicePkgName(): String
    protected abstract fun getServiceClassName(): String
    protected abstract fun asInterface(service: IBinder?): T
    protected abstract fun onBindSuccess()
    protected abstract fun onBinderDied()
    fun asBinder(): IBinder? {
        return null
    }

    companion object {
        const val THREAD_NAME = "bindServiceThread"
        const val DEFAULT_RETRY_TIME = 5000L
    }
}