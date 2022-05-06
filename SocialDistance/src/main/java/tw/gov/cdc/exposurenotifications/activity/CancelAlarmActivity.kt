package tw.gov.cdc.exposurenotifications.activity

import android.app.AlertDialog
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.os.Bundle
import android.os.Parcel
import android.text.ParcelableSpan
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.view.Menu
import kotlinx.android.synthetic.main.activity_cancel_alarm.*
import org.json.JSONException
import org.json.JSONObject
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.data.InstructionRepository
import tw.gov.cdc.exposurenotifications.keyupload.ApiConstants.VerifyV1
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.network.Padding
import java.util.*

class CancelAlarmActivity : BaseActivity() {

    companion object {
        private const val TAG = "UploadActivity"
    }

    private val instructionText by lazy {
        cancel_alarm_text.apply {
            movementMethod = ScrollingMovementMethod()
        }
    }
    private val checkBox by lazy { cancel_alarm_check_box }
    private val sendButton by lazy { cancel_alarm_send_button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_alarm)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val builder = SpannableStringBuilder()
        val strings = InstructionRepository.cancelAlarmInstruction.toTypedArray().takeIf { it.isNotEmpty() }
            ?: resources.getStringArray(R.array.cancel_alert_intruction)

        strings.forEachIndexed { index, s ->
            val number = "${index + 1}. "
            builder.append(
                number + s,
                LeadingMarginSpan.Standard(0, instructionText.paint.measureText(number).toInt()),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            if (index != strings.lastIndex) {
                builder.append("\n", object : LineHeightSpan {
                    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, lineHeight: Int, fm: Paint.FontMetricsInt?) {
                        fm?.apply {
                            val height = descent - ascent
                            if (height > 0) {
                                val ratio = 0.5
                                descent = (descent * ratio).toInt()
                                ascent = descent - (height * ratio).toInt()
                            }
                        }
                    }
                }, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        instructionText.text = builder

        instructionText.post {
            checkBox.isEnabled = !instructionText.canScrollVertically(1)
        }

        instructionText.setOnScrollChangeListener { _, _, _, _, _ ->
            checkBox.isEnabled = !instructionText.canScrollVertically(1)
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            sendButton.isEnabled = isChecked
        }

        sendButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.menu_cancel_alert)
                .setMessage(R.string.cancel_alert_confirm_title)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    ExposureNotificationManager.updateSafeSummaries(Calendar.getInstance())
                    finish()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .setCancelable(false)
                .create()
                .show()
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

    /**
     * Submit Code
     */

    @Throws(JSONException::class)
    private fun verificationCodeRequestBody(accCode: String): JSONObject {
        Log.d(TAG, "verificationCodeRequestBody")
        return Padding.addPadding(JSONObject().put(VerifyV1.VERIFICATION_CODE, accCode))
    }
}