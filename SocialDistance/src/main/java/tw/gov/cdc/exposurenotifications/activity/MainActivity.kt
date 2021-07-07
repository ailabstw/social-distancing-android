package tw.gov.cdc.exposurenotifications.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.android.synthetic.main.activity_main.*
import tw.gov.cdc.exposurenotifications.BuildConfig
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.*
import tw.gov.cdc.exposurenotifications.common.FeaturePresentManager.Feature
import tw.gov.cdc.exposurenotifications.common.Utils.getDateString
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager.ExposureNotificationState
import java.lang.reflect.InvocationTargetException
import java.util.*

class MainActivity : BaseActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
//                val bytesDownloaded = state.bytesDownloaded()
//                val totalBytesToDownload = state.totalBytesToDownload()
            }
            InstallStatus.DOWNLOADED -> {
                showCompleteUpdateDialog()
            }
            else -> {
            }
        }
    }

    private lateinit var riskStatus: RiskStatus

    private val textRiskBrief by lazy { main_risk_brief_text }
    private val textRiskDetail by lazy {
        main_risk_detail_text.apply {
            movementMethod = ScrollingMovementMethod()
        }
    }

    // This is to let ConstraintLayout to calculate the correct height of textRiskDetail
    private val textRiskDetailInvisible by lazy { main_risk_detail_text_invisible }

    private val textInfo by lazy { main_info_text }
    private val textVersion by lazy { main_version_text }
    private val buttonStart by lazy { main_start_button }

    private val allFeatures = mutableListOf(Feature.BARCODE_V2, Feature.DAILY_SUMMARY)
    private var currentPresentingFeature: Feature? = null
    private val featureBarcodeGroup by lazy { feature_barcode_group }
    private val featureDailySummaryGroup by lazy { feature_daily_summary_group }
    private val featureTouchView by lazy { feature_touch_view }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val hasBackCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)

        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.icon_qrcode)
            // Only provide 1922 sms qrcode scanning feature for devices have a back camera
            setDisplayHomeAsUpEnabled(hasBackCamera)
        }

        if (!hasBackCamera) {
            allFeatures.remove(Feature.BARCODE_V2)
        }

//        if (debugNotification) {
//            NotificationHelper.createNotificationChannelIfNeeded(NotificationHelper.NotificationType.ProvideDiagnosisKeys.channelInfo, this)
//            NotificationHelper.createNotificationChannelIfNeeded(NotificationHelper.NotificationType.ExposureNotFound.channelInfo, this)
//        }
        setupButton()

        ExposureNotificationManager.dailySummaries.observe(this, {
            updateStatus()
        })
        ExposureNotificationManager.state.observe(this, {
            updateStatus()
        })

        if (PreferenceManager.isFirstTimeEnableNeeded) {
            Log.v(TAG, "need first time enable")
            ExposureNotificationManager.start(this)
        } else {
            Log.v(TAG, "has enabled before")
        }

        textVersion.text = "v${BuildConfig.VERSION_NAME}"

        featureTouchView.setOnClickListener {
            clearCurrentPresentingFeature()
        }
    }

    override fun onStart() {
        super.onStart()
        checkUpdate()
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.lastEnterMainPageTime = Date().time
        ExposureNotificationManager.updateStatus(this)
        updateStatus()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_cancel_alarm)?.isVisible = ExposureNotificationManager.isInRisk
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            gotoBarcodePage()
            true
        }
        R.id.action_about -> {
            startActivity(Intent(this, IntroductionActivity::class.java))
            true
        }
        R.id.action_daily_summary -> {
            gotoDailySummaryPage()
            true
        }
        R.id.action_cancel_alarm -> {
            showCancelAlarmConfirmDialog()
            true
        }
        R.id.action_upload -> {
            showUploadConfirmDialog()
            true
        }
        R.id.action_privacy -> {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.PRIVACY))
            true
        }
        R.id.action_control -> {
            startActivity(Intent(this, ControlActivity::class.java))
            true
        }
        R.id.action_faq -> {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.FAQ))
            true
        }
        R.id.action_hints -> {
            FeaturePresentManager.resetPresented()
            presentFeatureIfNeeded()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun gotoDailySummaryPage() {
        startActivity(Intent(this, DailySummaryActivity::class.java))
    }

    private fun showCancelAlarmConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.cancel_alert_confirm_title)
            .setMessage(R.string.cancel_alert_confirm_message)
            .setNegativeButton(R.string.no) { _, _ ->
            }
            .setPositiveButton(R.string.yes) { _, _ ->
                startActivity(Intent(this, CancelAlarmActivity::class.java))
            }
            .create()
            .show()
    }

    private fun showUploadConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.upload_confirm_title)
            .setMessage(R.string.upload_confirm_message)
            .setNegativeButton(R.string.no) { _, _ ->
            }
            .setPositiveButton(R.string.yes) { _, _ ->
                startActivity(Intent(this, UploadActivity::class.java))
            }
            .create()
            .show()
    }

    private fun gotoBarcodePage() {
        if (!PermissionUtils.requestCameraPermissionIfNeeded(this)) {
            startActivity(Intent(this, BarcodeScanningActivity::class.java))
        }
    }

