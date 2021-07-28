package tw.gov.cdc.exposurenotifications.vitalfix

/**
 * Source: https://github.com/kswlee/android-vital-fix
 */

class VitalFixer {
    private class FixParams {
        var fixRemoteServiceException = false
        var fixSharePrefANRByService = false
        var fixSharePrefANRByActivity = false

        fun applyFix() {
            if (fixRemoteServiceException) {
                ActivityThreadHooker.setupScheduleCrashHandler()
            }

            if (fixSharePrefANRByService) {
                ActivityThreadHooker.setupServiceArgsHandler()
            }

            if (fixSharePrefANRByActivity) {
                ActivityThreadHooker.setupActivityArgsHandler()
            }

            ActivityThreadHooker.conditionalHook()
        }
    }

    class Builder {
        private val params = FixParams()

        fun remoteServiceException(): Builder {
            params.fixRemoteServiceException = true
            return this
        }

        fun sharePrefANRByService(): Builder {
            params.fixSharePrefANRByService = true
            return this
        }

        fun sharePrefANRByActivity(): Builder {
            params.fixSharePrefANRByActivity = true
            return this
        }

        fun fix() {
            params.applyFix()
        }
    }
}