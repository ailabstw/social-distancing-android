package tw.gov.cdc.exposurenotifications.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_control.view.*
import tw.gov.cdc.exposurenotifications.R
import tw.gov.cdc.exposurenotifications.activity.ControlViewModel.ControlItem

class ControlAdapter :
    ListAdapter<ControlItem, ControlAdapter.ViewHolder>(ControlDiffCallback()) {

    abstract class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bindTo(item: ControlItem)
    }

    class ControlViewHolder(v: View) : ViewHolder(v) {
        private val text = v.control_item_text
        private val switch = v.control_item_switch
        private val arrow = v.control_item_arrow
        private val divider = v.control_item_divider

        override fun bindTo(item: ControlItem) {
            when (item) {
                is ControlItem.Switch -> {
                    text.setText(item.textRes)
                    itemView.setOnClickListener(null)

                    switch.setOnCheckedChangeListener(null)
                    switch.isChecked = item.isChecked
                    switch.isEnabled = item.isEnabled
                    switch.setOnCheckedChangeListener(item.onCheckedChangeListener)

                    switch.visibility = View.VISIBLE
                    arrow.visibility = View.INVISIBLE
                    divider.visibility = if (item.isLastOne) View.INVISIBLE else View.VISIBLE
                }
                is ControlItem.Page -> {
                    text.setText(item.textRes)
                    itemView.setOnClickListener { item.onClick() }

                    switch.visibility = View.INVISIBLE
                    arrow.visibility = View.VISIBLE
                    divider.visibility = if (item.isLastOne) View.INVISIBLE else View.VISIBLE
                }
            }
        }
    }

    class ControlDiffCallback : DiffUtil.ItemCallback<ControlItem>() {
        override fun areItemsTheSame(oldItem: ControlItem, newItem: ControlItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ControlItem, newItem: ControlItem): Boolean =
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
        SWITCH, PAGE;

        fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            return ControlViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_control, parent, false)
            )
        }

        val value: Int = ordinal
    }
}