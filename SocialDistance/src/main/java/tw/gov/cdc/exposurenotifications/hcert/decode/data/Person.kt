package tw.gov.cdc.exposurenotifications.hcert.decode.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Person(
    @SerialName("fn")
    val familyName: String? = null,

    @SerialName("fnt")
    val familyNameTransliterated: String,

    @SerialName("gn")
    val givenName: String? = null,

    @SerialName("gnt")
    val givenNameTransliterated: String? = null,
)
