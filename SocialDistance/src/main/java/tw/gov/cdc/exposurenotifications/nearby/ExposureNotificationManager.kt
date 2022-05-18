package tw.gov.cdc.exposurenotifications.nearby

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.*
import com.google.android.gms.nearby.exposurenotification.DailySummariesConfig.DailySummariesConfigBuilder
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.UploadActivity
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.common.Utils.daysSinceEpoch
import tw.gov.cdc.exposurenotifications.common.Utils.toBeginOfTheDay
import tw.gov.cdc.exposurenotifications.nearby.NotificationHelper.NotificationType.ExposureNotFound
import tw.gov.cdc.exposurenotifications.nearby.NotificationHelper.NotificationType.ExposureStateUpdated
import java.util.*

object ExposureNotificationManager {

    private const val TAG = "ExposureNotificationManager"

    private val pState = MutableLiveData<ExposureNotificationState>()

    val state: LiveData<ExposureNotificationState> = pState

    sealed class ExposureNotificationState {
        object Disabled : ExposureNotificationState()
        object Enabled : ExposureNotificationState()
        data class NotSupport(@StringRes val reason: Int) : ExposureNotificationState()
        object PausedBle : ExposureNotificationState()
        object PausedLocation : ExposureNotificationState()
        object PausedLocationBle : ExposureNotificationState()
        object StorageLow : ExposureNotificationState()
    }

    fun start(activity: Activity) {
        Log.d(TAG, "start $activity")
        ExposureNotificationClientWrapper.start()
            .addOnSuccessListener {
                updateStatus(true)
            }
            .addOnFailureListener {
                (it as? ApiException)?.status?.also { status ->
                    if (status.hasResolution()) {
                        status.startResolutionForResult(activity,
                            RequestCode.REQUEST_RESOLUTION_EN_CLIENT_START)
                    } else {
                        status.connectionResult?.let { connectionResult ->
                            handleStatus(connectionResult.errorCode)
                        }
                    }
                } ?: run {
                    Log.w(TAG, "exception !is ApiException")
                }
            }
            .addOnCanceledListener {
                Log.w("Nearby", "exposureNotificationClient.start() Canceled")
            }
    }

    fun stop(context: Context) {
        ExposureNotificationClientWrapper.stop()
            .addOnSuccessListener {
                updateStatus(context)
            }
            .addOnFailureListener {
                updateStatus(context)
            }
    }

    private var lastGoSettingTime = Date().time

    fun askManageStorageIfNeeded(context: Context): Boolean {
        if (pState.value == ExposureNotificationState.StorageLow
                && StorageManagementHelper.isStorageManagementAvailable(context)) {
            showManageStorageConfirmDialog(context)
            return true
        }

        return false
    }

