package tw.gov.cdc.exposurenotifications.activity

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.activity_control.toolbar
import kotlinx.android.synthetic.main.activity_request_code.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.api.APIService
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants
import tw.gov.cdc.exposurenotifications.network.Padding
import java.util.*

class RequestCodeActivity : BaseActivity() {

    companion object {
        private const val TAG = "VerificationCodeActivity"
    }

    private val sendButton by lazy { request_code_send_button }
    private val phoneNumberText by lazy { request_code_edit_text }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_code)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        sendButton.setOnClickListener {
            val pattern = """09\d{8}""".toRegex()
            val phoneNumber = phoneNumberText.text.toString()
            if (!pattern.matches(phoneNumber)) {
                showResultDialog(GetCodeResult.FailedInvalidPhoneNumber)
                return@setOnClickListener
            }

            val lastGetCodeDaysSinceEpoch = (PreferenceManager.lastRequestCodeTime / 1000 / 60 / 60 / 24).toInt()
            val nowDaysSinceEpoch = (Date().time / 1000 / 60 / 60 / 24).toInt()
            if (PreferenceManager.requestCodeCount > 2 && nowDaysSinceEpoch == lastGetCodeDaysSinceEpoch) {
                showResultDialog(GetCodeResult.FailedLimitExceeded)
                return@setOnClickListener
            }

            sendButton.isEnabled = false
            showProgressBar()
            requestCode(phoneNumber)
        }
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

    // Request Code

    private fun requestCode(phoneNumber: String) {
        val body = requestCodeRequestBody(phoneNumber)
        val bodyRequest = body.toString().toRequestBody("application/json".toMediaTypeOrNull())

        APIService.verificationServer.requestCode(body = bodyRequest)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    Log.i(TAG, "requestCode onResponse $response")
                    if (response.isSuccessful) {
                        ++PreferenceManager.requestCodeCount
                        PreferenceManager.lastRequestCodeTime = Date().time
                        showResultDialog(GetCodeResult.Success)
                    } else {
                        showResultDialog(GetCodeResult.Failed)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "requestCode onFailure $t")
                    showResultDialog(GetCodeResult.Failed)
                }
            })
    }

    private fun showResultDialog(result: GetCodeResult) {
        sendButton.isEnabled = true
        hideProgressBar()
        AlertDialog.Builder(this).apply {
            result.title?.let { setTitle(it) }
            result.message?.let { setMessage(it) }
            setPositiveButton(R.string.confirm) { _, _ ->
                if (result != GetCodeResult.FailedInvalidPhoneNumber) {
                    finish()
                }
            }
            setCancelable(false)
        }.create().show()

    }

    private sealed class GetCodeResult {
        @StringRes
        open val title: Int? = null

        @StringRes
        open val message: Int? = null

        object Success: GetCodeResult() {
            override val title = R.string.request_verification_code_success
            override val message = R.string.request_verification_code_success_message
        }

        object Failed: GetCodeResult() {
            override val message = R.string.request_verification_code_fail_message
        }

        object FailedLimitExceeded: GetCodeResult() {
            override val message = R.string.request_verification_code_fail_limit_exceeded_message
        }

        object FailedInvalidPhoneNumber: GetCodeResult() {
            override val message = R.string.request_verification_code_fail_invalid_phone_number_message
        }
    }

    @Throws(JSONException::class)
    private fun requestCodeRequestBody(phoneNumber: String): JSONObject {
        Log.d(TAG, "requestCodeRequestBody")
        return Padding.addPadding(JSONObject().put(ApiConstants.VerifyV1.PHONE, phoneNumber))
    }
}
