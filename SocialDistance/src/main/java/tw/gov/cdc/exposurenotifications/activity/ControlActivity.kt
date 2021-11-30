package tw.gov.cdc.exposurenotifications.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.CompoundButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.control_item.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager.ExposureNotificationState

class ControlActivity : BaseActivity() {

    companion object {
        private const val TAG = "ControlActivity"
    }

    private val exposureNotificationServiceControl by lazy { control_exposure_notification_service }
    private val notFoundNotificationControl by lazy { control_not_found_notification }

    private val onServiceCheckedChangeListener: CompoundButton.OnCheckedChangeListener by lazy {
        CompoundButton.OnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Log.d(TAG, "onChecked serviceControl $isChecked")
            exposureNotificationServiceControl.sw.apply {
                setOnCheckedChangeListener(null)
                // Set the toggle back. It will only toggle to correct state if operation succeeds.
                setChecked(!isChecked)
                setOnCheckedChangeListener(onServiceCheckedChangeListener)
            }
            when (isChecked) {
                true -> if (ExposureNotificationManager.state.value == ExposureNotificationState.Disabled
                    || !ExposureNotificationManager.askTurningOnBluetoothOrLocationIfNeeded(this)) {
                    ExposureNotificationManager.start(this)
                }
                false -> showTurnOffDialog()
            }
        }
    }

    private val onNotificationCheckedChangeListener: CompoundButton.OnCheckedChangeListener by lazy {
        CompoundButton.OnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Log.d(TAG, "onChecked notificationControl $isChecked")
            PreferenceManager.isNotFoundNotificationEnabled = isChecked
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        ExposureNotificationManager.state.observe(this, {
            Log.d(TAG, "observe state $it")
            exposureNotificationServiceControl.sw.apply {
                setOnCheckedChangeListener(null)
                isChecked = it == ExposureNotificationState.Enabled
                setOnCheckedChangeListener(onServiceCheckedChangeListener)
            }
            notFoundNotificationControl.sw.apply {
                setOnCheckedChangeListener(null)
                isChecked = it == ExposureNotificationState.Enabled && PreferenceManager.isNotFoundNotificationEnabled
                setOnCheckedChangeListener(onNotificationCheckedChangeListener)
                isEnabled = it == ExposureNotificationState.Enabled
            }
        })

        exposureNotificationServiceControl.text.setText(R.string.control_exposure)
        notFoundNotificationControl.text.setText(R.string.control_not_found_notification)
    }

    override fun onResume() {
        super.onResume()
        ExposureNotificationManager.updateStatus(this)
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
            RequestCode.REQUEST_RESOLUTION_EN_CLIENT_START -> {
                if (resultCode == Activity.RESULT_OK) {
                    // The resolution is solved (for example, the user gave consent).
                    // Start ExposureNotificationsClient again.
                    ExposureNotificationManager.start(this)
                } else {
                    // The resolution was rejected or cancelled.
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun showTurnOffDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.control_exposure_turn_off_title)
            .setMessage(R.string.control_exposure_turn_off_detail)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(R.string.confirm) { _, _ ->
                ExposureNotificationManager.stop(this)
            }
            .show()
    }
}
