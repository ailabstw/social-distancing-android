package tw.gov.cdc.exposurenotifications.api.response

import com.google.gson.annotations.SerializedName

data class HealthEducationResponse(
    @SerializedName("risky_detail")
    val riskDetail: String,

    @SerializedName("risky_detail_header_separator")
    val riskDetailSplitMark: String,

    @SerializedName("alert_cancellation_info")
    val cancelAlarm: String
)
