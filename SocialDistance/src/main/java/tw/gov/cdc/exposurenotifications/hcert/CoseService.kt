package tw.gov.cdc.exposurenotifications.hcert



/**
 * Encodes/decodes input as a Sign1Message according to COSE specification (RFC8152)
 */
class CoseService {

    fun decode(input: ByteArray): ByteArray {
        val coseAdapter = CoseAdapter(input)

        return coseAdapter.getContent()
    }

}
