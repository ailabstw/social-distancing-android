package tw.gov.cdc.exposurenotifications.hcert.decode

/**
 * Encodes/decodes input in/from Base45
 */
class Base45Service {

    private val encoder = Base45Encoder

    fun decode(input: String): ByteArray {
        try {
            return Base45Encoder.decode(input)
        } catch (e: Throwable) {
            throw VerificationException(Error.BASE_45_DECODING_FAILED, cause = e)
        }
    }

}
