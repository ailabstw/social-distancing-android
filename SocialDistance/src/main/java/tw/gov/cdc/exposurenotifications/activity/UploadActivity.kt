package tw.gov.cdc.exposurenotifications.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.common.base.Joiner
import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import com.google.common.io.BaseEncoding
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.Secrets
import tw.gov.cdc.exposurenotifications.api.APIService
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.common.Utils
import tw.gov.cdc.exposurenotifications.common.Utils.toBeginOfTheDay
import tw.gov.cdc.exposurenotifications.common.Utils.toEndOfTheDay
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants.UploadV1
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants.VerifyV1
import tw.gov.cdc.exposurenotifications.keyupload.Upload
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.network.DiagnosisKey
import tw.gov.cdc.exposurenotifications.network.Padding
import tw.gov.cdc.exposurenotifications.storage.DiagnosisEntity
import tw.gov.cdc.exposurenotifications.storage.DiagnosisEntity.TestResult
import tw.gov.cdc.exposurenotifications.storage.DiagnosisEntity.TravelStatus
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class UploadActivity : BaseActivity() {

    companion object {
        private const val TAG = "UploadActivity"
    }

    // TODO: Remove or save to pref or somewhere
    private lateinit var diagnosis: DiagnosisEntity

    private val sendButton by lazy { upload_send_button }
    private val codeText by lazy { upload_code_edit_text }
    private val startDateText by lazy {
        upload_date_start_edit_text.apply {
            inputType = InputType.TYPE_NULL
        }
    }
    private val endDateText by lazy {
        upload_date_end_edit_text.apply {
            inputType = InputType.TYPE_NULL
        }
    }

    private var startDate = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_MONTH, -14)
    }
    private var endDate = Calendar.getInstance()

    private enum class State {
        INITIAL, CODE_VERIFIED
    }

    private var state = State.INITIAL

    private var recentKeys: List<TemporaryExposureKey>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        currentFocus?.clearFocus()

        startDateText.setOnClickListener {
            DatePickerDialog(this,
                             { _, year, monthOfYear, dayOfMonth ->
                                 startDateText.setText(getString(R.string.date_yyyy_mm_dd,
                                                                 year,
                                                                 monthOfYear + 1,
                                                                 dayOfMonth))
                                 startDate.apply {
                                     set(Calendar.YEAR, year)
                                     set(Calendar.MONTH, monthOfYear)
                                     set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                 }
                             },
                             startDate.get(Calendar.YEAR),
                             startDate.get(Calendar.MONTH),
                             startDate.get(Calendar.DAY_OF_MONTH))
                .apply {
                    Calendar.getInstance().add(Calendar.DAY_OF_MONTH, -14)
                    datePicker.minDate = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -14)
                    }.timeInMillis
                    datePicker.maxDate = endDate.timeInMillis
                }
                .show()
        }

        endDateText.setOnClickListener {
            DatePickerDialog(this,
                             { _, year, monthOfYear, dayOfMonth ->
                                 endDateText.setText(getString(R.string.date_yyyy_mm_dd,
                                                               year,
                                                               monthOfYear + 1,
                                                               dayOfMonth))
                                 endDate.apply {
                                     set(Calendar.YEAR, year)
                                     set(Calendar.MONTH, monthOfYear)
                                     set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                 }
                             },
                             endDate.get(Calendar.YEAR),
                             endDate.get(Calendar.MONTH),
                             endDate.get(Calendar.DAY_OF_MONTH))
                .apply {
                    Calendar.getInstance().add(Calendar.DAY_OF_MONTH, -14)
                    datePicker.minDate = startDate.timeInMillis
                    datePicker.maxDate = Date().time
                }
                .show()
        }

        sendButton.setOnClickListener {

            if (startDateText.text.isNullOrBlank()) {
                Utils.showHintDialog(this, R.string.upload_date_start_enter_hint)
                return@setOnClickListener
            }

            if (endDateText.text.isNullOrBlank()) {
                Utils.showHintDialog(this, R.string.upload_date_end_enter_hint)
                return@setOnClickListener
            }

            if (codeText.text.isNullOrBlank()) {
                Utils.showHintDialog(this, R.string.enter_verification_code)
                return@setOnClickListener
            }

            sendButton.isEnabled = false
            showProgressBar()

            when (state) {
                State.INITIAL -> {
                    submitCode()
                }
                State.CODE_VERIFIED -> {
                    requestKeysIfNeeded()
                }
            }
        }
    }

    private fun submitCode() {
        val code = codeText.text.toString()
        val upload = Upload.newBuilder(code).build()
        val body = verificationCodeRequestBody(upload)
        val bodyRequest = body.toString().toRequestBody("application/json".toMediaTypeOrNull())

        APIService.verificationServer.submitCode(body = bodyRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    Log.i(TAG, "submitCode onResponse $response")

                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody != null) {
                        val verifiedUpload = captureVerificationCodeResponse(upload, JSONObject(responseBody.string()))
                        diagnosis = getDiagnosisEntity(verifiedUpload = verifiedUpload, code = code)
                        state = State.CODE_VERIFIED
                        requestKeysIfNeeded()
                    } else {
                        showUploadResultDialog(UploadResult.FAILED)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "submitCode onFailure $t")
                    showUploadResultDialog(UploadResult.FAILED)
                }
            })
    }

    private enum class UploadResult {
        SUCCESS,
        FAILED,
        FAILED_NO_FILES,
        FAILED_NO_GRANTED_TEK
    }

    private fun showUploadResultDialog(result: UploadResult) {
        val success = when (result) {
            UploadResult.SUCCESS -> {
                true
            }
            UploadResult.FAILED -> {
                codeText.isEnabled = true
                state = State.INITIAL
                false
            }
            UploadResult.FAILED_NO_FILES, UploadResult.FAILED_NO_GRANTED_TEK -> {
                codeText.isEnabled = false
                false
            }
        }
        sendButton.isEnabled = true
        hideProgressBar()
        AlertDialog.Builder(this).apply {
            setTitle(if (success) R.string.upload_success else R.string.upload_fail)
            if (result == UploadResult.FAILED_NO_FILES) {
                setMessage(R.string.upload_fail_message_no_files)
            }
            setPositiveButton(R.string.confirm) { _, _ ->
                if (success) {
                    onUploadSuccess()
                }
            }
            setCancelable(false)
        }.create().show()

    }

    private fun onUploadSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    // ActionBar

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.no_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // The user consent for retrieving TEKs is granted for 24 hours.
            RequestCode.REQUEST_RESOLUTION_EN_TEK_HISTORY -> {
                if (resultCode == Activity.RESULT_OK) {
                    // The resolution was completed. Attempt to re-retrieve the keys again.
                    ExposureNotificationManager.getTemporaryExposureKeyHistory(this)
                } else {
                    // There was an issue with the user accepting the prompt.
                    showUploadResultDialog(UploadResult.FAILED_NO_GRANTED_TEK)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    /**
     * Submit Code
     */

    private val SUPPORTED_TEST_TYPES: JSONArray = JSONArray(
        ImmutableList.of(
            TestResult.CONFIRMED.toApiType(),
            TestResult.LIKELY.toApiType(),
            TestResult.NEGATIVE.toApiType()
        )
    )

    @Throws(JSONException::class)
    private fun verificationCodeRequestBody(upload: Upload): JSONObject {
        Log.d(TAG, "verificationCodeRequestBody")
        return Padding.addPadding(
            JSONObject()
                .put(VerifyV1.VERIFICATION_CODE, upload.verificationCode())
                .put(VerifyV1.ACCEPT_TEST_TYPES, SUPPORTED_TEST_TYPES)
        )
    }

    private fun captureVerificationCodeResponse(upload: Upload, response: JSONObject): Upload {
        Log.d(TAG, "captureVerificationCodeResponse")
        if (upload.isCoverTraffic) {
            // Ignore responses for cover traffic requests.
            return upload
        }
        val withResponse = upload.toBuilder()
        return try {
            if (response.has(VerifyV1.TEST_TYPE) &&
                !Strings.isNullOrEmpty(response.getString(VerifyV1.TEST_TYPE))
            ) {
                withResponse.setTestType(response.getString(VerifyV1.TEST_TYPE))
            }
            if (response.has(VerifyV1.VERIFICATION_TOKEN) &&
                !Strings.isNullOrEmpty(response.getString(VerifyV1.VERIFICATION_TOKEN))
            ) {
                withResponse.setLongTermToken(response.getString(VerifyV1.VERIFICATION_TOKEN))
            }
            if (response.has(VerifyV1.ONSET_DATE)
                && !Strings.isNullOrEmpty(response.getString(VerifyV1.ONSET_DATE))
            ) {
                // LocalDate.parse() defaults to iso-8601 date format "YYYY-MM-DD", as returned by
                // the verification server for symptomDate.
                withResponse.setSymptomOnset(LocalDate.parse(response.getString(VerifyV1.ONSET_DATE)))
            }
            withResponse.build()
        } catch (e: JSONException) {
            // TODO: Better exception
            throw RuntimeException(e)
        }
    }

    private fun getDiagnosisEntity(verifiedUpload: Upload, code: String): DiagnosisEntity {
        Log.d(TAG, "getDiagnosisEntity")
        // If successful, capture the long term token and some diagnosis facts into exposurenotifications.storage.
        val builder: DiagnosisEntity.Builder = DiagnosisEntity.newBuilder()
            .setVerificationCode(code).setIsCodeFromLink(false)
        // The long term token is required.
        builder.setLongTermToken(verifiedUpload.longTermToken())
        // Symptom onset may or may not be provided by the verification server.
        if (verifiedUpload.symptomOnset() != null) {
            builder.setIsServerOnsetDate(true)
            builder.setOnsetDate(verifiedUpload.symptomOnset())
                .setHasSymptoms(DiagnosisEntity.HasSymptoms.YES)
        }
        // Test type is currently always provided by the verification server, but that seems
        // like something that could change. Let's check.
        if (verifiedUpload.testType() != null) {
            builder.setTestResult(TestResult.of(verifiedUpload.testType()!!))
        }
        return builder.build()
    }

    /**
     * get Cert
     */

    private fun requestKeysIfNeeded() {
        recentKeys?.also {
            onReceivedKeys(it)
        } ?: run {
            ExposureNotificationManager.getTemporaryExposureKeyHistory(this@UploadActivity)
        }
    }

    fun onReceivedKeys(keys: List<TemporaryExposureKey>) {
        Log.d(TAG, "Converting TEKs into DiagnosisKeys...")
        recentKeys = keys
        val builder: ImmutableList.Builder<DiagnosisKey> = ImmutableList.Builder<DiagnosisKey>()

        val timeInMillisToIntervalNumber = { timeInMillis: Long ->
            timeInMillis / 1000 / 60 / 10
        }

        val startIntervalNumber = timeInMillisToIntervalNumber(startDate.toBeginOfTheDay().timeInMillis)
        val endInterValNumber = timeInMillisToIntervalNumber(endDate.toEndOfTheDay().timeInMillis)

        Log.d(TAG, "interval [$startIntervalNumber, $endInterValNumber]")

        for (k in keys) {
            if (k.rollingStartIntervalNumber + k.rollingPeriod >= startIntervalNumber && k.rollingStartIntervalNumber <= endInterValNumber) {
                Log.d(TAG, "add key $k")
                builder.add(DiagnosisKey.newBuilder().setKeyBytes(k.keyData)
                                .setIntervalNumber(k.rollingStartIntervalNumber)
                                .setRollingPeriod(k.rollingPeriod) // Accepting the default transmission risk for now, which the DiagnosisKey.Builder
                                // comes with pre-set.
                                .build())
            }
        }
        val diagnosisKeys = builder.build()
        if (diagnosisKeys.isEmpty()) {
            showUploadResultDialog(UploadResult.FAILED_NO_FILES)
        } else {
            getCertAndUploadKeys(diagnosisKeys)
        }
    }

    private fun getCertAndUploadKeys(diagnosisKeys: ImmutableList<DiagnosisKey>) {
        Log.d(TAG, "getCertAndUploadKeys")
        val upload = Upload.newBuilder(diagnosisKeys, diagnosis.verificationCode)
            .setLongTermToken(diagnosis.longTermToken).setSymptomOnset(diagnosis.onsetDate)
            .setCertificate(diagnosis.certificate)
            .setHasTraveled(TravelStatus.TRAVELED == diagnosis.travelStatus).build()

        if (Strings.isNullOrEmpty(upload.certificate())) {
            submitKeysForCert(upload)
        } else {
            upload(upload)
        }
    }

    private fun submitKeysForCert(upload: Upload) {
        Log.d(TAG, "submitKeysForCert")

        val body = certRequestBody(upload)
        val bodyRequest = body.toString().toRequestBody("application/json".toMediaTypeOrNull())

        APIService.verificationServer.submitKeysForCert(body = bodyRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    Log.i(TAG, "submitKeysForCert onResponse $response")

                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody != null) {
                        val certedUpload = captureCertResponse(upload, JSONObject(responseBody.string())).toBuilder()
                            .setRevisionToken(PreferenceManager.revisionToken).build()

                        upload(certedUpload)
                    } else {
                        showUploadResultDialog(UploadResult.FAILED)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "submitKeysForCert onFailure $t")
                    showUploadResultDialog(UploadResult.FAILED)
                }
            })
    }

    @Throws(JSONException::class)
    private fun certRequestBody(upload: Upload): JSONObject {
        Log.d(TAG, "certRequestBody")
        return Padding.addPadding(
            JSONObject()
                .put(VerifyV1.VERIFICATION_TOKEN, upload.longTermToken())
                .put(VerifyV1.HMAC_KEY, hashedKeys(upload))
        )
    }

    private fun captureCertResponse(upload: Upload, response: JSONObject): Upload {
        Log.d(TAG, "captureCertResponse")
        if (upload.isCoverTraffic) {
            // Ignore responses for cover traffic requests.
            return upload
        }
        val withResponse = upload.toBuilder()
        return try {
            if (response.has(VerifyV1.CERT)
                && !Strings.isNullOrEmpty(response.getString(VerifyV1.CERT))
            ) {
                withResponse.setCertificate(response.getString(VerifyV1.CERT))
            }
            withResponse.build()
        } catch (e: JSONException) {
            // TODO: Better exception
            throw java.lang.RuntimeException(e)
        }
    }

    private val COMMAS = Joiner.on(',')
    private val BASE64 = BaseEncoding.base64()
    private val HASH_ALGO = "HmacSHA256"

    private fun hashedKeys(upload: Upload): String {
        Log.d(TAG, "hashedKeys")
        val cleartextSegments: MutableList<String> = ArrayList<String>(upload.keys()!!.size)
        for (k in upload.keys()!!) {
            cleartextSegments.add(
                java.lang.String.format(
                    Locale.ENGLISH,
                    "%s.%d.%d.%d",
                    BASE64.encode(k.keyBytes),
                    k.intervalNumber,
                    k.rollingPeriod,
                    k.transmissionRisk
                )
            )
        }
        cleartextSegments.sort()
        val cleartext: String = COMMAS.join(cleartextSegments)
        Log.d(TAG, upload.keys()!!.size.toString() + " keys for hashing prior to verification: [" + cleartext + "]")
        return try {
            val mac = Mac.getInstance(HASH_ALGO)
            mac.init(
                SecretKeySpec(
                    BASE64.decode(upload.hmacKeyBase64()),
                    HASH_ALGO
                )
            )
            BASE64.encode(
                mac.doFinal(
                    cleartext.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
            )
        } catch (e: NoSuchAlgorithmException) {
            // TODO: Better exception
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw java.lang.RuntimeException(e)
        }
    }

    /**
     * upload keys
     */

    private fun upload(upload: Upload) {
        Log.d(TAG, "Uploading keys and cert to keyserver...")

        if (upload.keys()!!.isEmpty()) {
            Log.d(TAG, "Zero keys given, skipping.")
            showUploadResultDialog(UploadResult.FAILED_NO_FILES)
            return
        }
        Log.d(TAG, "Uploading keys: [" + upload.keys()!!.size.toString() + "]")

        val payload = createPayload(upload)
        val bodyRequest = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())

        APIService.keyServer.submitKeys(body = bodyRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    Log.i(TAG, "submitKeys onResponse $response")
                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody != null) {
                        val revisionedUpload = captureRevisionToken(
                            response = JSONObject(responseBody.string()),
                            upload = upload)

                        // Successfully submitted
                        Log.d(TAG, "Upload success: $revisionedUpload")

                        // Update diagnosis
                        diagnosis = diagnosis.toBuilder()
                            .setCertificate(revisionedUpload.certificate())
                            .setRevisionToken(revisionedUpload.revisionToken())
                            .setSharedStatus(DiagnosisEntity.Shared.SHARED)
                            .build()

                        revisionedUpload.revisionToken()?.let {
                            PreferenceManager.revisionToken = it
                        }
                        showUploadResultDialog(UploadResult.SUCCESS)
                    } else {
                        showUploadResultDialog(UploadResult.FAILED)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "submitKeys onFailure $t")
                    showUploadResultDialog(UploadResult.FAILED)
                }
            })
    }

    private fun createPayload(upload: Upload): JSONObject {
        Log.d(TAG, "createPayload")
        var payload = JSONObject()
        val keysJson = JSONArray()
        try {
            for (k in upload.keys()!!) {
                Log.d(TAG, "Adding key: $k to submission.")
                keysJson.put(
                    JSONObject()
                        .put(UploadV1.KEY, BASE64.encode(k.keyBytes))
                        .put(UploadV1.ROLLING_START_NUM, k.intervalNumber)
                        .put(UploadV1.ROLLING_PERIOD, k.rollingPeriod)
                        .put(UploadV1.TRANSMISSION_RISK, k.transmissionRisk)
                )
            }
            val regionCodesJson = JSONArray()
            for (r in upload.regions()!!) {
                regionCodesJson.put(r)
            }
            payload
                .put(UploadV1.KEYS, keysJson)
                .put(UploadV1.HEALTH_AUTHORITY_ID, if (APIService.USE_DEV_SERVER) {
                    Secrets().getZNBOgmGj(packageName)
                } else {
                    Secrets().getWcUvCKEx(packageName)
                })
                .put(UploadV1.HMAC_KEY, upload.hmacKeyBase64())
                .put(UploadV1.VERIFICATION_CERT, upload.certificate())
                .put(UploadV1.TRAVELER, upload.hasTraveled())

            // Onset date is optional
            if (upload.symptomOnset() != null) {
                val onsetDateInterval = DiagnosisKey.instantToInterval(
                    upload.symptomOnset()!!.atStartOfDay(ZoneOffset.UTC).toInstant()
                )
                payload.put(UploadV1.ONSET, onsetDateInterval)
            }

            // We have a revision token only on second and subsequent uploads.
            if (upload.revisionToken() != null) {
                payload.put(UploadV1.REVISION_TOKEN, upload.revisionToken())
            }
            payload = Padding.addPadding(payload)
            return payload
        } catch (e: JSONException) {
            return payload
        }
    }

    //    @Throws(KeysSubmitFailureException::class)
    private fun captureRevisionToken(response: JSONObject, upload: Upload): Upload {
        return if (upload.isCoverTraffic) {
            upload
        } else try {
            upload.toBuilder()
                .setRevisionToken(response.getString(UploadV1.REVISION_TOKEN))
                .build()
        } catch (e: JSONException) {
            upload
            // "Server error" here is maybe a bit optimistic: it assumes that the response body was
            // incorrect, but it could be that the app's interpretation of the response is incorrect.

//            throw KeysSubmitFailureException(UploadError.SERVER_ERROR)
        }
    }
}