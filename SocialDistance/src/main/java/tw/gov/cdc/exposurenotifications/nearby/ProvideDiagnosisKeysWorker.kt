package tw.gov.cdc.exposurenotifications.nearby

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.common.base.Splitter
import com.google.common.collect.ImmutableList
import com.google.common.collect.Iterables
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.apache.commons.io.FileUtils
import retrofit2.await
import tw.gov.cdc.exposurenotifications.api.APIService
import tw.gov.cdc.exposurenotifications.api.DownloadInterface
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.common.Utils.toSpecificTime
import tw.gov.cdc.exposurenotifications.data.InstructionRepository
import tw.gov.cdc.exposurenotifications.keydownload.KeyFile
import tw.gov.cdc.exposurenotifications.nearby.NotificationHelper.NotificationType.ProvideDiagnosisKeys
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ProvideDiagnosisKeysWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val TAG = "ProvideDiagnosisKeysWorker"
        private val WHITESPACE_SPLITTER = Splitter.onPattern("\\s+").trimResults().omitEmptyStrings()
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork")
        ExposureNotificationManager.updateStatus(context)
        return try {
            withTimeout(TimeUnit.MINUTES.toMillis(10)) {
                val isNearbyAPIEnabled = ExposureNotificationClientWrapper.isEnabled.await() as? Boolean ?: false
                if (isNearbyAPIEnabled && !isStopped) {
                    
                    startForegroundConditionally()

                    InstructionRepository.updateInstruction()

                    Log.d(TAG, "getIndexFile()")
                    val indexContent = APIService.downloadTEKs.getIndexFile().await().string()
                    val keyFiles = handleIndexFile(indexContent = indexContent)

                    val downloadedFiles: MutableList<KeyFile> = ArrayList()
                    val folder = File(context.filesDir, "/diag_keys/${DownloadInterface.FOLDER_NAME}")
                    if (folder.isDirectory) {
                        downloadedFiles.addAll(folder.listFiles().map {
                            Log.d(TAG, "Add existing file ${it.name}")
                            KeyFile.create(it.name, false).with(it)
                        })
                    }

                    Log.d(TAG, "Start download ${keyFiles.size} files")
                    for (file in keyFiles) {
                        val path = "/diag_keys/${file.indexEntry()}"
                        val toFile = File(context.filesDir, path)
                        val content = APIService.downloadTEKs.downloadFile(file.indexEntry()).await()
                        FileUtils.writeByteArrayToFile(toFile, content.bytes())
                        downloadedFiles.add(file.with(toFile))
                        PreferenceManager.mostRecentSuccessfulDownload = file.indexEntry()
                    }

                    if (downloadedFiles.isNotEmpty()) {
                        DiagnosisKeyFileHelper.submitFiles(ImmutableList.copyOf(downloadedFiles)).await()
                    } else {
                        Log.d(TAG, "No files to provide to google play services.")
                    }

                    // FIXME: CDC reports that sometimes ExposureStateUpdated is not showing until users open the app.
                    delay(2000) // Wait 2s to get the newest DailySummaries.
                    ExposureNotificationManager.updateDailySummaries(context)
                    scheduleNotFoundNotificationIfNeeded()
                    delay(1000) // Prevent success before updateDailySummaries done.

                    Log.d(TAG, "doWork success")
                    PreferenceManager.lastCheckTime = Date().time
                    Result.success()
                } else {
                    Log.d(TAG, "doWork failure")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "doWork runAttemptCount $runAttemptCount exception $e")
            if (runAttemptCount < 3) {
                Log.d(TAG, "doWork retry")
                Result.retry()
            } else {
                Log.d(TAG, "doWork failure")
                Result.failure()
            }
        }
    }

    private suspend fun startForegroundConditionally() {
        val isInValidTime = Calendar.getInstance().let {
            val now = it.timeInMillis
            val startTime = it.toSpecificTime(hour = 18).timeInMillis
            val endTime = it.toSpecificTime(hour = 22, minute = 1).timeInMillis

            now in startTime..endTime
        }
        if (isInValidTime) {
            NotificationHelper.generateNotification(
                type = ProvideDiagnosisKeys,
                context = context
            ).let {
                setForeground(ForegroundInfo(it.notificationId, it.notification))
            }
        }
    }

    private fun handleIndexFile(indexContent: String): List<KeyFile> {
        val indexEntries: List<String> = WHITESPACE_SPLITTER.splitToList(indexContent).let {
            Log.d(TAG, "Index file has ${it.size} lines.")
            it.subList(it.indexOf(PreferenceManager.mostRecentSuccessfulDownload) + 1, it.size)
        }

        val builder = ImmutableList.builder<KeyFile>()
        for (indexEntry in indexEntries) {
            val isMostRecent = indexEntry == Iterables.getLast(indexEntries)
            builder.add(KeyFile.create(indexEntry, isMostRecent))
        }

        return builder.build()
    }

    private fun scheduleNotFoundNotificationIfNeeded() {
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 18) {
            val intent = Intent(context, ExposureNotificationBroadcastReceiver::class.java).apply {
                action = ExposureNotificationBroadcastReceiver.ACTION_SCHEDULED_NOT_FOUND_NOTIFICATION
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                RequestCode.REQUEST_INTENT_NOT_FOUND_NOTIFICATION,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().toSpecificTime(hour = 22).timeInMillis, pendingIntent)
        }
    }
}
