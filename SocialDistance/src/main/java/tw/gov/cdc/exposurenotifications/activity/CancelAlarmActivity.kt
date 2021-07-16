package tw.gov.cdc.exposurenotifications.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import kotlinx.android.synthetic.main.activity_cancel_alarm.*
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
import tw.gov.cdc.exposurenotifications.common.Utils
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants.VerifyV1
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.network.Padding
import java.util.*

class CancelAlarmActivity : BaseActivity() {

    companion object {
        private const val TAG = "UploadActivity"
    }

    private val sendButton by lazy { cancel_alarm_send_button }
    private val codeText by lazy { cancel_alarm_code_edit_text }
    private val dateText by lazy {
        cancel_alarm_test_date_edit_text.apply {
            inputType = InputType.TYPE_NULL
        }
    }

    private var cancelDate = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_alarm)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        currentFocus?.clearFocus()

        dateText.setOnClickListener {
            DatePickerDialog(this,
                             { _, year, monthOfYear, dayOfMonth ->
                                 dateText.setText(getString(R.string.date_yyyy_mm_dd,
                                                            year,
                                                            monthOfYear + 1,
                                                            dayOfMonth))
                                 cancelDate.apply {
                                     set(Calendar.YEAR, year)
                                     set(Calendar.MONTH, monthOfYear)
                                     set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                 }
                             },
                             cancelDate.get(Calendar.YEAR),
                             cancelDate.get(Calendar.MONTH),
                             cancelDate.get(Calendar.DAY_OF_MONTH)).apply {
                Calendar.getInstance().add(Calendar.DAY_OF_MONTH, -14)
                datePicker.minDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -14)
                }.timeInMillis
                datePicker.maxDate = Date().time
            }.show()
        }

        sendButton.setOnClickListener {

            if (dateText.text.isNullOrBlank()) {
                Utils.showHintDialog(this, R.string.cancel_alert_test_date)
                return@setOnClickListener
            }

            if (codeText.text.isNullOrBlank()) {
                Utils.showHintDialog(this, R.string.enter_verification_code)
                return@setOnClickListener
            }

            sendButton.isEnabled = false
            showProgressBar()

            val code = codeText.text.toString()
            val body = verificationCodeRequestBody(code)
            val bodyRequest = body.toString().toRequestBody("application/json".toMediaTypeOrNull())

            APIService.verificationServer.submitAccCode(body = bodyRequest)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>,
                                            response: Response<ResponseBody>) {
                        Log.i(TAG, "submitAccCode onResponse $response")
                        if (response.isSuccessful) {
                            ExposureNotificationManager.updateSafeSummaries(cancelDate)
                            showUploadResultDialog(true)
                        } else {
                            showUploadResultDialog(false)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e(TAG, "submitAccCode onFailure $t")
                        showUploadResultDialog(false)
                    }
                })
        }
    }

    private fun showUploadResultDialog(success: Boolean) {
        sendButton.isEnabled = true
        hideProgressBar()
        AlertDialog.Builder(this)
            .setMessage(if (success) R.string.upload_success else R.string.upload_fail)
            .setPositiveButton(R.string.confirm) { _, _ ->
                if (success) {
                    onUploadSuccess()
                }
            }
            .setCancelable(false)
            .create()
            .show()
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

    /**
     * Submit Code
     */

    @Throws(JSONException::class)
    private fun verificationCodeRequestBody(accCode: String): JSONObject {
        Log.d(TAG, "verificationCodeRequestBody")
        return Padding.addPadding(JSONObject().put(VerifyV1.VERIFICATION_CODE, accCode))
    }
}