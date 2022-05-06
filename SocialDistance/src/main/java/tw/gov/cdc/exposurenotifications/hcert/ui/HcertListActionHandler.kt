package tw.gov.cdc.exposurenotifications.hcert.ui

interface HcertListActionHandler {

    fun onHcertClick(position: Int)

    fun onDragStart(viewHolder: HcertListViewHolder)
}