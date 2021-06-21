package tw.gov.cdc.exposurenotifications.activity

import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_control.toolbar
import kotlinx.android.synthetic.main.activity_daily_summary.*
import tw.ailabs.Yating.Transcriber.fragment.dialog.recording.DailySummaryAdapter
import tw.ailabs.Yating.Transcriber.fragment.dialog.recording.DailySummaryViewModel
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.Utils.getDateString
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import java.util.*

class DailySummaryActivity : BaseActivity() {

    private val lastCheckTimeText by lazy { daily_summary_last_check_time_text }
    private val recyclerView by lazy { daily_summary_recycler_view }

    private val viewModel: DailySummaryViewModel by lazy {
        ViewModelProvider(this)
            .get(DailySummaryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_summary)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        lastCheckTimeText.text = if (PreferenceManager.lastCheckTime != 0L) {
            resources.getString(
                R.string.main_info_last_check_time,
                getDateString(Date().apply {
                    time = PreferenceManager.lastCheckTime
                })
            )
        } else {
            " "
        }
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

    // Recycler View

    private fun setupRecyclerView() {
        val dailySummaryAdapter = DailySummaryAdapter()

        recyclerView.apply {
            adapter = dailySummaryAdapter
            layoutManager = LinearLayoutManager(this@DailySummaryActivity)
        }

        ExposureNotificationManager.dailySummaries.observe(this, {
            viewModel.updateItems(it)
        })

        viewModel.allItems.observe(this, {
            dailySummaryAdapter.submitList(it)
        })
    }
}