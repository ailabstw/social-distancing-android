package tw.gov.cdc.exposurenotifications

import android.app.Application
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.hcert.data.HcertRepository
import tw.gov.cdc.exposurenotifications.data.InstructionRepository
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.WorkScheduler
import tw.gov.cdc.exposurenotifications.vitalfix.VitalFixer
import java.util.*

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        ExposureNotificationManager.updateStatus(this)
        ExposureNotificationManager.state.observeForever {
            if (it != ExposureNotificationManager.ExposureNotificationState.Disabled) {
                val now = Date().time
                val lastCheck = PreferenceManager.lastCheckTime
                val dayInMillis = 1000L * 60L * 60L * 24L
                if (now - lastCheck > dayInMillis) {
                    WorkScheduler.schedule(ExistingPeriodicWorkPolicy.REPLACE)
                } else {
                    WorkScheduler.schedule(ExistingPeriodicWorkPolicy.KEEP)
                }
            }
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        VitalFixer.Builder()
            .remoteServiceException() // auto fix remote service exception
            .fix()
    }

    companion object {
        lateinit var instance: BaseApplication
            private set
    }

    // TODO: Consider a Dependency Injection framework.
    val hcertRepository: HcertRepository by lazy { HcertRepository() }
}