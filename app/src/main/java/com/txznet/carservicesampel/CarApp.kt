package com.txznet.carservicesampel

import android.app.Application
import android.content.Intent
import android.os.Build
import com.txznet.sdk.SdkAppGlobal

/**
 * Created by Rick on 2023-02-06  11:28.
 * Description:
 */
class CarApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SdkAppGlobal.init(this)
        startService()
    }

    private fun startService() {
        val startServiceIntent = Intent(this, SimpleService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startServiceIntent)
        } else {
            startService(startServiceIntent)
        }
    }

}