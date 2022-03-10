package tw.gov.cdc.exposurenotifications.hcert

import COSE.MessageTag
import COSE.Sign1Message
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

class CoseAdapter (input: ByteArray) {

    private val sign1Message = try {
        Sign1Message.DecodeFromBytes(augmentedInput(input), MessageTag.Sign1) as Sign1Message
    } catch (t: Throwable) {
        throw VerificationException(Error.SIGNATURE_INVALID, cause = t)
    }

    init {
        Security.addProvider(BouncyCastleProvider()) // for SHA256withRSA/PSS
    }


    fun getContent() = sign1Message.GetContent()

    /**
     * Input may be missing COSE Tag 0xD2 = 18 = cose-sign1.
     * But we need this, to cast the parsed object to [Cbor.Tagged].
     * So we'll add the Tag to the input.
     *
     * It may also be tagged as a CWT (0xD8, 0x3D) and a Sign1 (0xD2).
     * But the library expects only one tag.
     * So we'll strip the CWT tag from the input.
     */
    private fun augmentedInput(input: ByteArray): ByteArray {
        if (input.size >= 1 && isArray(input[0]))
            return byteArrayOf(0xD2.toByte()) + input
        if (input.size >= 3 && isCwt(input[0], input[1]) && isSign1(input[2])) {
            return input.drop(2).toByteArray()
        }
        return input
    }

    private fun isSign1(byte: Byte) = byte == 0xD2.toByte()

    private fun isCwt(firstByte: Byte, secondByte: Byte) = firstByte == 0xD8.toByte() && secondByte == 0x3D.toByte()

    private fun isArray(byte: Byte) = byte == 0x84.toByte()

}
