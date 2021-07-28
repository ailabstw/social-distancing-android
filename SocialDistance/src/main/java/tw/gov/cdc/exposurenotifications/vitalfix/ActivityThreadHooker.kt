package tw.gov.cdc.exposurenotifications.vitalfix

import android.os.Build
import android.os.Handler
import android.os.Message

/**
 * Source: https://github.com/kswlee/android-vital-fix
 */

/**
 * Class ActivityThreadHooker
 *
 * Use to hook ActivityThread handler callback to override system behavior
 */
object ActivityThreadHooker {
    private const val sPauseActivity = 101
    private const val sResumeActivity = 107
    private const val sServiceArgs = 115
    private const val sScheduleCrash = 134
    private const val sActivityThreadName = "android.app.ActivityThread"

    private val msgHandlers: MutableMap<Int, (() -> Boolean)?> = mutableMapOf()

    private val callbackHandler = object: Handler.Callback {
        override fun handleMessage(message: Message): Boolean {
            if (msgHandlers.containsKey(message.what)) {
                return msgHandlers[message.what]?.invoke() == true
            }

            return false
        }
    }

    fun setupScheduleCrashHandler() {
        msgHandlers[sScheduleCrash] = {
            // Fix RemoteServiceException
            true
        }
    }

    fun setupServiceArgsHandler() {
        // Only apply to Android 5 ~ 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
            Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            return
        }

        msgHandlers[sServiceArgs] = {
            QueuedWorkProxy.consumePendingWorks()
            false
        }
    }

    fun setupActivityArgsHandler() {
        // Only apply to Android 5 ~ 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
            Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            return
        }

        val handler = {
            QueuedWorkProxy.consumePendingWorks()
            false
        }

        listOf(sPauseActivity, sResumeActivity).forEach {
            msgHandlers[it] = handler
        }
    }

    fun hook() {
        setupScheduleCrashHandler()
        conditionalHook()
    }

    fun conditionalHook() {
        val requireHook = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (!requireHook) {
            return
        }

        try {
            hookActivityThread()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun hookActivityThread() {
        val activityThread = Reflection.getClassMember(
            sActivityThreadName,
            "sCurrentActivityThread"
        )
        val internalHandler = Reflection.getClassMember(
            sActivityThreadName,
            "mH", activityThread
        )
        Reflection.setClassMember(
            Handler::class.java as Class<Any>, "mCallback",
            internalHandler, callbackHandler
        )
    }
}