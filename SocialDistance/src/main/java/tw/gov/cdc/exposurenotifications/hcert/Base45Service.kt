package tw.gov.cdc.exposurenotifications.hcert

/**
 * Encodes/decodes input in/from Base45
 */
class Base45Service {

    private val encoder = Base45Encoder

    fun decode(input: String): ByteArray {
        try {
            return encoder.decode(input)
        } catch (e: Throwable) {
            throw VerificationException(Error.BASE_45_DECODING_FAILED, cause = e)
        }
    }

}
