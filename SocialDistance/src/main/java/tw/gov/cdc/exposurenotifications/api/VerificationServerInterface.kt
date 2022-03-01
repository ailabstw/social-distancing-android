package tw.gov.cdc.exposurenotifications.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.BuildConfig
import tw.gov.cdc.exposurenotifications.Secrets
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
}
