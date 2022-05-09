package tw.gov.cdc.exposurenotifications.hcert.decode

import tw.gov.cdc.exposurenotifications.hcert.decode.data.CborObject
import tw.gov.cdc.exposurenotifications.hcert.decode.data.CwtDecodeResult
import java.util.*

/**
 * Encodes/decodes input as a CWT structure, ready to sign with COSE
 */
class CwtService {

    fun decode(input: ByteArray, throwWhenExpired: Boolean): CwtDecodeResult {
        try {
            val now = Date()
            val map = CwtHelper.fromCbor(input)
            var isExpired = false

            fun throwIfNeeded(e: VerificationException): Long {
                if (throwWhenExpired) {
                    throw e
                } else {
                    isExpired = true
                }
                return 0L
            }

            val issuedAtMilliSeconds = map.getNumber(CwtHeaderKeys.ISSUED_AT.intVal)?.let { it.toLong() * 1000 }
                ?: throwIfNeeded(VerificationException(Error.CWT_EXPIRED, details = mapOf("issuedAt" to "null")))

            if (issuedAtMilliSeconds > now.time) {
                throwIfNeeded(
                    VerificationException(
                        Error.CWT_NOT_YET_VALID, details = mapOf(
                            "issuedAt" to Date(issuedAtMilliSeconds).toString(),
                            "currentTime" to now.toString()
                        )
                    )
                )
            }

            val expirationMilliSeconds = map.getNumber(CwtHeaderKeys.EXPIRATION.intVal)?.let { it.toLong() * 1000 }
                ?: throwIfNeeded(VerificationException(Error.CWT_EXPIRED, details = mapOf("expirationTime" to "null")))

            if (expirationMilliSeconds < now.time) {
                throwIfNeeded(
                    VerificationException(
                        Error.CWT_EXPIRED, details = mapOf(
                            "expirationTime" to Date(expirationMilliSeconds).toString(),
                            "currentTime" to now.toString()
                        )
                    )
                )
            }

            val hcert: CwtAdapter = map.getMap(CwtHeaderKeys.HCERT.intVal)
                ?: throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, "CWT contains no HCERT")

            val dgc = hcert.getMap(CwtHeaderKeys.EUDGC_IN_HCERT.intVal)
                ?: throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, "CWT contains no EUDGC")

            return CwtDecodeResult(dgc.toCborObject(), isExpired, issuedAtMilliSeconds)
        } catch (e: VerificationException) {
            throw e
        } catch (e: Throwable) {
            throw VerificationException(Error.CBOR_DESERIALIZATION_FAILED, e.message, e)
        }
    }
}
