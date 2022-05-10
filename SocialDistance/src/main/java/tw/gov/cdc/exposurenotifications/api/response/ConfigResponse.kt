package tw.gov.cdc.exposurenotifications.api.response

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("alarm_period")
    val riskAlarmPeriod: Int
)
