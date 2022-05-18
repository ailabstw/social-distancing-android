package tw.gov.cdc.exposurenotifications.hcert.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import tw.gov.cdc.exposurenotifications.BaseApplication
import tw.gov.cdc.exposurenotifications.hcert.decode.Chain
import tw.gov.cdc.exposurenotifications.hcert.decode.VerificationException
import tw.gov.cdc.exposurenotifications.hcert.decode.data.GreenCertificate
import java.text.SimpleDateFormat
import java.util.*

class HcertViewModel : ViewModel() {

    companion object {
        private const val TAG = "HcertViewModel"
    }

    private val _repository = BaseApplication.instance.hcertRepository
    val canAdd: Boolean get() = _repository.canAdd

    val allItems: LiveData<List<HcertModel>> = Transformations.map(_repository.hcerts) {
        it.fold<String, MutableList<HcertModel>>(mutableListOf()) { acc, hcert ->
            try {
                acc.add(createHcertModel(Chain.decode(hcert, false)))
            } catch (e: VerificationException) { }
            acc
        }.also { calculateItemChanged(it.size) }
    }

    private var itemCount = allItems.value?.size ?: 0

    private val _autoScroll = MutableLiveData<Boolean>()
    val autoScroll: LiveData<Boolean> = _autoScroll

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

    fun updateAutoScroll(isAuto: Boolean) {
        if (_autoScroll.value != isAuto) {
            _autoScroll.value = isAuto
        }
    }

    private val regex = """[a-zA-Z]""".toRegex()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    private fun createHcertModel(hcert: GreenCertificate): HcertModel {
        val name: String
        val nameTransliterated: String

        hcert.subject.run {
            val fNameTransliterated = familyNameTransliterated.replace("<", "-")
            val gNameTransliterated = givenNameTransliterated?.replace("<", "-") ?: ""
            if (familyName?.contains(regex) == true || givenName?.contains(regex) == true) {
                nameTransliterated = "$gNameTransliterated $fNameTransliterated".trim(' ', ',', '-').takeIf { it.contains(regex) } ?: "-"
                name = "${givenName ?: ""} ${familyName ?: ""}".trim(' ', ',', '-').takeIf { it.isNotEmpty() } ?: nameTransliterated
            } else {
                nameTransliterated = "$fNameTransliterated, $gNameTransliterated".trim(' ', ',', '-').takeIf { it.contains(regex) } ?: "-"
                name = "${familyName ?: ""}${givenName ?: ""}".trim(' ', ',', '-').takeIf { it.isNotEmpty() } ?: nameTransliterated
            }
        }

        return hcert.vaccinations!!.firstNotNullOf { it }.run {
            HcertModel(
                isExpired = hcert.isExpired,
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
                issueDate = dateFormat.format(hcert.issuedAtMilliSeconds),
                rawString = hcert.rawString
            )
        }
    }

    private fun calculateItemChanged(newCount: Int) {
        val oldCount = itemCount
        itemCount = newCount
        _autoScroll.value = oldCount != 0 && oldCount < itemCount
    }
}