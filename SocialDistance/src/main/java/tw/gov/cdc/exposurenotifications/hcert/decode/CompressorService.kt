package tw.gov.cdc.exposurenotifications.hcert.decode


/**
 * Compresses/decompresses input with ZLIB, [level] specifies the compression level (0-9)
 */
class CompressorService (private val level: Int = 9) {

    private val adapter = CompressorAdapter()

    /**
     * Decompresses input with ZLIB = inflating.
     */
    fun decode(input: ByteArray): ByteArray {
        try {
            return adapter.decode(input)
        } catch (e: Throwable) {
            throw VerificationException(Error.DECOMPRESSION_FAILED, cause = e)
        }
    }

}
