package tw.gov.cdc.exposurenotifications.nearby

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.MainActivity
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.common.Utils.toSpecificTime
import java.util.*

object NotificationHelper {
    private const val TAG = "NotificationHelper"

    private const val NOTIFICATION_CHANNEL_ID = "TAIWAN_NEAR_BY_CHANNEL"
    private const val NOTIFICATION_CHANNEL_ID_FOREGROUND = "NB_CHANNEL_FOREGROUND"

    fun postNotification(type: NotificationType, context: Context, force: Boolean = false) {
        if (force || shouldPostNotification(type = type)) {
            generateNotification(type = type, context = context).let {
                Log.d(TAG, "postNotification $type")
                when (type) {
                    NotificationType.ExposureNotFound -> {
                        PreferenceManager.lastNotFoundNotificationTime = Calendar.getInstance().timeInMillis
                    }
                    NotificationType.ExposureStateUpdated -> {
                        PreferenceManager.lastStateUpdatedNotificationTime = Calendar.getInstance().timeInMillis
                    }
                    else -> {}
                }
                NotificationManagerCompat.from(context).notify(it.notificationId, it.notification)
            }
        } else if (type is NotificationType.ExposureNotFound) {
            NotificationManagerCompat.from(context).cancel(type.notificationId)
            NotificationManagerCompat.from(context).cancel(NotificationType.ServiceStateUpdated(false).notificationId)
        }
    }

    fun generateNotification(type: NotificationType, context: Context): NotificationInfo {
        Log.d(TAG, "generateNotification $type")

        createNotificationChannelIfNeeded(type.channelInfo, context)

        val contentTitle = context.getString(type.contentTitle)
        val contentText = context.getString(type.contentText)
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .setBigContentTitle(contentTitle)
            .bigText(contentText)

        val launchAppIntent = PendingIntent.getActivity(
            context,
            RequestCode.REQUEST_INTENT_LAUNCH_APP,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, type.channelInfo.channelId)
            .setStyle(bigTextStyle)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setColor(ContextCompat.getColor(context, R.color.primary))
            .setSmallIcon(R.drawable.ic_notification)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(type.priority != NotificationCompat.PRIORITY_MAX)
            .setOngoing(type.priority == NotificationCompat.PRIORITY_MAX)
            .setPriority(type.priority)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(launchAppIntent)

        return NotificationInfo(
            notificationId = type.notificationId,
            notification = notificationBuilder.build()
        )
    }

    private fun shouldPostNotification(type: NotificationType): Boolean {
        return when (type) {
            NotificationType.ExposureNotFound -> {
                PreferenceManager.isNotFoundNotificationEnabled
                && ExposureNotificationManager.state.value == ExposureNotificationManager.ExposureNotificationState.Enabled
                && Calendar.getInstance().let {
                    val now = it.timeInMillis
                    val startTime = it.toSpecificTime(hour = 18).timeInMillis
                    val endTime = it.toSpecificTime(hour = 22, minute = 1).timeInMillis

                    now in startTime..endTime
                    && PreferenceManager.lastNotFoundNotificationTime !in startTime..endTime
                }
            }
            NotificationType.ExposureStateUpdated -> {
                val now = Date().time

                kotlin.math.abs(now - PreferenceManager.lastStateUpdatedNotificationTime) > 2000
                && PreferenceManager.lastEnterMainPageTime <= PreferenceManager.lastStateUpdatedNotificationTime
            }
            else -> true
        }
    }

    private fun createNotificationChannelIfNeeded(channelInfo: NotificationChannelInfo,
                                                  context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelInfo.channelId,
                                              context.getString(channelInfo.channelName),
                                              channelInfo.importance)
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }
    }

    data class NotificationInfo(
        val notificationId: Int,
        val notification: Notification)

    data class NotificationChannelInfo(
        val channelId: String,
        @StringRes val channelName: Int,
        val importance: Int)

    sealed class NotificationType {

        object ProvideDiagnosisKeys : NotificationType()

        object ExposureNotFound : NotificationType()

        object ExposureStateUpdated : NotificationType()

        data class ServiceStateUpdated(val isEnabled: Boolean) : NotificationType()

        val contentTitle: Int
            @StringRes get() = when (this) {
                ProvideDiagnosisKeys -> R.string.notification_provide_diagnosis_keys_title
                ExposureNotFound -> R.string.notification_exposure_not_found_title
                ExposureStateUpdated -> R.string.notification_exposure_state_updated_title
                is ServiceStateUpdated -> R.string.notification_service_state_updated_title
            }

        val contentText: Int
            @StringRes get() = when (this) {
                ProvideDiagnosisKeys -> R.string.notification_provide_diagnosis_keys_message
                ExposureNotFound -> R.string.notification_exposure_not_found_message
                ExposureStateUpdated -> R.string.notification_exposure_state_updated_message
                is ServiceStateUpdated -> {
                    if (isEnabled) R.string.notification_service_state_updated_message_on
                    else R.string.notification_service_state_updated_message_off
                }
            }

        val priority: Int
            get() = when (this) {
                ProvideDiagnosisKeys -> NotificationCompat.PRIORITY_MIN
                ExposureNotFound -> NotificationCompat.PRIORITY_MIN
                ExposureStateUpdated -> NotificationCompat.PRIORITY_MAX
                is ServiceStateUpdated -> {
                    if (isEnabled) NotificationCompat.PRIORITY_DEFAULT
                    else NotificationCompat.PRIORITY_MAX
                }
            }

        val notificationId: Int
            get() = when (this) {
                ProvideDiagnosisKeys -> 19191919
                ExposureNotFound -> 18181818
                ExposureStateUpdated -> 18181818
                is ServiceStateUpdated -> 17171717
            }

        val channelInfo: NotificationChannelInfo
            @RequiresApi(Build.VERSION_CODES.N)
            get() = when (this) {
                ProvideDiagnosisKeys -> NotificationChannelInfo(
                    channelId = NOTIFICATION_CHANNEL_ID_FOREGROUND,
                    channelName = R.string.notification_channel_name_foreground_service,
                    importance = NotificationManager.IMPORTANCE_NONE)
                else -> NotificationChannelInfo(
                    channelId = NOTIFICATION_CHANNEL_ID,
                    channelName = R.string.notification_channel_name,
                    importance = NotificationManager.IMPORTANCE_DEFAULT)
            }
    }
}