package tw.gov.cdc.exposurenotifications.hcert.ui

import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

interface HcertListActionHandler {

    fun onHcertClick(hcert: GreenCertificate, position: Int)

    fun onDragStart(viewHolder: HcertListViewHolder)
}