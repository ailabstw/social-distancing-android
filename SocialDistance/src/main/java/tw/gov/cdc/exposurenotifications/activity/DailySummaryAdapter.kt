package tw.ailabs.Yating.Transcriber.fragment.dialog.recording

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.daily_summary_item_view.view.*
import tw.ailabs.Yating.Transcriber.fragment.dialog.recording.DailySummaryViewModel.SummaryItem
import tw.gov.cdc.exposurenotifications.R

class DailySummaryAdapter :
    ListAdapter<SummaryItem, DailySummaryAdapter.ViewHolder>(RecordingDiffCallback()) {

    abstract class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bindTo(item: SummaryItem)
    }

    class SummaryViewHolder(v: View) : ViewHolder(v) {
        private val dateText = v.daily_summary_item_date_text
        private val summaryText = v.daily_summary_item_summary_text
        private val dividerView = v.daily_summary_item_divider_view

        override fun bindTo(item: SummaryItem) {
            if (item is SummaryItem.Day) {
                dateText.text = item.dateString
                summaryText.text = when (item.exposureSeconds) {
                    0 -> {
                        itemView.context.getString(R.string.daily_summary_no_contact)
                    }
                    in 1 until 120 -> {
                        itemView.context.getString(R.string.daily_summary_less_then_2_mins)
                    }
                    in 120 until 3600 -> {
                        val min = item.exposureSeconds / 60
                        itemView.context.getString(R.string.daily_summary_mins, min)
                    }
                    else -> {
                        val exposureMinutes = item.exposureSeconds / 60
                        val min = exposureMinutes % 60
                        val hr = exposureMinutes / 60
                        itemView.context.getString(R.string.daily_summary_hrs_mins, hr, min)
                    }
                }
                updateColor(isInRisk = item.isInRisk)
                dividerView.visibility = if (item.isLastOne) View.INVISIBLE else View.VISIBLE
            }
        }

        private fun updateColor(isInRisk: Boolean) {
            when (isInRisk) {
                true -> {
                    ContextCompat.getColor(itemView.context, R.color.text_risk_high)
                }
                false -> {
                    ContextCompat.getColor(itemView.context, R.color.text)
                }
            }.also {
                dateText.setTextColor(it)
                summaryText.setTextColor(it)
            }
        }
    }

    class RecordingDiffCallback : DiffUtil.ItemCallback<SummaryItem>() {
        override fun areItemsTheSame(oldItem: SummaryItem, newItem: SummaryItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: SummaryItem, newItem: SummaryItem): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewType.values()[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    enum class ViewType {
        DAY {
            override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
                return SummaryViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.daily_summary_item_view, parent, false)
                )
            }
        };

        abstract fun onCreateViewHolder(parent: ViewGroup): ViewHolder

        val value: Int = ordinal
    }
}