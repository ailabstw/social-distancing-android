package tw.gov.cdc.exposurenotifications.hcert.ui

data class HcertModel(
    val name: String,
    val nameTransliterated: String,
    val dateOfBirth: String,
    val targetDisease: String,
    val vaccine: String,
    val medicinalProduct: String,
    val authorizationHolder: String,
    val doseState: String,
    val dateOfVaccination: String,
    val country: String,
    val certificateIssuer: String,
    val certificateIdentifier: String,
    val rawString: String
)
