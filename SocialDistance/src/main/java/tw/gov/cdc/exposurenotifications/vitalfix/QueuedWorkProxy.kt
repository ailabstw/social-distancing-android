package tw.gov.cdc.exposurenotifications.vitalfix

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Source: https://github.com/kswlee/android-vital-fix
 */

object QueuedWorkProxy {
    private var pendingWorks: ConcurrentLinkedQueue<Runnable>? = null

    fun consumePendingWorks() {
        if (null == pendingWorks) {
            pendingWorks = Reflection.getClassMember("android.app.QueuedWork",
                "sPendingWorkFinishers", null) as? ConcurrentLinkedQueue<Runnable>
        }

        pendingWorks?.apply {
            clear()
        }
    }
}