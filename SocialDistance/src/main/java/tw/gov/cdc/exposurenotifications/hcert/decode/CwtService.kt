package tw.gov.cdc.exposurenotifications.hcert.decode

import tw.gov.cdc.exposurenotifications.hcert.decode.data.CborObject
import java.util.*

/**
 * Encodes/decodes input as a CWT structure, ready to sign with COSE
 */
class CwtService {

    fun decode(input: ByteArray): CborObject {
        try {
            val now = Date()
            val map = CwtHelper.fromCbor(input)

            val issuedAtMilliSeconds = map.getNumber(CwtHeaderKeys.ISSUED_AT.intVal)?.let { it.toLong() * 1000 }
                ?: throw VerificationException(Error.CWT_EXPIRED, details = mapOf("issuedAt" to "null"))

            if (issuedAtMilliSeconds > now.time) {
                throw VerificationException(
                    Error.CWT_NOT_YET_VALID, details = mapOf(
                        "issuedAt" to Date(issuedAtMilliSeconds).toString(),
                        "currentTime" to now.toString()
                    )
                )
            }

            val expirationMilliSeconds = map.getNumber(CwtHeaderKeys.EXPIRATION.intVal)?.let { it.toLong() * 1000 }
                ?: throw VerificationException(Error.CWT_EXPIRED, details = mapOf("expirationTime" to "null"))

            if (expirationMilliSeconds < now.time) {
                throw VerificationException(
                    Error.CWT_EXPIRED, details = mapOf(
                        "expirationTime" to Date(expirationMilliSeconds).toString(),
                        "currentTime" to now.toString()
                    )
                )
            }

            val hcert: CwtAdapter = map.getMap(CwtHeaderKeys.HCERT.intVal)
                ?: throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, "CWT contains no HCERT")

            val dgc = hcert.getMap(CwtHeaderKeys.EUDGC_IN_HCERT.intVal)
                ?: throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, "CWT contains no EUDGC")

            return dgc.toCborObject()
        } catch (e: VerificationException) {
            throw e
        } catch (e: Throwable) {
            throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, e.message, e)
        }
    }

}
