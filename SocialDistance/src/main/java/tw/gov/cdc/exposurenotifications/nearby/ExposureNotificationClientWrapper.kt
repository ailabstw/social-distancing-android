package tw.gov.cdc.exposurenotifications.nearby

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.*
import com.google.android.gms.tasks.Task
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.common.Log
import java.io.File

object ExposureNotificationClientWrapper {

    private const val TAG = "ENCWrapper"

    private val exposureNotificationClient by lazy {
        Nearby.getExposureNotificationClient(BaseApplication.instance)
    }

    fun start(): Task<Void> {
        return exposureNotificationClient.start()
            .addOnSuccessListener {
                Log.d(TAG, "start Success")
            }
            .addOnFailureListener {
                Log.e(TAG, "start Failure $it")
            }
    }

    fun stop(): Task<Void> {
        return exposureNotificationClient.stop()
            .addOnSuccessListener {
                Log.d(TAG, "stop Success")
            }
            .addOnFailureListener {
                Log.e(TAG, "stop Failure $it")
            }
    }

    val isEnabled: Task<Boolean>
        get() {
            return exposureNotificationClient.isEnabled
                .addOnSuccessListener {
                    Log.d(TAG, "isEnabled Success $it")
                }
                .addOnFailureListener {
                    Log.e(TAG, "isEnabled Failure $it")
                }
        }

    val status: Task<Set<ExposureNotificationStatus>>
        get() {
            return exposureNotificationClient.status
                .addOnSuccessListener {
                    Log.d(TAG, "status Success $it")
                }
                .addOnFailureListener {
                    Log.e(TAG, "status Failure $it")
                }
        }

    fun provideDiagnosisKeys(files: List<File>): Task<Void> {
        return exposureNotificationClient.provideDiagnosisKeys(files)
            .addOnSuccessListener {
                Log.d(TAG, "provideDiagnosisKeys Success")
                files.forEach { file ->
                    file.delete()
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "provideDiagnosisKeys Failure $it")
            }
    }

    fun setDiagnosisKeysDataMapping(diagnosisKeysDataMapping: DiagnosisKeysDataMapping): Task<Void> {
        return exposureNotificationClient.setDiagnosisKeysDataMapping(diagnosisKeysDataMapping)
            .addOnSuccessListener {
                Log.d(TAG, "setDiagnosisKeysDataMapping Success $it")
            }
            .addOnFailureListener {
                Log.e(TAG, "setDiagnosisKeysDataMapping Failure $it")
            }
    }

    val diagnosisKeysDataMapping: Task<DiagnosisKeysDataMapping>
        get() {
            return exposureNotificationClient.diagnosisKeysDataMapping
                .addOnSuccessListener {
                    Log.d(TAG, "diagnosisKeysDataMapping $it")
                }
                .addOnFailureListener {
                    Log.e(TAG, "diagnosisKeysDataMapping Failure $it")
                }
        }

    fun getDailySummaries(dailySummariesConfig: DailySummariesConfig): Task<List<DailySummary>> {
        return exposureNotificationClient.getDailySummaries(dailySummariesConfig)
            .addOnSuccessListener {
                if (it.isEmpty()) {
                    Log.d(TAG, "getDailySummaries $it")
                } else {
                    Log.d(TAG, "getDailySummaries has ${it.size} summaries")
                    it.forEachIndexed { index, dailySummary ->
                        Log.d(TAG, "$index daysSinceEpoch ${dailySummary.daysSinceEpoch} ${dailySummary.summaryData}")
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "getDailySummaries Failure $it")
            }
    }

    val exposureWindows: Task<List<ExposureWindow>>
        get() {
            return exposureNotificationClient.exposureWindows
                .addOnSuccessListener {
                    if (it.isEmpty()) {
                        Log.d(TAG, "exposureWindows Success but is empty")
                    } else {
                        it.forEach { exposureWindow ->
                            Log.d(TAG, "exposureWindows $exposureWindow")
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "exposureWindows Failure $it")
                }
        }

    val temporaryExposureKeyHistory: Task<List<TemporaryExposureKey>>
        get() {
            return exposureNotificationClient.temporaryExposureKeyHistory
                .addOnSuccessListener {
                    it.forEachIndexed { index, temporaryExposureKey ->
                        Log.d(TAG, "temporaryExposureKeyHistory $index $temporaryExposureKey")
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "temporaryExposureKeyHistory Failure $it")
                }
        }
}