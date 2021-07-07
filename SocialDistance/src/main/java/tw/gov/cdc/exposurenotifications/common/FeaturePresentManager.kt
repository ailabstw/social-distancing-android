package tw.gov.cdc.exposurenotifications.common

object FeaturePresentManager {

    enum class Feature {
        // BARCODE, // Shortcut
        BARCODE_V2, // Shortcut + Widget + Tile
        DAILY_SUMMARY
    }

    private val featuresNeedToPresent: Set<Feature>
        get() {
            val presentedFeatures = PreferenceManager.presentedFeatures
            return Feature.values().filter {
                !presentedFeatures.contains(it.name)
            }.toSet()
        }

    fun getFeaturesNeedToPresent(features: List<Feature>): List<Feature> {
        val allFeatures = featuresNeedToPresent
        return features.filter {
            allFeatures.contains(it)
        }
    }

    fun setPresented(features: Set<Feature>) {
        val presentedFeatures = PreferenceManager.presentedFeatures.toMutableSet()
        presentedFeatures.addAll(features.map { it.name })
        PreferenceManager.presentedFeatures = presentedFeatures
    }

    fun resetPresented() {
        PreferenceManager.presentedFeatures = setOf()
    }
}
