package tw.gov.cdc.exposurenotifications.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.nearby.exposurenotification.DailySummary
import tw.gov.cdc.exposurenotifications.common.PreferenceManager
import tw.gov.cdc.exposurenotifications.common.Utils.daysSinceEpoch
import tw.gov.cdc.exposurenotifications.common.Utils.setDaysSinceEpoch
import tw.gov.cdc.exposurenotifications.nearby.ExposureNotificationManager
import java.text.DateFormat
import java.util.*

class DailySummaryViewModel : ViewModel() {

    companion object {
        private const val TAG = "DailySummaryViewModel"
    }

    private val _allItems = MutableLiveData<List<SummaryItem>>()

    val allItems: LiveData<List<SummaryItem>> = _allItems

    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

    init {
        updateItems(ExposureNotificationManager.dailySummaries.value!!)

    }

    fun updateItems(rowData: List<DailySummary>) {

        val mutableList = mutableListOf<SummaryItem.Day>()

        val safeSummaries = PreferenceManager.safeSummaries
        val currentSummary = rowData.map {
            it.daysSinceEpoch to it.summaryData.weightedDurationSum
        }.toMap()

        val calendar = Calendar.getInstance()
        val daysSinceEpoch = calendar.daysSinceEpoch()
        val alarmPeriod = PreferenceManager.riskAlarmPeriod.let {
            val today = Calendar.getInstance().daysSinceEpoch()
            today - it..today
        }

        for (i in daysSinceEpoch downTo daysSinceEpoch - 14) {
            val currentSum = currentSummary[i] ?: 0.0
            val safeSum = safeSummaries[i] ?: 0.0

            mutableList.add(SummaryItem.Day(
                dateString = dateFormat.format(calendar.setDaysSinceEpoch(i).time),
                exposureSeconds = currentSum.toInt(),
                isInRisk = i in alarmPeriod && currentSum - safeSum >= 120.0)
            )
        }
        mutableList.last().isLastOne = true

        _allItems.apply {
            value = mutableList.toList()
        }
    }

    sealed class SummaryItem {
        data class Day(val dateString: String, val exposureSeconds: Int, val isInRisk: Boolean, var isLastOne: Boolean = false) : SummaryItem()

        val viewType: Int
            get() {
                return when (this) {
                    is Day -> DailySummaryAdapter.ViewType.DAY.value
                }
            }
    }
}