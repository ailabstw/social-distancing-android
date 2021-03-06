package tw.gov.cdc.exposurenotifications.common

import android.content.Context
import androidx.core.content.edit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.api.APIService
import tw.gov.cdc.exposurenotifications.api.response.ConfigResponse
import tw.gov.cdc.exposurenotifications.api.response.HealthEducationResponse
import tw.gov.cdc.exposurenotifications.data.InstructionRepository
import java.util.*

object PreferenceManager {

    private const val TAG = "PreferenceManager"

    private const val PREF_NAME = "PREF_NAME_TW_CDC_EN"

    private const val PREF_KEY_INSTRUCTION_RISK_DETAIL = "PREF_KEY_INSTRUCTION_RISK_DETAIL"
    private const val PREF_KEY_INSTRUCTION_RISK_DETAIL_SPLIT_MARK = "PREF_KEY_INSTRUCTION_RISK_DETAIL_SPLIT_MARK"
    private const val PREF_KEY_INSTRUCTION_CANCEL_ALARM = "PREF_KEY_INSTRUCTION_CANCEL_ALARM"

    private const val PREF_KEY_FIRST_LAUNCH_TIME = "PREF_KEY_FIRST_LAUNCH_TIME"
    private const val PREF_KEY_ACCUMULATED_DISABLE_TIME = "PREF_KEY_ACCUMULATED_DISABLE_TIME"
    private const val PREF_KEY_LAST_DISABLE_TIME = "PREF_KEY_LAST_DISABLE_TIME"
    private const val PREF_KEY_LAST_CHECK_TIME = "PREF_KEY_LAST_CHECK_TIME"
    private const val PREF_KEY_LAST_CHECK_UPDATE_TIME = "PREF_KEY_LAST_CHECK_UPDATE_TIME"
    private const val PREF_KEY_LAST_ENTER_MAIN_PAGE_TIME = "PREF_KEY_LAST_ENTER_MAIN_PAGE_TIME"

    private const val PREF_KEY_REVISION_TOKEN = "PREF_KEY_REVISION_TOKEN"
    private const val PREF_KEY_MOST_RECENT_SUCCESSFUL_DOWNLOAD = "PREF_KEY_MOST_RECENT_SUCCESSFUL_DOWNLOAD"

    private const val PREF_KEY_SAFE_SUMMARY = "PREF_KEY_SAFE_SUMMARY"

    private const val PREF_KEY_NOT_FOUND_NOTIFICATION_ENABLED = "PREF_KEY_NOT_FOUND_NOTIFICATION_ENABLED"
    private const val PREF_KEY_NOT_FOUND_NOTIFICATION_TIME = "PREF_KEY_NOT_FOUND_NOTIFICATION_TIME"
    private const val PREF_KEY_STATE_UPDATED_NOTIFICATION_TIME = "PREF_KEY_STATE_UPDATED_NOTIFICATION_TIME"

    private const val PREF_KEY_PRESENTED_FEATURES = "PREF_KEY_INTRODUCTION_HISTORY"

    private const val PREF_KEY_GET_CODE_COUNT = "PREF_KEY_GET_CODE_COUNT"
    private const val PREF_KEY_LAST_GET_CODE_TIME = "PREF_KEY_LAST_GET_CODE_TIME"

    private const val PREF_KEY_HCERT = "PREF_KEY_HCERT"

    private val sharedPreferences =
        BaseApplication.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var riskDetailInstruction: String
        get() {
            return sharedPreferences.getString(PREF_KEY_INSTRUCTION_RISK_DETAIL, "") ?: ""
        }
        set(value) {
            sharedPreferences.edit {
                putString(PREF_KEY_INSTRUCTION_RISK_DETAIL, value)
            }
        }

    var riskDetailInstructionSplitMark: String
        get() {
            return sharedPreferences.getString(PREF_KEY_INSTRUCTION_RISK_DETAIL_SPLIT_MARK, "") ?: ""
        }
        set(value) {
            sharedPreferences.edit {
                putString(PREF_KEY_INSTRUCTION_RISK_DETAIL_SPLIT_MARK, value)
            }
        }

    var cancelAlarmInstruction: String
        get() {
            return sharedPreferences.getString(PREF_KEY_INSTRUCTION_CANCEL_ALARM, "") ?: ""
        }
        set(value) {
            sharedPreferences.edit {
                putString(PREF_KEY_INSTRUCTION_CANCEL_ALARM, value)
            }
        }

