package tw.gov.cdc.exposurenotifications.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.annotation.StringRes
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_barcode_scanning.*
import kotlinx.android.synthetic.main.activity_control.toolbar
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PermissionUtils
import tw.gov.cdc.exposurenotifications.common.RequestCode
import tw.gov.cdc.exposurenotifications.hcert.data.HcertRepositoryError
import tw.gov.cdc.exposurenotifications.hcert.data.HcertRepositoryException
import tw.gov.cdc.exposurenotifications.hcert.decode.Chain
import tw.gov.cdc.exposurenotifications.hcert.decode.Error
import tw.gov.cdc.exposurenotifications.hcert.decode.VerificationException
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanningActivity : BaseActivity() {

    companion object {
        private const val TAG = "BarcodeScanningActivity"
        private lateinit var cameraExecutor: ExecutorService
        const val EXTRA_HCERT_MODE = "EXTRA_HCERT_MODE"
    }

    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val previewView by lazy { barcode_scanning_previewView }
    private val hintText by lazy { barcode_scanning_hint_text }

    private var hcertMode = false
    private val hcertApplyButton by lazy { barcode_hcert_apply_button }
    private val hcertImportButton by lazy { barcode_hcert_import_button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanning)
        setSupportActionBar(toolbar)

        hcertMode = intent.getBooleanExtra(EXTRA_HCERT_MODE, false)
        title = getString(if (hcertMode) R.string.hcert_full_name else R.string.menu_barcode_scanning)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!isTaskRoot)
            setDisplayShowHomeEnabled(!isTaskRoot)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        setUpHcertUI()
        setUpCamera()
    }

    override fun onStart() {
        super.onStart()
        PermissionUtils.requestCameraPermissionIfNeeded(activity = this, finishOnDeny = true)
    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
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

    // HCERT

    fun setUpHcertUI() {
        if (hcertMode) {
            hcertApplyButton.visibility = View.VISIBLE
            hcertImportButton.visibility = View.VISIBLE
        } else {
            hcertApplyButton.visibility = View.GONE
            hcertImportButton.visibility = View.GONE
        }

        hcertApplyButton.setOnClickListener {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.HCERT_APPLY))
        }
    }

    // Camera

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: ExecutionException) {
                Log.e(TAG, "bindCameraUseCases error $e")
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "bindCameraUseCases error $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(cameraExecutor, { imageProxy ->
                    scanBarcodes(imageProxy)
                })
            }

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    private fun scanBarcodes(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            )
            .build()

        BarcodeScanning.getClient(options).process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (hcertMode) {
                    var hcertError: Error? = null
                    var repositoryError: HcertRepositoryError? = null
                    barcodes.forEach {
                        it.rawValue?.let { raw ->
                            try {
                                val hcert = Chain.decode(raw)
                                hideHintText(true)
                                if (addHcert(hcert)) {
                                    return@addOnSuccessListener
                                }
                            } catch (e: VerificationException) {
                                hcertError = e.error
                            } catch (e: HcertRepositoryException) {
                                repositoryError = e.error
                            }
                        }
                    }
                    when {
                        hcertError == Error.CWT_EXPIRED -> showHintText(Hint.HINT_HCERT_EXPIRED)
                        repositoryError == HcertRepositoryError.DUPLICATED -> showHintText(Hint.HINT_HCERT_DUPLICATED)
                        barcodes.isNotEmpty() -> showHintText(Hint.HINT_HCERT_INVALID)
                        else -> hideHintText()
                    }
                    imageProxy.close()
                } else {
                    barcodes.firstOrNull {
                        it.sms?.run { phoneNumber == "1922" && !message.isNullOrBlank() } ?: false
                    }?.also {
                        hideHintText(true)
                        imageAnalysis?.clearAnalyzer()
                        gotoSendSMS(it.sms!!)
                        imageProxy.close()
                    } ?: run {
                        if (barcodes.isNotEmpty()) showHintText(Hint.HINT_1922_INVALID) else hideHintText()
                        imageProxy.close()
                    }
                }
            }
            .addOnFailureListener {
                imageProxy.close()
            }
            .addOnCanceledListener {
                imageProxy.close()
            }
    }

    // Hint Text

    private var lastUpdateHintTime = Date().time

    private fun showHintText(hint: Hint) {
        val now = Date().time
        hintText.setText(hint.res)
        hintText.visibility = View.VISIBLE
        lastUpdateHintTime = now
    }

    private fun hideHintText(force: Boolean = false) {
        val now = Date().time
        if (force || now - lastUpdateHintTime > 300) {
            hintText.visibility = View.INVISIBLE
            lastUpdateHintTime = now
        }
    }

    enum class Hint(@StringRes val res: Int) {
        HINT_1922_INVALID(R.string.barcode_hint_1922_invalid),
        HINT_HCERT_INVALID(R.string.barcode_hint_hcert_invalid),
        HINT_HCERT_DUPLICATED(R.string.barcode_hint_hcert_duplicated),
        HINT_HCERT_EXPIRED(R.string.barcode_hint_hcert_expired)
    }

    // Send SMS

    private var isSMSSent: Boolean = false

    private fun gotoSendSMS(sms: Barcode.Sms) {
        if (isSMSSent) {
            finish()
            return
        }
        isSMSSent = true
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${sms.phoneNumber}")).apply {
            putExtra("sms_body", sms.message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let {
            startActivity(it)
        }
        finish()
    }

    // Hcert

    private fun addHcert(hcert: GreenCertificate): Boolean {
        BaseApplication.instance.hcertRepository.addHcert(hcert)
        imageAnalysis?.clearAnalyzer()
        finish()
        return true
    }

    // Permission

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RequestCode.REQUEST_CAMERA_PERMISSION -> {
                if (!PermissionUtils.provideLinkToSettingIfNeeded(activity = this, finishOnDeny = true)) {
                    PermissionUtils.requestCameraPermissionIfNeeded(this, finishOnDeny = true)
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}