    private fun showManageStorageConfirmDialog(context: Context) {
        AlertDialog.Builder(context)
                .setTitle(R.string.dialog_low_storage_title)
                .setMessage(R.string.dialog_low_storage_message)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    StorageManagementHelper.launchStorageManagement(context)
                }
                .setCancelable(false)
                .create()
                .show()
    }

    fun askTurningOnBluetoothOrLocationIfNeeded(activity: Activity): Boolean {

        if (Date().time - lastGoSettingTime < 500) {
            return false
        }

        if (pState.value == ExposureNotificationState.PausedLocation
            || pState.value == ExposureNotificationState.PausedBle
            || pState.value == ExposureNotificationState.PausedLocationBle) {

            lastGoSettingTime = Date().time

            activity.startActivity(Intent(ExposureNotificationClient.ACTION_EXPOSURE_NOTIFICATION_SETTINGS))
            return true
        }

        return false
    }

    fun getTemporaryExposureKeyHistory(activity: Activity) {
        Log.d(TAG, "getTemporaryExposureKeyHistory $activity")
        ExposureNotificationClientWrapper.temporaryExposureKeyHistory
            .addOnSuccessListener {
                // Keys were successfully retrieved. Call your own method to handle the upload.
                (activity as? UploadActivity)?.onReceivedKeys(it)
            }
            .addOnFailureListener {
                (it as? ApiException)?.status?.also { status ->
                    if (status.hasResolution()) {
                        status.startResolutionForResult(activity,
                            RequestCode.REQUEST_RESOLUTION_EN_TEK_HISTORY)
                    } else {
                        status.connectionResult?.let { connectionResult ->
                            handleStatus(connectionResult.errorCode)
                        }
                    }
                } ?: run {
                    Log.w(TAG, "exception !is ApiException")
                }
            }
    }

    private val pDailySummaries = MutableLiveData<List<DailySummary>>().apply {
        value = listOf()
    }

    val dailySummaries: LiveData<List<DailySummary>> = pDailySummaries

    val isInRisk: Boolean
        get() {
            val safeSummaries = PreferenceManager.safeSummaries
            val alarmPeriod = PreferenceManager.riskAlarmPeriod.let {
                val today = Calendar.getInstance().daysSinceEpoch()
                today - it..today
            }

            Log.d(TAG, "isInRisk safeSummaries $safeSummaries")
            pDailySummaries.value?.forEach { dailySummary ->
                val weightedDurationSum = dailySummary.summaryData.weightedDurationSum
                val safeSum = safeSummaries[dailySummary.daysSinceEpoch] ?: 0.0
                // Only shows an alert when exposed more than 2 minutes and within alarm period.
                if (dailySummary.daysSinceEpoch in alarmPeriod && weightedDurationSum - safeSum >= 120.0) {
                    return true
                }
            }
            PreferenceManager.lastStateUpdatedNotificationTime = Long.MAX_VALUE
            return false
        }

    fun updateSafeSummaries(safeDate: Calendar) {
        val safeDaysSinceEpoch = safeDate.toBeginOfTheDay().let {
            Log.d(TAG, "timeInMillis ${it.timeInMillis} rawOffset ${it.timeZone.rawOffset}")
            (it.timeInMillis + it.timeZone.rawOffset) / 1000 / 60 / 60 / 24
        }
        Log.d(TAG, "updateSafeSummaries safeDaysSinceEpoch $safeDaysSinceEpoch")

        pDailySummaries.value?.filter {
            it.daysSinceEpoch <= safeDaysSinceEpoch
        }?.map {
            it.daysSinceEpoch to it.summaryData.weightedDurationSum
        }?.toMap()?.let {
            PreferenceManager.safeSummaries = it
        }
    }

    fun updateDailySummaries(context: Context) {
        ExposureNotificationClientWrapper.getDailySummaries(dailySummariesConfig)
            .addOnSuccessListener {
                pDailySummaries.apply {
                    value = it
                }
                NotificationHelper.postNotification(if (isInRisk) ExposureStateUpdated else ExposureNotFound, context)
            }
    }

    private val diagnosisKeysDataMapping: DiagnosisKeysDataMapping by lazy {
        val daysToInfectiousness = mutableMapOf<Int, Int>()
        for (i in -14..14) {
            when (i) {
                in -5..-3 -> daysToInfectiousness[i] = Infectiousness.STANDARD
                in -2..5 -> daysToInfectiousness[i] = Infectiousness.STANDARD
                in 6..10 -> daysToInfectiousness[i] = Infectiousness.STANDARD
                else -> daysToInfectiousness[i] = Infectiousness.STANDARD
            }
        }
        DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder()
            .setDaysSinceOnsetToInfectiousness(daysToInfectiousness)
            .setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.STANDARD)
            .setReportTypeWhenMissing(ReportType.CONFIRMED_TEST)
            .build()
    }

    private val dailySummariesConfig: DailySummariesConfig by lazy {
        val res = BaseApplication.instance.resources
        /*
         * In the configuration, weights are provided in integer percent, while the API expects
         * double values. We use a double factor to avoid unnecessary casts.
         */
        val weightFactor = 0.01
        DailySummariesConfigBuilder()
            /*
             * Filtering: Remove windows with a score lower than x. This is not supplied by the
             * HA/configuration, so we don't set our own value and rather use defaults here.
             */
            .setMinimumWindowScore(0.0)
            .setDaysSinceExposureThreshold(res.getInteger(R.integer.enx_daysSinceExposureThreshold))
            .setAttenuationBuckets(
                listOf(
                    res.getInteger(R.integer.enx_attenuationImmediateNearThreshold),
                    res.getInteger(R.integer.enx_attenuationNearMedThreshold),
                    res.getInteger(R.integer.enx_attenuationMedFarThreshold)
                ),
                listOf(
                    res.getInteger(R.integer.enx_attenuationImmediateWeight) * weightFactor,
                    res.getInteger(R.integer.enx_attenuationNearWeight) * weightFactor,
                    res.getInteger(R.integer.enx_attenuationMedWeight) * weightFactor,
                    res.getInteger(R.integer.enx_attenuationOtherWeight) * weightFactor
                )
            )
            .setInfectiousnessWeight(
                Infectiousness.STANDARD,
                res.getInteger(R.integer.enx_infectiousnessStandardWeight) * weightFactor
            )
            .setInfectiousnessWeight(
                Infectiousness.HIGH,
                res.getInteger(R.integer.enx_infectiousnessHighWeight) * weightFactor
            )
            /*
             * There are two additional ReportTypes that we don't configure here:
             *  - ReportType.REVOKED is used to revoke keys (and thus does not need a weight)
             *  - ReportType.RECURSIVE which is not supported by the configuration tool (yet)
             */
            .setReportTypeWeight(
                ReportType.CONFIRMED_TEST,
                res.getInteger(R.integer.enx_reportTypeConfirmedTestWeight) * weightFactor
            )
            .setReportTypeWeight(
                ReportType.CONFIRMED_CLINICAL_DIAGNOSIS,
                res.getInteger(R.integer.enx_reportTypeConfirmedClinicalDiagnosisWeight) * weightFactor
            )
            .setReportTypeWeight(
                ReportType.SELF_REPORT,
                res.getInteger(R.integer.enx_reportTypeSelfReportWeight) * weightFactor
            )
            .build()
    }

    fun updateStatus(context: Context) {
        updateDailySummaries(context)
        ExposureNotificationClientWrapper.isEnabled
            .addOnSuccessListener {
                updateStatus(it)
            }
            .addOnFailureListener {
                updateStatus(false)
            }
    }

    private fun updateStatus(isEnabled: Boolean) {
        ExposureNotificationClientWrapper.status.addOnSuccessListener {
            pState.apply {
                val currentState = getStateForStatusAndIsEnabled(statusSet = it,
                                                                 isEnabled = isEnabled)
                if (value != currentState) {
                    if (currentState == ExposureNotificationState.Enabled) {
                        PreferenceManager.clearLastDisableTime()
                    } else {
                        PreferenceManager.lastDisableTime = Date().time
                    }
                    Log.d(TAG, "updateStatus to $currentState")
                    value = currentState
                }
            }
        }
    }

    // TODO: Error handling
    private fun handleStatus(errorCode: Int) {
        Log.w(TAG, "handleStatus ${ExposureNotificationStatusCodes.getStatusCodeString(errorCode)}")
        when (errorCode) {
            ExposureNotificationStatusCodes.FAILED_RATE_LIMITED -> {
            }
            ExposureNotificationStatusCodes.FAILED_UNAUTHORIZED -> {
            }
            ExposureNotificationStatusCodes.FAILED_SERVICE_DISABLED -> {
            }
            ExposureNotificationStatusCodes.FAILED_NOT_SUPPORTED -> {
            }
            ExposureNotificationStatusCodes.FAILED_BLUETOOTH_DISABLED -> {
            }
            else -> {
            }
        }
    }

    private fun getStateForStatusAndIsEnabled(
        statusSet: Set<ExposureNotificationStatus>,
        isEnabled: Boolean
    ): ExposureNotificationState {

        val getOtherState: () -> ExposureNotificationState = {
            when {
                statusSet.contains(ExposureNotificationStatus.EN_NOT_SUPPORT) -> {
                    ExposureNotificationState.NotSupport(R.string.state_en_not_support)
                }
                statusSet.contains(ExposureNotificationStatus.HW_NOT_SUPPORT) -> {
                    ExposureNotificationState.NotSupport(R.string.state_hw_not_support)
                }
                statusSet.contains(ExposureNotificationStatus.USER_PROFILE_NOT_SUPPORT) -> {
                    ExposureNotificationState.NotSupport(R.string.state_user_profile_not_support)
                }
                statusSet.contains(ExposureNotificationStatus.NOT_IN_ALLOWLIST) -> {
                    ExposureNotificationState.NotSupport(R.string.state_not_in_allowlist)
                }
                statusSet.contains(ExposureNotificationStatus.UNKNOWN) -> {
                    ExposureNotificationState.NotSupport(R.string.state_unknown)
                }
                else -> {
                    ExposureNotificationState.Disabled
                }
            }
        }

        return when {
            /**
             * The EN is disabled. However, if we also hit a Low Storage error, display it first, as EN
             * can only be (re-)enabled with enough storage space available.
             */
            !isEnabled -> {
                when {
                    statusSet.contains(ExposureNotificationStatus.LOW_STORAGE) -> {
                        ExposureNotificationState.StorageLow
                    }
                    else -> {
                        getOtherState()
                    }
                }
            }

            // The EN is enabled and operational.
            statusSet.contains(ExposureNotificationStatus.ACTIVATED) -> {
                ExposureNotificationState.Enabled
            }

            // The EN is enabled but non-operational.
            statusSet.contains(ExposureNotificationStatus.LOW_STORAGE) -> {
                ExposureNotificationState.StorageLow
            }
            statusSet.contains(ExposureNotificationStatus.BLUETOOTH_DISABLED)
                    && statusSet.contains(ExposureNotificationStatus.LOCATION_DISABLED) -> {
                ExposureNotificationState.PausedLocationBle
            }
            statusSet.contains(ExposureNotificationStatus.BLUETOOTH_DISABLED) -> {
                ExposureNotificationState.PausedBle
            }
            statusSet.contains(ExposureNotificationStatus.LOCATION_DISABLED) -> {
                ExposureNotificationState.PausedLocation
            }

            // For all the remaining scenarios, return the DISABLED state as this is the most suitable one
            // among those currently supported in the app.
            else -> {
                getOtherState()
            }
        }
    }

    init {
        Log.i(TAG, "init")
        ExposureNotificationClientWrapper.diagnosisKeysDataMapping
            .addOnSuccessListener {
                if (!it.equals(diagnosisKeysDataMapping)) {
                    Log.i(TAG, "Update diagnosisKeysDataMapping")
                    ExposureNotificationClientWrapper.setDiagnosisKeysDataMapping(
                        diagnosisKeysDataMapping)
                } else {
                    Log.i(TAG, "No need to update diagnosisKeysDataMapping")
                }
            }
    }
}