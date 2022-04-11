package tw.gov.cdc.exposurenotifications.hcert.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_control.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.BaseActivity
import tw.gov.cdc.exposurenotifications.activity.WebViewActivity


class HcertActivity : BaseActivity() {

    companion object {
        private const val TAG = "HcertActivity"
    }

    private val viewModel by viewModels<HcertViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hcert)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        if (savedInstanceState == null) {
            showMainFragment()
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

    private fun showMainFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, HcertMainFragment())
            .commit()
    }
}
