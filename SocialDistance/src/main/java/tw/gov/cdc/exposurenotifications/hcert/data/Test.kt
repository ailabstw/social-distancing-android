package tw.gov.cdc.exposurenotifications.hcert.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Test constructor(
    @SerialName("tg")
    val target: ValueSetEntryAdapter,

    @SerialName("tt")
    val type: ValueSetEntryAdapter,

    @SerialName("nm")
    val nameNaa: String? = null,

    @SerialName("ma")
    val nameRat: ValueSetEntryAdapter? = null,

    @SerialName("sc")
    val dateTimeSample: String,

    @SerialName("dr")
    val dateTimeResult: String? = null,

    @SerialName("tr")
    val resultPositive: ValueSetEntryAdapter,

    @SerialName("tc")
    val testFacility: String? = null,

    @SerialName("co")
    val country: ValueSetEntryAdapter,

    @SerialName("is")
    val certificateIssuer: String,

    @SerialName("ci")
    val certificateIdentifier: String,
)
