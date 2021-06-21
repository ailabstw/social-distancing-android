package tw.gov.cdc.exposurenotifications.nearby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import tw.gov.cdc.exposurenotifications.nearby.NotificationHelper.NotificationType.ExposureStateUpdated
import tw.gov.cdc.exposurenotifications.nearby.NotificationHelper.NotificationType.ServiceStateUpdated

class ExposureNotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ENBroadcastReceiver"

        const val ACTION_SCHEDULED_NOT_FOUND_NOTIFICATION = "ACTION_SCHEDULED_NOT_FOUND_NOTIFICATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive $intent ${intent.action}")

        val action = intent.action ?: return

        when (action) {
            ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND -> {
                // Post notification in updateDailySummaries
                ExposureNotificationManager.updateDailySummaries(context)
                // Not proper to post ExposureNotFound here, because
                // ACTION_EXPOSURE_NOT_FOUND is received when no exposure of "new" provided TEKs,
                // even if there are exposure found in previous provided TEKs.
                // NotificationHelper.postNotification(ExposureNotFound, context)
            }
            ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED -> {
                ExposureNotificationManager.updateDailySummaries(context)
                NotificationHelper.postNotification(ExposureStateUpdated, context)
            }
            ExposureNotificationClient.ACTION_SERVICE_STATE_UPDATED -> {
                ExposureNotificationManager.updateStatus(context)
                intent.takeIf {
                    it.hasExtra(ExposureNotificationClient.EXTRA_SERVICE_STATE)
                }?.let {
                    val isEnabled = it.getBooleanExtra(ExposureNotificationClient.EXTRA_SERVICE_STATE, false)
                    NotificationHelper.postNotification(ServiceStateUpdated(isEnabled), context)
                }
            }
            ACTION_SCHEDULED_NOT_FOUND_NOTIFICATION -> {
                ExposureNotificationManager.updateDailySummaries(context)
            }
            else -> {
            }
        }
    }
}
