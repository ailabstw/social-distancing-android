package tw.gov.cdc.exposurenotifications

import android.app.Application
import android.content.Context
import tw.gov.cdc.exposurenotifications.hcert.data.HcertRepository
import tw.gov.cdc.exposurenotifications.data.InstructionRepository
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.WorkScheduler
import tw.gov.cdc.exposurenotifications.vitalfix.VitalFixer

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