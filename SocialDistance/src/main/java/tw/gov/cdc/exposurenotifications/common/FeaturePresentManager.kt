package tw.gov.cdc.exposurenotifications.common

object FeaturePresentManager {

    enum class Feature {
        BARCODE,
        DAILY_SUMMARY
    }

    val featuresNeedToPresent: Set<Feature>
        get() {
            val presentedFeatures = PreferenceManager.presentedFeatures
            return Feature.values().filter {
                !presentedFeatures.contains(it.name)
            }.toSet()
        }

    fun setPresented(features: Set<Feature>) {
        val presentedFeatures = PreferenceManager.presentedFeatures.toMutableSet()
        presentedFeatures.addAll(features.map { it.name })
        PreferenceManager.presentedFeatures = presentedFeatures
    }
}
