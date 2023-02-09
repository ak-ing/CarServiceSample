package com.txznet.sdk.sample

import android.os.IBinder
import com.txznet.sdk.HvacCallback

/**
 * Created by Rick on 2023-02-03  17:08.
 */
interface DefaultHvacCallback : HvacCallback {
    override fun asBinder(): IBinder? = null
}