package tw.gov.cdc.exposurenotifications.hcert

import com.upokecenter.cbor.CBORObject
import tw.gov.cdc.exposurenotifications.hcert.data.CborObject

object CwtHelper {
    fun fromCbor(input: ByteArray): CwtAdapter = CwtAdapter(input)
}

class CwtAdapter(input: ByteArray) {

    private val map = CBORObject.DecodeFromBytes(input)

    fun getString(key: Int) = map[key]?.AsString()

    fun getNumber(key: Int) = map[key]?.AsInt64() as Number?

    fun getMap(key: Int): CwtAdapter? {
        if (!map.ContainsKey(key)) return null
        return try {
            CwtAdapter(map[key].GetByteString())
        } catch (e: Throwable) {
            CwtAdapter(map[key].EncodeToBytes())
        }
    }

    fun toCborObject(): CborObject = JvmCborObject(map)

    internal class JvmCborObject(private val cbor: CBORObject) : CborObject {
        override fun toJsonString(): String = cbor.ToJSONString()

        //if not present in object structure, this is technically a schema issue and we therefore do not handle it here
        override fun getVersionString() = try {
            cbor["ver"]?.AsString()
        } catch (t: Throwable) {
            null
        }
    }
}
