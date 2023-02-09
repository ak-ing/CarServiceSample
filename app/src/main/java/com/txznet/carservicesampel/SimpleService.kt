package com.txznet.carservicesampel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.txznet.carservicesampel.binder.HvacBinder
import com.txznet.sdk.util.CLASS_TAG
import com.txznet.sdk.util.logV

/**
 * Created by Rick on 2023-02-06  11:00.
 * Description:
 */
class SimpleService : LifecycleService() {

    private val TAG = CLASS_TAG

    companion object {
        private const val CHANNEL_ID_STRING = "SimpleService"
        private const val CHANNEL_ID = 0x11
    }


    private var mHvacBinder: HvacBinder? = null

    override fun onCreate() {
        super.onCreate()
        startServiceForeground()
        mHvacBinder = HvacBinder()
        logV(TAG, "[onCreate]")
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        logV(TAG, "[onBind]")
        return mHvacBinder
    }

    private fun startServiceForeground() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(
                CHANNEL_ID_STRING, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            val notification = Notification.Builder(
                applicationContext,
                CHANNEL_ID_STRING
            ).build()
            startForeground(CHANNEL_ID, notification)
        }
    }

}