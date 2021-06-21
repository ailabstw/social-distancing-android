package tw.gov.cdc.exposurenotifications.common

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import mu.KotlinLogging
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.BuildConfig

object Log {

    init {
        val filePath: String = Environment.getExternalStorageState().toString() + "/logcat.txt"
        Runtime.getRuntime().exec(arrayOf("logcat", "-f", filePath, "MyAppTAG:V", "*:S"))
    }

    private val DEBUG: Boolean = BuildConfig.DEBUG
    private var powerManager = BaseApplication.instance.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val logger = KotlinLogging.logger {}

    private val isIdle: String
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return if (powerManager.isDeviceIdleMode) {
                    "[ IDLE ] "
                } else {
                    "[ACTIVE] "
                }
            }
            return "[NO DOZE] "
        }

    fun v(tag: String, msg: String) {
        if (DEBUG) logger.debug { "$isIdle$tag: $msg" }
    }

    fun d(tag: String, msg: String) {
        if (DEBUG) logger.debug { "$isIdle$tag: $msg" }
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) logger.info { "$isIdle$tag: $msg" }
    }

    fun w(tag: String, msg: String) {
        if (DEBUG) logger.warn { "$isIdle$tag: $msg" }
    }

    fun e(tag: String, msg: String) {
        if (DEBUG) logger.error { "$isIdle$tag: $msg" }
    }
}
