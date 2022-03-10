package tw.gov.cdc.exposurenotifications.hcert.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializer(forClass = ValueSetEntryAdapter::class)
object ValueSetEntryAdapterSerializer : KSerializer<ValueSetEntryAdapter> {

    override fun deserialize(decoder: Decoder): ValueSetEntryAdapter {
        val key = decoder.decodeString()
        return ValueSetsInstanceHolder.INSTANCE.find(key)
    }

    override fun serialize(encoder: Encoder, value: ValueSetEntryAdapter) {
        encoder.encodeString(value.key.trim())
    }
}
