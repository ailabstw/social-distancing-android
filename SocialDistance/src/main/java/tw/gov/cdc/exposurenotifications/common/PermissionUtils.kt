package tw.gov.cdc.exposurenotifications.common

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import tw.gov.cdc.exposurenotifications.R

object PermissionUtils {

    fun requestCameraPermissionIfNeeded(activity: Activity, @StringRes message: Int, finishOnDeny: Boolean = false): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                false
            }
            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.barcode_camera_permission_dialog_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.deny) { _, _ ->
                        if (finishOnDeny) {
                            activity.finish()
                        }
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            RequestCode.REQUEST_CAMERA_PERMISSION
                        )
                    }
                    .setCancelable(false)
                    .create()
                    .show()
                true
            }
            else -> {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    RequestCode.REQUEST_CAMERA_PERMISSION
                )
                true
            }
        }
    }

    fun provideLinkToSettingIfNeeded(activity: Activity, @StringRes message: Int, finishOnDeny: Boolean = false): Boolean {
        return when {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                false
            }
            activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                false
            }
            else -> {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.barcode_camera_permission_dialog_title)
                    .setMessage(message)
                    .setNegativeButton(R.string.deny) { _, _ ->
                        if (finishOnDeny) {
                            activity.finish()
                        }
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.packageName, null)
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.let {
                            activity.startActivity(it)
                        }
                    }
                    .setCancelable(false)
                    .create()
                    .show()
                true
            }
        }
    }
}