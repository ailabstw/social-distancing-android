package tw.gov.cdc.exposurenotifications.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.PreferenceManager

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            routeToAppropriatePage()
        }, 600)
    }

    private fun routeToAppropriatePage() {
        startActivity(
            when (PreferenceManager.isFirstTimeEnableNeeded) {
                false -> Intent(this, MainActivity::class.java)
                true -> {
                    Intent(this, IntroductionActivity::class.java).apply {
                        putExtra(IntroductionActivity.EXTRA_SHOW_START_BUTTON, true)
                    }
                }
            }
        )
        finish()
    }
}
