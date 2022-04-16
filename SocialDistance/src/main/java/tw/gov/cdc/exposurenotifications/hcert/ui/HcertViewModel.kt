package tw.gov.cdc.exposurenotifications.hcert.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    val isEmpty: LiveData<Boolean> = Transformations.map(allItems) {
        it.isEmpty()
    }

    fun deleteAt(position: Int) {
        _repository.removeAt(position)
    }

    fun updatePosition(newPosition: Int) {
        if (_currentPosition.value != newPosition) {
            _currentPosition.value = newPosition
        }
    }

    private val regex = """[a-zA-Z]""".toRegex()

    private fun createHcertModel(hcert: GreenCertificate): HcertModel {
        val name: String
        val nameTransliterated: String

        hcert.subject.run {
            if (familyName?.contains(regex) == true || givenName?.contains(regex) == true) {
                name = "$givenName $familyName"
                nameTransliterated = "${givenNameTransliterated?.replace("<", "-")} ${familyNameTransliterated.replace("<", "-")}"
            } else {
                name = "$familyName$givenName"
                nameTransliterated = "${familyNameTransliterated.replace("<", "-")}, ${givenNameTransliterated?.replace("<", "-")}"
            }
        }

        return hcert.vaccinations!!.firstNotNullOf { it }.run {
            HcertModel(
                name = name,
                nameTransliterated = nameTransliterated,
                dateOfBirth = hcert.dateOfBirthString.replace('-', '.'),
                targetDisease = target.valueSetEntry.display,
                vaccine = vaccine.valueSetEntry.display,
                medicinalProduct = medicinalProduct.valueSetEntry.display,
                authorizationHolder = authorizationHolder.valueSetEntry.display,
                doseState = "${doseNumber}/${doseTotalNumber}",
                dateOfVaccination = dateString.replace('-', '.'),
                country = country.valueSetEntry.display,
                certificateIssuer = certificateIssuer,
                certificateIdentifier = certificateIdentifier,
                rawString = hcert.rawString
            )
        }
    }
}