    var lastEnterMainPageTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_LAST_ENTER_MAIN_PAGE_TIME, 0L)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_LAST_ENTER_MAIN_PAGE_TIME, value)
            }
        }

    var lastStateUpdatedNotificationTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_STATE_UPDATED_NOTIFICATION_TIME, Long.MAX_VALUE)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_STATE_UPDATED_NOTIFICATION_TIME, value)
            }
        }

    var lastNotFoundNotificationTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_NOT_FOUND_NOTIFICATION_TIME, 0L)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_NOT_FOUND_NOTIFICATION_TIME, value)
            }
        }

    var isNotFoundNotificationEnabled: Boolean
        get() {
            return sharedPreferences.getBoolean(PREF_KEY_NOT_FOUND_NOTIFICATION_ENABLED, true)
        }
        set(value) {
            sharedPreferences.edit {
                putBoolean(PREF_KEY_NOT_FOUND_NOTIFICATION_ENABLED, value)
            }
        }

    var lastCheckTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_LAST_CHECK_TIME, 0L)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_LAST_CHECK_TIME, value)
            }
        }

    var lastCheckUpdateTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_LAST_CHECK_UPDATE_TIME, 0L)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_LAST_CHECK_UPDATE_TIME, value)
            }
        }

    var revisionToken: String
        get() {
            return sharedPreferences.getString(PREF_KEY_REVISION_TOKEN, "") ?: ""
        }
        set(value) {
            sharedPreferences.edit {
                putString(PREF_KEY_REVISION_TOKEN, value)
            }
        }

    var mostRecentSuccessfulDownload: String
        get() {
            return sharedPreferences.getString(PREF_KEY_MOST_RECENT_SUCCESSFUL_DOWNLOAD, "") ?: ""
        }
        set(value) {
            sharedPreferences.edit {
                putString(PREF_KEY_MOST_RECENT_SUCCESSFUL_DOWNLOAD, value)
            }
        }

    val isFirstTimeEnableNeeded: Boolean
        get() {
            return firstEnabledTime == 0L
        }

    var firstEnabledTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_FIRST_LAUNCH_TIME, 0L)
        }
        set(value) {
            if (isFirstTimeEnableNeeded) {
                sharedPreferences.edit {
                    putLong(PREF_KEY_FIRST_LAUNCH_TIME, value)
                }
            }
        }

    var lastDisableTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_LAST_DISABLE_TIME, 0L)
        }
        set(value) {
            if (lastDisableTime == 0L) {
                sharedPreferences.edit {
                    putLong(PREF_KEY_LAST_DISABLE_TIME, value)
                }
            }
        }

    var accumulatedDisableTime: Long
        get() {
            val currentDisableDurationTime = if (lastDisableTime == 0L) {
                0L
            } else {
                Date().time - maxOf(lastDisableTime, firstEnabledTime)
            }
            return currentDisableDurationTime +
                sharedPreferences.getLong(PREF_KEY_ACCUMULATED_DISABLE_TIME, 0L)
        }
        private set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_ACCUMULATED_DISABLE_TIME, value)
            }
        }

    fun clearLastDisableTime() {
        val nowTime = Date().time

        firstEnabledTime = nowTime

        if (lastDisableTime != 0L) {
            accumulatedDisableTime = nowTime - maxOf(lastDisableTime, firstEnabledTime) +
                                     // Don't use accumulatedDisableTime directly
                                     sharedPreferences.getLong(PREF_KEY_ACCUMULATED_DISABLE_TIME, 0L)

            sharedPreferences.edit {
                putLong(PREF_KEY_LAST_DISABLE_TIME, 0L)
            }
        }
    }

    var safeSummaries: Map<DaysSinceEpoch, WeightedDurationSum>
        get() {
            return sharedPreferences.getStringSet(PREF_KEY_SAFE_SUMMARY, setOf())?.map {
                val data = it.split(",")
                data[0].toInt() to data[1].toDouble()
            }?.toMap() ?: mapOf()
        }
        set(value) {
            sharedPreferences.edit {
                putStringSet(PREF_KEY_SAFE_SUMMARY, value.map {
                    "${it.key},${it.value}"
                }.toSet())
            }
        }

    var presentedFeatures: Set<String>
        get() {
            return sharedPreferences.getStringSet(PREF_KEY_PRESENTED_FEATURES, setOf())!!.toSet()
        }
        set(value) {
            sharedPreferences.edit {
                putStringSet(PREF_KEY_PRESENTED_FEATURES, value)
            }
        }

    var requestCodeCount: Int
        get() {
            return sharedPreferences.getInt(PREF_KEY_GET_CODE_COUNT, 0)
        }
        set(value) {
            sharedPreferences.edit {
                putInt(PREF_KEY_GET_CODE_COUNT, value)
            }
        }

    var lastRequestCodeTime: Long
        get() {
            return sharedPreferences.getLong(PREF_KEY_LAST_GET_CODE_TIME, 0L)
        }
        set(value) {
            sharedPreferences.edit {
                putLong(PREF_KEY_LAST_GET_CODE_TIME, value)
            }
        }

    var hcerts: Set<String>
        get() {
            return sharedPreferences.getStringSet(PREF_KEY_HCERT, setOf())!!.toSet()
        }
        set(value) {
            sharedPreferences.edit {
                putStringSet(PREF_KEY_HCERT, value)
            }
        }

    /**
     * Cloud Config
     */

    private const val PREF_KEY_CONFIG_ALARM_PERIOD = "PREF_KEY_CONFIG_ALARM_PERIOD"

    fun updateCloudConfig() {
        APIService.verificationServer.cloudConfig()
            .enqueue(object : Callback<ConfigResponse> {
                override fun onResponse(call: Call<ConfigResponse>,
                                        response: Response<ConfigResponse>
                ) {
                    Log.i(TAG, "updateCloudConfig onResponse $response")
                    response.body()?.let {
                        riskAlarmPeriod = it.riskAlarmPeriod
                    }
                }

                override fun onFailure(call: Call<ConfigResponse>, t: Throwable) {
                    Log.e(TAG, "updateCloudConfig onFailure $t")
                }
            })
    }

    var riskAlarmPeriod: Int
        get() {
            return sharedPreferences.getInt(PREF_KEY_CONFIG_ALARM_PERIOD, 7)
        }
        private set(value) {
            sharedPreferences.edit {
                putInt(PREF_KEY_CONFIG_ALARM_PERIOD, value)
            }
        }
}

typealias DaysSinceEpoch = Int
typealias WeightedDurationSum = Double