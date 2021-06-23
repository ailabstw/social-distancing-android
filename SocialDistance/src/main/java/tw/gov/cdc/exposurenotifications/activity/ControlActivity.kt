package tw.gov.cdc.exposurenotifications.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.control_item.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager.ExposureNotificationState

class ControlActivity : BaseActivity() {

    companion object {
        private const val TAG = "ControlActivity"
    }

    private val controlView by lazy { controlBle }

    private val onCheckedChangeListener: CompoundButton.OnCheckedChangeListener by lazy {
        CompoundButton.OnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            Log.d(TAG, "onChecked toggled $isChecked")
            controlView.sw.apply {
                setOnCheckedChangeListener(null)
                // Set the toggle back. It will only toggle to correct state if operation succeeds.
                setChecked(!isChecked)
                setOnCheckedChangeListener(onCheckedChangeListener)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        ExposureNotificationManager.state.observe(this, Observer {
            Log.d(TAG, "observe state $it")
            controlView.sw.apply {
                setOnCheckedChangeListener(null)
                isChecked = it == ExposureNotificationState.Enabled
                setOnCheckedChangeListener(onCheckedChangeListener)
            }
        })

        controlView.text.setText(R.string.control_exposure)
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
