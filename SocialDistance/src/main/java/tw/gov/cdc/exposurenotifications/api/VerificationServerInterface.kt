package tw.gov.cdc.exposurenotifications.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.Secrets
import tw.gov.cdc.exposurenotifications.api.response.ConfigResponse
import tw.gov.cdc.exposurenotifications.api.response.HealthEducationResponse
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants

interface VerificationServerInterface {

    companion object {
        private val PACKAGE_NAME: String = BaseApplication.instance.packageName
        private val API_KEY by lazy {
            if (APIService.USE_DEV_SERVER) {
                Secrets().getdrYLWUMd(PACKAGE_NAME)
            } else {
                Secrets().getOkRhmBYA(PACKAGE_NAME)
            }
        }
    }

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @POST("/api/verify")
    fun submitCode(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                   @Body body: RequestBody): Call<ResponseBody>

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @POST("/api/certificate")
    fun submitKeysForCert(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                          @Body body: RequestBody): Call<ResponseBody>

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @POST("/api/verify-acc")
    fun submitAccCode(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                      @Body body: RequestBody): Call<ResponseBody>

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @POST("/api/phone-check")
    fun requestCode(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                    @Body body: RequestBody): Call<ResponseBody>

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @GET("/api/asset/health_education")
    fun healthEducation(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                        @Query("lang") language: String): Call<HealthEducationResponse>

    @Headers(
        "content-type: application/json",
        "accept: application/json"
    )
    @GET("/api/config")
    fun cloudConfig(@Header(ApiConstants.VerifyV1.API_KEY_HEADER) apiKey: String = API_KEY,
                    ): Call<ConfigResponse>

}
