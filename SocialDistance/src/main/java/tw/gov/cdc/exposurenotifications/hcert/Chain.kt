package tw.gov.cdc.exposurenotifications.hcert

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import tw.gov.cdc.exposurenotifications.hcert.data.CborObject
import tw.gov.cdc.exposurenotifications.hcert.data.GreenCertificate


/**
 * Main entry point for the decoding of HCERT data from QR codes
 */
object Chain {

    private val cwtService = CwtService()
    private val coseService = CoseService()
    private val compressorService = CompressorService()
    private val base45Service = Base45Service()
    private val contextIdentifierService = ContextIdentifierService()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Process the [input], apply decoding in this order:
     * - [ContextIdentifierService]
     * - [Base45Service]
     * - [CompressorService]
     * - [CoseService]
     * - [CwtService]
     */
    fun decode(input: String): GreenCertificate {

        val rawEuGcc: String?
        val cborObj: CborObject?
        val cwt: ByteArray?
        val cose: ByteArray?
        val compressed: ByteArray?
        val encoded: String?

        encoded = contextIdentifierService.decode(input)
        compressed = base45Service.decode(encoded)
        cose = compressorService.decode(compressed)
        cwt = coseService.decode(cose)
        cborObj = cwtService.decode(cwt)
        rawEuGcc = cborObj.toJsonString()

        return json.decodeFromString(rawEuGcc)
    }
}
