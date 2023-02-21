package com.txznet.sdk.util

import android.text.TextUtils
import android.util.Log

/**
 * Created by Rick on 2023-02-03  14:40.
 */


/**
 * Print verbose log info.
 *
 * @param tag  title
 * @param info description
 */
fun logV(tag: String?, info: String) =
    SdkLogUtils.showLogWithLineNum(tag, "[thread:" + Thread.currentThread().name + "] - ", info)

/**
 * TAG
 */
val Any.CLASS_TAG get() = this::class.java.simpleName + ":" + Integer.toHexString(hashCode())

object SdkLogUtils {
    const val TAG_SDK = "[SDK]-LogUtil："
    private var VERBOSE = true
    fun init(verbose: Boolean) {
        VERBOSE = verbose
    }

    fun v(tag: String?, info: String) {
        if (VERBOSE) {
            showLogWithLineNum(tag, "[thread:" + Thread.currentThread().name + "] - ", info)
        }
    }

    fun showLogWithLineNum(tag: String?, thread: String, content: String) {
        if (!VERBOSE) {
            return
        }
        val contents = getAutoJumpLogInfos()
        if (!TextUtils.isEmpty(tag) && tag != contents[0]) {
            contents[0] = contents[0] + ":" + tag
        }
        if (TextUtils.isEmpty(content)) {
            Log.v(contents[0] + "." + contents[1], thread + contents[2] + content)
            return
        }
        var index = 0
        var size = 1024 * 3
        while (index < content.length) {
            if (content.length < index + size) {
                size = content.length - index
            }
            val subContent = content.substring(index, index + size)
            index += size
            Log.v(contents[0] + "." + contents[1], thread + contents[2] + subContent)
        }
    }

    /**
     * 获取打印信息所在方法名，行号等信息
     *
     * @return
     */
    private fun getAutoJumpLogInfos(): Array<String> {
        val contents = arrayOf("", "", "")
        val stackTrace = Thread.currentThread().stackTrace
        return if (stackTrace.size < 6) {
            Log.e(TAG_SDK, "Stack is too shallow!!!")
            contents
        } else {
            contents[0] = "txz_link_" + stackTrace[5].className.substring(
                stackTrace[5].className.lastIndexOf(".") + 1
            )
            contents[1] = stackTrace[5].methodName + "()"
            contents[2] = (" (" + stackTrace[5].fileName + ":"
                    + stackTrace[5].lineNumber + ") ")
            contents
        }
    }

}