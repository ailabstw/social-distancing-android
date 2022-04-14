package tw.gov.cdc.exposurenotifications.hcert.ui

import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

interface HcertDetailActionHandler {

    fun onHcertDelete(hcert: GreenCertificate, position: Int)
}