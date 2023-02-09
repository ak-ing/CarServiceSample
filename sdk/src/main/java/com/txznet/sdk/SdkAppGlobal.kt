package com.txznet.sdk

import android.app.Application
import com.txznet.sdk.util.CLASS_TAG
import com.txznet.sdk.util.SdkLogUtils

/**
 * Created by Rick on 2023-01-30  21:12.
 * Description: 全局ApplicationContext
 */
object SdkAppGlobal {
    private val TAG = SdkLogUtils.TAG_SDK + CLASS_TAG

    const val CLASS_FOR_NAME = "android.app.ActivityThread"
    const val CURRENT_APPLICATION = "currentApplication"
    const val GET_INITIAL_APPLICATION = "getInitialApplication"

    private var app: Application? = null

    fun init(application: Application) {
        app = application
    }

    val context: Application
        get() {
            if (app == null) {
                getApplication()
            }
            return app!!
        }

    /**
     * Get application.
     * @return application context.
     */
    private fun getApplication(): Application? {
        try {
            val atClass = Class.forName(CLASS_FOR_NAME)
            val method = atClass.getDeclaredMethod(CURRENT_APPLICATION)
            method.isAccessible = true
            app = method.invoke(null) as Application
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        app?.let { return app }
        try {
            val atClass = Class.forName(CLASS_FOR_NAME)
            val method = atClass.getDeclaredMethod(GET_INITIAL_APPLICATION)
            method.isAccessible = true
            app = method.invoke(null) as Application
        } catch (exception: IllegalAccessException) {
            exception.printStackTrace()
        }
        return app
    }
}