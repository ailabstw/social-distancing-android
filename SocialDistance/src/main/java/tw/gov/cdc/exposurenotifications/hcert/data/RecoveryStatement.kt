package tw.gov.cdc.exposurenotifications.hcert.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecoveryStatement(
    @SerialName("tg")
    val target: ValueSetEntryAdapter,

    @SerialName("fr")
    val dateOfFirstPositiveTestResult: String,

    @SerialName("co")
    val country: ValueSetEntryAdapter,

    @SerialName("is")
    val certificateIssuer: String,

    @SerialName("df")
    val certificateValidFrom: String,

    @SerialName("du")
    val certificateValidUntil: String,

    @SerialName("ci")
    val certificateIdentifier: String,
)
