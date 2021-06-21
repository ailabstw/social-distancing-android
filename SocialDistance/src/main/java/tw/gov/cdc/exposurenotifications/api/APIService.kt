package tw.gov.cdc.exposurenotifications.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.BuildConfig
import tw.gov.cdc.exposurenotifications.Secrets
import java.util.concurrent.TimeUnit

object APIService {

    val USE_DEV_SERVER = BuildConfig.DEBUG // TODO: [p0] BuildConfig.DEBUG

    private val PACKAGE_NAME: String = BaseApplication.instance.packageName

    private val logging = HttpLoggingInterceptor().setLevel(if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    })

    private val client = OkHttpClient.Builder()
        .writeTimeout(600, TimeUnit.SECONDS) // For uploading a file
        .readTimeout(600, TimeUnit.SECONDS) // For waiting for API response after uploaded a file
        .addInterceptor(logging)
        .build()

    private fun <T> buildService(service: Class<T>, baseUrl: String): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(service)
    }

    val verificationServer =
        buildService(VerificationServerInterface::class.java, if (USE_DEV_SERVER) {
            Secrets().getZRhSQcHw(PACKAGE_NAME)
        } else {
            Secrets().getWWCVtKZp(PACKAGE_NAME)
        })

    val keyServer = buildService(KeyServerInterface::class.java, if (USE_DEV_SERVER) {
        Secrets().getiKOmAwhW(PACKAGE_NAME)
    } else {
        Secrets().getUFdijPVb(PACKAGE_NAME)
    })

    val downloadTEKs = buildService(DownloadInterface::class.java, if (USE_DEV_SERVER) {
        Secrets().gettAsyYeFk(PACKAGE_NAME)
    } else {
        Secrets().getsfLvmZak(PACKAGE_NAME)
    })
}
