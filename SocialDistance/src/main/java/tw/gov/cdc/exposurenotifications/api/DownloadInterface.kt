package tw.gov.cdc.exposurenotifications.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.Secrets

interface DownloadInterface {

    companion object {
        private val PACKAGE_NAME: String = BaseApplication.instance.packageName
        val FOLDER_NAME by lazy {
            if (APIService.USE_DEV_SERVER) {
                Secrets().getdSsBWfYh(PACKAGE_NAME)
            } else {
                Secrets().getksTrLMGV(PACKAGE_NAME)
            }
        }
    }

    @Streaming
    @GET("/{folder}/index.txt")
    fun getIndexFile(@Path("folder") folderName: String = FOLDER_NAME): Call<ResponseBody>

    @GET("/{uri}")
    fun downloadFile(@Path("uri") uri: String): Call<ResponseBody>
}
