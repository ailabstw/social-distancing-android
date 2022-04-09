package tw.gov.cdc.exposurenotifications.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_hcert.*
import tw.gov.cdc.exposurenotifications.R


class HcertActivity : BaseActivity() {

    companion object {
        private const val TAG = "HcertActivity"
    }

    private val emptyViewGroup by lazy { hcert_empty_view_group }
    private val buttonAdd by lazy { hcert_add_button }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hcert)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        buttonAdd.setOnClickListener {
            Intent(this, BarcodeScanningActivity::class.java).apply {
                putExtra(BarcodeScanningActivity.EXTRA_HCERT_MODE, true)
            }.let(::startActivity)
        }
    }

    // ActionBar

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.hcert, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_hcert_apply -> {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.HCERT_APPLY))
            true
        }
        R.id.action_vaccine_appointment -> {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.HCERT_VACCINE_APPOINTMENT))
            true
        }
        R.id.action_faq -> {
            startActivity(WebViewActivity.getIntent(this, WebViewActivity.Page.HCERT_FAQ))
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
