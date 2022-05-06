package tw.gov.cdc.exposurenotifications.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.gov.cdc.exposurenotifications.api.APIService
import tw.gov.cdc.exposurenotifications.api.response.HealthEducationResponse
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import java.util.*

object InstructionRepository {

    private const val TAG = "InstructionRepository"

    val riskDetailInstruction get() = PreferenceManager.riskDetailInstruction.split("\n").filter { it.isNotEmpty() }.map { it + "\n" }
    val riskDetailInstructionSplitMark get() = PreferenceManager.riskDetailInstructionSplitMark
    val cancelAlarmInstruction get() = PreferenceManager.cancelAlarmInstruction.split("\n").filter { it.isNotEmpty() }.map { it + "\n" }

    fun updateInstruction() {
        APIService.verificationServer.healthEducation(language = getLanguage())
            .enqueue(object : Callback<HealthEducationResponse> {
                override fun onResponse(call: Call<HealthEducationResponse>,
                                        response: Response<HealthEducationResponse>) {
                    Log.i(TAG, "healthEducation onResponse $response")
                    response.body()?.let { (riskDetail, riskDetailSplitMark, cancelAlarm) ->
                        PreferenceManager.riskDetailInstruction = riskDetail
                        PreferenceManager.riskDetailInstructionSplitMark = riskDetailSplitMark
                        PreferenceManager.cancelAlarmInstruction = cancelAlarm
                    }
                }

                override fun onFailure(call: Call<HealthEducationResponse>, t: Throwable) {
                    Log.e(TAG, "healthEducation onFailure $t")
                }
            })
    }

    private fun getLanguage(): String {
        return if (Locale.getDefault().language.contains("zh")) "zh-hant" else Locale.getDefault().language
    }
}
