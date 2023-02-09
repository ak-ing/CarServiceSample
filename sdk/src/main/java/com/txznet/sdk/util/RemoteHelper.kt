package com.txznet.sdk.util

import android.os.RemoteException

/**
 * Created by Rick on 2023-02-03  14:59.
 * Description: 安全的调用远程方法
 *
 * @author WuJia
 * @version 1.0
 * @date 2021/8/13
 */
object RemoteHelper {
    private val TAG: String = SdkLogUtils.TAG_SDK + CLASS_TAG

    /**
     * Throwing void function.
     */
    fun interface RemoteVoidFunction {
        /**
         * The actual throwing function.
         */
        @Throws(RemoteException::class)
        fun call()
    }

    /**
     * Throwing function that returns some value.
     * @param <V> Return type for the function.
    </V> */
    fun interface RemoteFunction<V> {
        /**
         * The actual throwing function.
         */
        @Throws(RemoteException::class)
        fun call(): V
    }

    /**
     * Wraps remote function and rethrows [RemoteException].
     */
    fun <V> exec(func: RemoteFunction<V>): V {
        return try {
            func.call()
        } catch (exception: RemoteException) {
            throw IllegalArgumentException("Failed to execute remote call$exception")
        }
    }



    /**
     * Wraps remote void function and logs in case of [RemoteException].
     */
    fun tryExec(func: RemoteVoidFunction) {
        try {
            func.call()
        } catch (exception: RemoteException) {
            logV(TAG, exception.toString())
        }
    }
}