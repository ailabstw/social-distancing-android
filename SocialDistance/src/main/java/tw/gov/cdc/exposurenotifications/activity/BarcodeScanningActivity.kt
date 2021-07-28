package tw.gov.cdc.exposurenotifications.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
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
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.common.PermissionUtils
import tw.gov.cdc.exposurenotifications.common.RequestCode
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScanningActivity : BaseActivity() {

    companion object {
        private const val TAG = "BarcodeScanningActivity"
    }

    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var cameraExecutor: ExecutorService

    private val previewView by lazy { barcode_scanning_previewView }
    private val hintText by lazy { barcode_scanning_hint_text }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanning)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!isTaskRoot)
            setDisplayShowHomeEnabled(!isTaskRoot)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

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
                barcodes.firstOrNull {
                    it.sms?.phoneNumber == "1922"
                }?.also {
                    setHintTextVisible(visible = false, force = true)
                    imageAnalysis?.clearAnalyzer()
                    imageProxy.close()
                    gotoSendSMS(it.sms!!)
                } ?: run {
                    setHintTextVisible(barcodes.isNotEmpty())
                    imageProxy.close()
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

    private fun setHintTextVisible(visible: Boolean, force: Boolean = false) {
        val now = Date().time
        if (force || visible || now - lastUpdateHintTime > 300) {
            hintText.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            lastUpdateHintTime = now
        }
    }

    // Send SMS

    private fun gotoSendSMS(sms: Barcode.Sms) {
        Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${sms.phoneNumber}")).apply {
            putExtra("sms_body", sms.message)
        }.let {
            startActivity(it)
        }
        finish()
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