//    var debugNotification = false
//    var debugCount = 0

    private fun setupButton() {

        buttonStart.setOnClickListener {
//            if (debugNotification) {
//                when (debugCount % 5) {
//                    0 -> {
//                        NotificationHelper.postNotification(NotificationHelper.NotificationType.ProvideDiagnosisKeys, this, true)
//                    }
//                    1 -> {
//                        NotificationHelper.postNotification(NotificationHelper.NotificationType.ExposureNotFound, this, true)
//                    }
//                    2 -> {
//                        NotificationHelper.postNotification(NotificationHelper.NotificationType.ExposureStateUpdated, this, true)
//                    }
//                    3 -> {
//                        NotificationHelper.postNotification(NotificationHelper.NotificationType.ServiceStateUpdated(true), this, true)
//                    }
//                    4 -> {
//                        NotificationHelper.postNotification(NotificationHelper.NotificationType.ServiceStateUpdated(false), this, true)
//                    }
//                }
//                debugCount ++
//            }
            (ExposureNotificationManager.state.value as? ExposureNotificationState.NotSupport)?.also {
                showNotSupportDialog(it.reason)
            } ?: run {
                if (ExposureNotificationManager.state.value == ExposureNotificationState.Disabled
                    || (!ExposureNotificationManager.askManageStorageIfNeeded(this)
                          && !ExposureNotificationManager.askTurningOnBluetoothOrLocationIfNeeded(this))) {
                    ExposureNotificationManager.start(this)
                }
            }
            updateStatus()
        }
    }

    private fun showNotSupportDialog(@StringRes reason: Int) {
        AlertDialog.Builder(this)
            .setMessage(getString(reason) + getString(R.string.state_instruction))
            .setNegativeButton(R.string.cancel) { _, _ ->
            }
            .setPositiveButton(R.string.state_instruction_next_step) { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_clean_cache))))
            }
            .create()
            .show()
    }
    private fun updateStatus() {

        /**
         * Update bottom area
         */

        textInfo.text = run {

            val firstLaunchTime = resources.getString(
                R.string.main_info_first_launch_time,
                getDateString(Date().apply {
                    time = PreferenceManager.firstEnabledTime
                })
            )

            // To prevent totalTime == 0,
            val enableTimePercent = maxOf(Date().time - PreferenceManager.firstEnabledTime, 1000).let { totalTime ->
                resources.getString(
                    R.string.main_info_enable_time_percent,
                    // textEnableTimePercent is [0, 100] percent
                    minOf(
                        maxOf(
                            ((totalTime - PreferenceManager.accumulatedDisableTime) * 100 / totalTime),
                            0
                        ), 100
                    )
                )
            }

            val lastCheckTime = if (PreferenceManager.lastCheckTime != 0L) {
                resources.getString(
                    R.string.main_info_last_check_time,
                    getDateString(Date().apply {
                        time = PreferenceManager.lastCheckTime
                    })
                )
            } else {
                " "
            }

            resources.getString(
                R.string.main_info,
                firstLaunchTime,
                enableTimePercent,
                lastCheckTime
            )
        }

        val isExposureNotificationEnabled =
            ExposureNotificationManager.state.value == ExposureNotificationState.Enabled

        when (isExposureNotificationEnabled) {
            true -> {
                textInfo.visibility = View.VISIBLE
                buttonStart.isEnabled = false
                buttonStart.setText(R.string.main_button_already_start)
            }
            false -> {
                textInfo.visibility = View.INVISIBLE
                buttonStart.isEnabled = true
                buttonStart.setText(R.string.main_button_start)
            }
        }

        /**
         * Update top area
         */

        val setNormalColorWithDot = { withDot: Boolean ->
            textRiskBrief.setTextColor(ContextCompat.getColor(this, R.color.text_risk_low))
            textRiskDetail.setTextColor(ContextCompat.getColor(this, R.color.text_risk_low))
            textRiskBrief.setBackgroundResource(
                if (withDot) R.drawable.background_text_risk_low_doted
                else R.drawable.background_text_risk_low
            )
        }

        val setRiskyColorWithDot = { withDot: Boolean ->
            textRiskBrief.setTextColor(ContextCompat.getColor(this, R.color.text_risk_high))
            textRiskDetail.setTextColor(ContextCompat.getColor(this, R.color.text))
            textRiskBrief.setBackgroundResource(
                if (withDot) R.drawable.background_text_risk_high_doted
                else R.drawable.background_text_risk_high
            )
        }

        riskStatus = when {
            ExposureNotificationManager.isInRisk -> RiskStatus.RISKY
            !isExposureNotificationEnabled -> RiskStatus.UNKNOWN
            else -> RiskStatus.NORMAL
        }

        val getBulletSpan = {
            BulletSpan(18, ContextCompat.getColor(this, R.color.text), 8)
        }

        // FIXME: Should have a better solution
        // get lineCount and update text in textRiskDetailInvisible to let textRiskDetail has the correct height
        val updateInvisibleText = {
            textRiskDetail.post {
                val char = textRiskDetail.text.first()
                val lineCount = textRiskDetail.lineCount
                var invisibleString = ""
                for (i in 1..lineCount) {
                    invisibleString += "$i $char\n"
                }
                textRiskDetailInvisible.text = invisibleString
            }
        }

        when (riskStatus) {
            RiskStatus.UNKNOWN -> {
                textRiskBrief.setText(R.string.risk_brief_unknown)
                textRiskDetail.setText(R.string.risk_detail_unknown)
                updateInvisibleText()
                setNormalColorWithDot(false)
                textRiskBrief.setOnClickListener(null)
            }
            RiskStatus.NORMAL -> {
                textRiskBrief.setText(R.string.risk_brief_normal)
                textRiskDetail.setText(R.string.risk_detail_normal)
                updateInvisibleText()
                setNormalColorWithDot(true)
                textRiskBrief.setOnClickListener {
                    gotoDailySummaryPage()
                }
            }
            RiskStatus.RISKY -> {
                textRiskBrief.setText(R.string.risk_brief_risky)
                val strings = resources.getStringArray(R.array.risk_detail_risky)
                if (strings.size > 1) {
                    val builder = SpannableStringBuilder()
                    val splitMark = getString(R.string.risk_detail_risky_split)
                    var startIndex = 0
                    strings.forEach { item ->
                        builder.append(
                            item,
                            getBulletSpan(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        builder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            startIndex,
                            startIndex + item.indexOf(splitMark) + splitMark.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        startIndex = builder.lastIndex

                    }
                    textRiskDetail.text = builder

                    // FIXME: Should have a better solution
                    // get lineCount and update text in textRiskDetailInvisible to let textRiskDetail has the correct height
                    textRiskDetail.post {
                        val char = builder.first()
                        val lineCount = textRiskDetail.lineCount
                        val invisibleString = SpannableStringBuilder()
                        for (i in 1..lineCount) {
                            invisibleString.append(
                                "$i $char\n",
                                getBulletSpan(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        invisibleString.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            invisibleString.lastIndex + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        textRiskDetailInvisible.text = invisibleString
                    }
                } else if (strings.size == 1) {
                    textRiskDetail.text = strings[0]
                    updateInvisibleText()
                }
                setRiskyColorWithDot(true)
                textRiskBrief.setOnClickListener {
                    gotoDailySummaryPage()
                }
            }
        }

        /**
         * present feature if needed
         */

        presentFeatureIfNeeded()
    }

    private fun checkUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Log.d(TAG, "appUpdateInfo $appUpdateInfo")
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    showCompleteUpdateDialog()
                    return@addOnSuccessListener
                }
                try {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        when {
                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> {
                                appUpdateManager.registerListener(appUpdateListener)
                                appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.FLEXIBLE,
                                    this,
                                    RequestCode.REQUEST_UPDATE_APP
                                )
                            }
                            appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                                appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    this,
                                    RequestCode.REQUEST_UPDATE_APP
                                )
                            }
                        }
                    } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            RequestCode.REQUEST_UPDATE_APP
                        )
                    }
                } catch (e: IntentSender.SendIntentException) {
                    // TODO: Show a dialog for navigating to play store
                    Log.e(TAG, "startUpdateFlowForResult error $e")
                } catch (e: InvocationTargetException) {
                    // TODO: Show a dialog for navigating to play store
                    Log.e(TAG, "startUpdateFlowForResult error $e")
                }
            }
    }

    private fun showCompleteUpdateDialog() {
        hideProgressBar()
        appUpdateManager.unregisterListener(appUpdateListener)
        if (Looper.getMainLooper().isCurrentThread) {
            AlertDialog.Builder(this)
                .setMessage(R.string.app_update_message)
                .setPositiveButton(R.string.app_update_restart) { _, _ ->
                    appUpdateManager.completeUpdate()
                }
                .setCancelable(false)
                .create()
                .show()
        } else {
            appUpdateManager.completeUpdate()
        }
    }

    private fun presentFeatureIfNeeded() {
        if (currentPresentingFeature == null) {
            FeaturePresentManager.getFeaturesNeedToPresent(allFeatures).forEach {
                if (presentFeature(it)) {
                    currentPresentingFeature = it
                    return
                }
            }
        }
    }

    private fun presentFeature(feature: Feature): Boolean {
        return when (feature) {
            Feature.BARCODE_V2 -> {
                featureBarcodeGroup.visibility = View.VISIBLE
                featureTouchView.visibility = View.VISIBLE
                true
            }
            Feature.DAILY_SUMMARY -> {
                when (riskStatus) {
                    RiskStatus.UNKNOWN -> {
                        false
                    }
                    else -> {
                        featureDailySummaryGroup.visibility = View.VISIBLE
                        featureTouchView.visibility = View.VISIBLE
                        true
                    }
                }
            }
        }
    }

    private fun clearCurrentPresentingFeature() {
        val feature = currentPresentingFeature ?: return

        when (feature) {
            Feature.BARCODE_V2 -> {
                featureBarcodeGroup.visibility = View.GONE
                featureTouchView.visibility = View.GONE
            }
            Feature.DAILY_SUMMARY -> {
                featureDailySummaryGroup.visibility = View.GONE
                featureTouchView.visibility = View.GONE
            }
        }
        FeaturePresentManager.setPresented(setOf(feature))
        currentPresentingFeature = null

        presentFeatureIfNeeded()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCode.REQUEST_RESOLUTION_EN_CLIENT_START -> {
                if (resultCode == RESULT_OK) {
                    // The resolution is solved (for example, the user gave consent).
                    // Start ExposureNotificationsClient again.
                    ExposureNotificationManager.start(this)
                } else {
                    // The resolution was rejected or cancelled.
                }
            }
            RequestCode.REQUEST_UPDATE_APP -> {
                if (resultCode != RESULT_OK) {
                    // The resolution was rejected or cancelled.
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RequestCode.REQUEST_CAMERA_PERMISSION -> {
                if (!PermissionUtils.provideLinkToSettingIfNeeded(this)) {
                    gotoBarcodePage()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}

enum class RiskStatus {
    NORMAL, RISKY, UNKNOWN
}
