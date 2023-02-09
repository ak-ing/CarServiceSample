package com.txznet.sdk.listener

/**
 * Created by Rick on 2023-02-03  15:18.
 */
interface IServiceConnectListener {
    fun onServiceConnected() {}

    fun onServiceDisconnected() {}

    fun onBindServiceResult(result: Boolean) {}

    fun onUnbindService() {}

    fun onBinderDied() {}
}