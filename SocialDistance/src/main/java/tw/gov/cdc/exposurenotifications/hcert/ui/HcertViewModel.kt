package tw.gov.cdc.exposurenotifications.hcert.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.hcert.decode.Chain
import tw.gov.cdc.exposurenotifications.hcert.decode.VerificationException
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate

class HcertViewModel : ViewModel() {

    companion object {
        private const val TAG = "HcertViewModel"
    }

    private val _repository = BaseApplication.instance.hcertRepository

    val allItems: LiveData<List<GreenCertificate>> = Transformations.map(_repository.hcerts) {
        it.fold(mutableListOf()) { acc, hcert ->
            try {
                acc.add(Chain.decode(hcert))
            } catch (e: VerificationException) {
                // TODO: Show hint for expired hcerts
            }
            acc
        }
    }

    val isEmpty: LiveData<Boolean> = Transformations.map(allItems) {
        it.isEmpty()
    }
}