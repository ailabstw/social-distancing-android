package tw.gov.cdc.exposurenotifications.hcert.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tw.gov.cdc.exposurenotifications.BaseApplication

val inputPaths = sequenceOf(
    "value-sets/disease-agent-targeted.json",
    "value-sets/test-manf.json",
    "value-sets/test-type.json",
    "value-sets/test-result.json",
    "value-sets/vaccine-mah-manf.json",
    "value-sets/vaccine-medicinal-product.json",
    "value-sets/vaccine-prophylaxis.json",
    "value-sets/country-2-codes.json",
)

/**
 * Holds a list of value sets, that get lazily loaded from the JSON files in "src/main/resources/value-sets".
 */
data class ValueSetHolder(
    val valueSets: List<ValueSet>
) {
    fun find(valueSetId: String, key: String): ValueSetEntryAdapter {
        // we'll trim it, to work around entries containing whitespace
        val trimmed = key.trim()
        return valueSets.firstOrNull { it.valueSetId == valueSetId }
            ?.valueSetValues?.get(trimmed)?.let { ValueSetEntryAdapter(trimmed, it) }
            ?: return buildMissingValueSetEntry(trimmed, valueSetId)
    }

    fun find(key: String): ValueSetEntryAdapter {
        val trimmed = key.trim()
        valueSets.forEach {
            if (it.valueSetValues.containsKey(trimmed))
                return ValueSetEntryAdapter(trimmed, it.valueSetValues[trimmed]!!)
        }
        return buildMissingValueSetEntry(trimmed, null)
    }

    private fun buildMissingValueSetEntry(key: String, valueSetId: String?): ValueSetEntryAdapter {
        val entry = ValueSetEntry(key, "en", false, "http://example.com/missing", "0", valueSetId)
        return ValueSetEntryAdapter(key, entry)
    }

}

object ValueSetsInstanceHolder {

    val INSTANCE: ValueSetHolder by lazy {
        val seq: List<ValueSet> = inputPaths
            .map { BaseApplication.instance.resources.assets.open(it) }
            .map { it.bufferedReader().use { it.readText() } }.toList()
            .map { Json.decodeFromString(it) }
        ValueSetHolder(seq.toList())
    }

}

@Serializable
data class ValueSet constructor(
    val valueSetId: String,
    val valueSetDate: String,
    val valueSetValues: Map<String, ValueSetEntry>
)

@Serializable
data class ValueSetEntry(
    val display: String,
    val lang: String,
    val active: Boolean,
    val system: String,
    val version: String,
    val valueSetId: String? = null
)

@Serializable(with = ValueSetEntryAdapterSerializer::class)
data class ValueSetEntryAdapter(
    val key: String,
    val valueSetEntry: ValueSetEntry
)
