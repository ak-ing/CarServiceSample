package com.txznet.carservicesampel.binder

import com.txznet.sdk.HvacCallback
import com.txznet.sdk.HvacInterface
import com.txznet.sdk.util.CLASS_TAG
import com.txznet.sdk.util.logV

/**
 * Created by Rick on 2023-02-06  15:51.
 * Description:
 */
class HvacBinder : HvacInterface.Stub() {
    private var hvacCallback: HvacCallback? = null
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
        hvacCallback?.onTemperatureChanged(getHvacTemp())
    }

    override fun requestTemperature() {
        logV(CLASS_TAG, "requestTemperature")
        hvacCallback?.onTemperatureChanged(getHvacTemp())
    }

    override fun registerCallback(callback: HvacCallback?): Boolean {
        hvacCallback = callback
        return true
    }

    override fun unregisterCallback(callback: HvacCallback?): Boolean {
        hvacCallback = null
        return true
    }
}