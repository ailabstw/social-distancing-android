package tw.gov.cdc.exposurenotifications.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface KeyServerInterface {

    @POST("/v1/publish")
    fun submitKeys(@Body body: RequestBody): Call<ResponseBody>
}
