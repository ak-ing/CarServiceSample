package com.txznet.carservicesampel.binder

import android.os.RemoteCallbackList
import com.txznet.sdk.HvacCallback
import com.txznet.sdk.HvacInterface
import com.txznet.sdk.util.CLASS_TAG
import com.txznet.sdk.util.logV

/**
 * Created by Rick on 2023-02-06  15:51.
 * Description:
 */
class HvacBinder : HvacInterface.Stub() {
    private var hvacCallbacks = RemoteCallbackList<HvacCallback>()
    private var mHvacTemp = 18.0

    private fun getHvacTemp(): Double {
        return mHvacTemp
    }

    private fun setHvacTemp(temperature: Double) {
        mHvacTemp = temperature
    }

    override fun setTemperature(temperature: Int) {
        logV(CLASS_TAG, "setTemperature: $temperature")
        setHvacTemp(temperature.toDouble())
        requestTemperature()
    }

    override fun requestTemperature() {
        logV(CLASS_TAG, "requestTemperature")
        val count = hvacCallbacks.beginBroadcast()
        for (i in 0 until count) {
            val hvacCallback = hvacCallbacks.getBroadcastItem(i)
            hvacCallback.onTemperatureChanged(getHvacTemp())
        }
        hvacCallbacks.finishBroadcast()
    }

    override fun registerCallback(callback: HvacCallback): Boolean {
        logV(CLASS_TAG, "registerCallback")
        return hvacCallbacks.register(callback)
    }

    override fun unregisterCallback(callback: HvacCallback): Boolean {
        logV(CLASS_TAG, "unregisterCallback")
        return hvacCallbacks.unregister(callback)
    }
}