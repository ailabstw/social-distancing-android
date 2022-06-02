package tw.gov.cdc.exposurenotifications.nearby

import androidx.work.*
import tw.gov.cdc.exposurenotifications.BaseApplication
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val PROVIDE_KEY_WORK = "PROVIDE_KEY_WORK"

    /**
     * The app must ensure that the jobs are enqueued properly in the following situations:
     *
     *  1. When the application starts and the ExposureNotification API is enabled for your app.
     *  2. When the start() method succeeds after the user authorizes your app.
     *  3. When the ACTION_SERVICE_STATE_UPDATED broadcast is received with EXTRA_SERVICE_STATE equal to true.
     *
     * At the same time, you should stop any enqueued job when the Exposure Notification API is disabled:
     *
     *  1. When calling stop() (such as when the user toggles a switch in your app).
     *  2. When the ACTION_SERVICE_STATE_UPDATED broadcast is received with EXTRA_SERVICE_STATE equal to false.
     */

    fun schedule(policy: ExistingPeriodicWorkPolicy) {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<ProvideDiagnosisKeysWorker>(4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 16, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(BaseApplication.instance)
            // Use a unique work to avoid multiple workers.
            .enqueueUniquePeriodicWork(
                PROVIDE_KEY_WORK,
                policy,
                workRequest
            )
    }
}