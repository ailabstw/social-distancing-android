package tw.gov.cdc.exposurenotifications.hcert.ui

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_hcert_list.view.*
import tw.gov.cdc.exposurenotifications.R

class HcertListAdapter(
    private val actionHandler: HcertListActionHandler
) : ListAdapter<HcertModel, HcertListViewHolder>(HcertListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HcertListViewHolder {
        return HcertListViewHolder.createViewHolder(parent, viewType, actionHandler)
    }

    override fun onBindViewHolder(holder: HcertListViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}

sealed class HcertListViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    abstract fun bind(item: HcertModel, position: Int)

    class HcertListHolder(v: View, private val actionHandler: HcertListActionHandler) : HcertListViewHolder(v) {

        private val name by lazy { v.item_hcert_list_name }
        private val lastDoseDate by lazy { v.item_hcert_list_last_dose_date }
        private val reorderIcon by lazy { v.item_hcert_list_reorder_icon }

        override fun bind(item: HcertModel, position: Int) {
            name.text = item.name
            lastDoseDate.text = lastDoseDate.context.getString(
                R.string.hcert_last_dose_date, item.dateOfVaccination
            )

            reorderIcon.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    actionHandler.onDragStart(this)
                }
                return@setOnTouchListener false
            }

            itemView.setOnClickListener {
                actionHandler.onHcertClick(adapterPosition)
            }
        }

        companion object {
            private val regex = """[a-zA-Z]""".toRegex()
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup, viewType: Int, actionsHandler: HcertListActionHandler): HcertListViewHolder {
            return HcertListHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_hcert_list, parent, false),
                actionsHandler
            )
        }
    }
}

class HcertListDiffCallback : DiffUtil.ItemCallback<HcertModel>() {
    override fun areItemsTheSame(oldItem: HcertModel, newItem: HcertModel): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: HcertModel, newItem: HcertModel): Boolean = oldItem == newItem
}
