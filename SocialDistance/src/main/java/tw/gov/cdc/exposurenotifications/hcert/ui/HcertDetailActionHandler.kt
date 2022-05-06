package tw.gov.cdc.exposurenotifications.hcert.ui

interface HcertDetailActionHandler {

    fun onHcertDelete(hcert: HcertModel, position: Int)
}