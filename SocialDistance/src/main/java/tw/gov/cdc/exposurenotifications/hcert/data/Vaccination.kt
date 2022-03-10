package tw.gov.cdc.exposurenotifications.hcert.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vaccination(
    @SerialName("tg")
    val target: ValueSetEntryAdapter,

    @SerialName("vp")
    val vaccine: ValueSetEntryAdapter,

    @SerialName("mp")
    val medicinalProduct: ValueSetEntryAdapter,

    @SerialName("ma")
    val authorizationHolder: ValueSetEntryAdapter,

    @SerialName("dn")
    val doseNumber: Int,

    @SerialName("sd")
    val doseTotalNumber: Int,

    @SerialName("dt")
    val dateString: String,

    @SerialName("co")
    val country: ValueSetEntryAdapter,

    @SerialName("is")
    val certificateIssuer: String,

    @SerialName("ci")
    val certificateIdentifier: String,
)
