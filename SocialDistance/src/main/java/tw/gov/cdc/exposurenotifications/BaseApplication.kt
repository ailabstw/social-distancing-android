package tw.gov.cdc.exposurenotifications

import android.app.Application
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.WorkScheduler

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        ExposureNotificationManager.updateStatus(this)
        ExposureNotificationManager.state.observeForever {
            if (it != ExposureNotificationManager.ExposureNotificationState.Disabled) {
                WorkScheduler.schedule()
            }
        }
    }

    companion object {
        lateinit var instance: BaseApplication
            private set
    }
}