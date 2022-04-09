package tw.gov.cdc.exposurenotifications.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.activity_web_view.*
import tw.gov.cdc.exposurenotifications.R

class WebViewActivity : BaseActivity() {

    enum class Page(
        @StringRes val url: Int,
        @StringRes val label: Int? = null,
        val lockNavigation: Boolean = false
    ) {
        PRIVACY(url = R.string.url_privacy, label = R.string.menu_privacy, lockNavigation = true),
        FAQ(url = R.string.url_faq),
        HCERT_APPLY(url = R.string.url_hcert_apply),
        HCERT_VACCINE_APPOINTMENT(url = R.string.url_hcert_vaccine_appointment),
        HCERT_FAQ(url = R.string.url_hcert_faq)
    }

    companion object {
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_LABEL = "EXTRA_LABEL"
        const val EXTRA_LOCK_NAVIGATION = "EXTRA_LOCK_NAVIGATION"

        fun getIntent(context: Context, page: Page): Intent {
            return when (page) {
                Page.PRIVACY -> {
                    getIntent(
                        context = context,
                        url = page.url,
                        label = page.label!!,
                        lockNavigation = page.lockNavigation
                    )
                }
                else -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(page.url)))
                }
            }
        }

        private fun getIntent(
            context: Context,
            @StringRes url: Int,
            @StringRes label: Int,
            lockNavigation: Boolean
        ): Intent {
            return Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, context.getString(url))
                putExtra(EXTRA_LABEL, context.getString(label))
                putExtra(EXTRA_LOCK_NAVIGATION, lockNavigation)
            }
        }
    }

    var lockNavigate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = intent.getStringExtra(EXTRA_LABEL)
        }

        lockNavigate = intent.getBooleanExtra(EXTRA_LOCK_NAVIGATION, false)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                if (!lockNavigate) {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                webView_progressBar.visibility = View.GONE
                super.onPageFinished(view, url)
            }
        }
        webView.settings.javaScriptEnabled = true
        intent.getStringExtra(EXTRA_URL)?.let { webView.loadUrl(it) }
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
}