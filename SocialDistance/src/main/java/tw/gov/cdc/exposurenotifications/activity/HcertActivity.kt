package tw.gov.cdc.exposurenotifications.activity

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_hcert.*
import tw.gov.cdc.exposurenotifications.R


class HcertActivity : BaseActivity() {

    companion object {
        private const val TAG = "HcertActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hcert)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
}
