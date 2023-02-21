package com.txznet.sdk.sample

import android.os.IBinder
import com.txznet.sdk.HvacCallback
import com.txznet.sdk.HvacInterface
import com.txznet.sdk.base.BaseConnectManager
import com.txznet.sdk.util.RemoteHelper
import com.txznet.sdk.util.SdkLogUtils
import com.txznet.sdk.util.logV

/**
 * Created by Rick on 2023-02-03  17:10.
 * Description: 示例
 */
class HvacManager : BaseConnectManager<HvacInterface>(), HvacInterface {
    companion object {
        private const val TAG = SdkLogUtils.TAG_SDK + "HvacManager"
        private const val SERVICE_PACKAGE = "com.txznet.carservicesampel"
        private const val SERVICE_CLASSNAME = "com.txznet.carservicesampel.SimpleService"

        val instant by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { HvacManager() }
    }

    private val mCallbacks = mutableListOf<HvacCallback>()

    //Dispatch Callbacks
    private val mSampleDispatchCallback: HvacCallback = object : HvacCallback.Stub() {
        override fun onTemperatureChanged(temperature: Double) {
            logV(TAG, "[onTemperatureChanged] $temperature")
            getMainHandler().post {
                for (callback in mCallbacks) {
                    callback.onTemperatureChanged(temperature)
                }
            }
        }
    }

    override fun getServicePkgName(): String = SERVICE_PACKAGE

    override fun getServiceClassName(): String = SERVICE_CLASSNAME

    override fun asInterface(service: IBinder?): HvacInterface {
        return HvacInterface.Stub.asInterface(service)
    }

    override fun onBindSuccess() {
        logV(TAG, "[onBindSuccess]")
        getProxy().registerCallback(mSampleDispatchCallback)
    }

    override fun onBinderDied() {
        logV(TAG, "[onBinderDied]")
        getProxy().unregisterCallback(mSampleDispatchCallback)
    }

    /******************/

    override fun requestTemperature() {
        tryExec {
            logV(TAG, "[requestTemperature] getProxy().requestTemperature ${getProxy()}")
            getProxy().requestTemperature()
        }
    }

    override fun setTemperature(temperature: Int) {
        tryExec {
            logV(TAG, "[setTemperature] getProxy().setTemperature $temperature")
            getProxy().setTemperature(temperature)
        }
    }

    override fun registerCallback(callback: HvacCallback): Boolean {
        return RemoteHelper.exec {
            if (isServiceConnected(true)) {
                logV(TAG, "[registerCallback] getProxy().registerCallback ${getProxy()}")
                val result = getProxy().registerCallback(mSampleDispatchCallback)
                if (result) {
                    mCallbacks.remove(callback)
                    mCallbacks.add(callback)
                }
                return@exec result
            } else {
                getTaskQueue().offer {
                    logV(TAG, "[registerCallback] offer")
                    registerCallback(callback)
                }
                return@exec false
            }
        }
    }

    override fun unregisterCallback(callback: HvacCallback): Boolean {
        logV(TAG, "[unregisterCallback]")
        return RemoteHelper.exec {
            if (isServiceConnected(true)) {
                return@exec mCallbacks.remove(callback)
            } else {
                getTaskQueue().offer {
                    unregisterCallback(callback)
                }
                return@exec false
            }
        }
    }

}