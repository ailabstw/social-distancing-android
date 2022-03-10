package tw.gov.cdc.exposurenotifications.hcert

import tw.gov.cdc.exposurenotifications.hcert.CompressionConstants.MAX_DECOMPRESSED_SIZE
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.InflaterInputStream

class CompressorAdapter {

    fun decode(input: ByteArray) =
        InflaterInputStream(input.inputStream()).readBytes().also {
            val inflaterStream = InflaterInputStream(input.inputStream())
            val outputStream = ByteArrayOutputStream(DEFAULT_BUFFER_SIZE)
            inflaterStream.copyTo(outputStream)
            outputStream.toByteArray()
        }

}

object CompressionConstants {
    /**
     * Limit the byte array size after decompression to 5 MB.
     *
     * Reasoning:
     * 1. QR codes can hold at most < 4500 alphanumeric chars (https://www.qrcode.com/en/about/version.html)
     *    Sidenote: The EHN spec recommends a compression level of Q, which limits it to at most < 2500 alphanumeric chars
     * 	  (https://ec.europa.eu/health/sites/default/files/ehealth/docs/digital-green-certificates_v1_en.pdf#page=7)
     *    This is a lower bound (since any DCC should be encodable in both Aztec and QR codes).
     * 2. As an additional upper bound: base45 encodes 2 bytes into 3 chars (https://datatracker.ietf.org/doc/html/draft-faltstrom-base45-04#section-4)
     * 3.  zlib's maximum compression factor is roughly 1000:1 (http://www.zlib.net/zlib_tech.html)
     */
    const val MAX_DECOMPRESSED_SIZE = 5 * 1024 * 1024
}

// Adapted from kotlin-stdblib's kotlin.io.IOStreams.kt
private fun InflaterInputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
        // begin patch
        if (bytesCopied > MAX_DECOMPRESSED_SIZE) {
            throw IllegalArgumentException("Decompression exceeded $MAX_DECOMPRESSED_SIZE bytes, is: $bytesCopied! Input must be invalid.")
        }
        // end patch
    }
    return bytesCopied
}
