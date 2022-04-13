package tw.gov.cdc.exposurenotifications.hcert.ui

import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

interface HcertMainActionHandler {

    fun onHcertClick(hcert: GreenCertificate)
}