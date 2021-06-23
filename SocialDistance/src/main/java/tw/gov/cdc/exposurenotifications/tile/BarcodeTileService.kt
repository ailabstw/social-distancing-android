package tw.gov.cdc.exposurenotifications.tile

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import tw.gov.cdc.exposurenotifications.activity.BarcodeScanningActivity
import tw.gov.cdc.exposurenotifications.common.Log

@RequiresApi(Build.VERSION_CODES.N)
class BarcodeTileService : TileService() {

    companion object {
        private const val TAG = "BarcodeTileService"
    }

    override fun onClick() {
        super.onClick()
        try {
            startActivityAndCollapse(Intent(this, BarcodeScanningActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Log.w(TAG, "$e")
        }
    }
}