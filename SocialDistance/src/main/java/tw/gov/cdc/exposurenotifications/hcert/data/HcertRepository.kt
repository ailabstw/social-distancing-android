package tw.gov.cdc.exposurenotifications.hcert.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import tw.gov.cdc.exposurenotifications.common.PreferenceManager

class HcertRepository {

    private var _hcertsCache: MutableList<String>
    private val _hcerts: MutableLiveData<List<String>>
    val hcerts: LiveData<List<String>>

    init {
        val certs = PreferenceManager.hcerts.map {
            val (index, hcert) = it.split("_", ignoreCase = false, limit = 2)
            (index.toInt()) to hcert
        }.sortedBy {
            it.first
        }.map {
            it.second
        }

        _hcertsCache = certs.toMutableList()
        _hcerts = MutableLiveData(certs)
        hcerts = _hcerts
    }

    private fun updateHcerts() {
        PreferenceManager.hcerts = _hcertsCache.mapIndexed { index, hcert ->
            "${index}_${hcert}"
        }.toSet()
        _hcerts.value = _hcertsCache
    }

    // TODO: Check duplicate
    fun addHcert(hcert: String): Boolean {
        _hcertsCache.apply { add(hcert) }
        updateHcerts()
        return true
    }

    fun removeAt(index: Int) {
        _hcertsCache.apply { removeAt(index) }
        updateHcerts()
    }

    private var _updateJob: Job? = null

    fun itemMoved(from: Int, to: Int) = CoroutineScope(Dispatchers.Main).launch {
        _hcertsCache.apply {
            val item = removeAt(from)
            add(to, item)
        }

        // delay update due to this function can be called rapidly
        // update live data rapidly will have bad impact on rendering
        _updateJob?.cancelAndJoin()
        _updateJob = launch {
            delay(500)
            updateHcerts()
        }
    }
}