package tw.gov.cdc.exposurenotifications.nearby

import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.common.collect.ImmutableList
import com.google.common.io.BaseEncoding
import org.apache.commons.io.IOUtils
import tw.gov.cdc.exposurenotifications.BuildConfig
import tw.gov.cdc.exposurenotifications.common.Log
import tw.gov.cdc.exposurenotifications.keydownload.KeyFile
import tw.gov.cdc.exposurenotifications.nearby.proto.TEKSignatureList
import tw.gov.cdc.exposurenotifications.nearby.proto.TemporaryExposureKey
import tw.gov.cdc.exposurenotifications.nearby.proto.TemporaryExposureKeyExport
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipFile

object DiagnosisKeyFileHelper {

    private const val TAG = "DiagnosisKeyFileSubmitter"
    private val BASE16: BaseEncoding = BaseEncoding.base16().lowerCase()
    private val BASE64: BaseEncoding = BaseEncoding.base64()

    fun submitFiles(keyFiles: ImmutableList<KeyFile>): Task<Void> {
        Log.d(TAG, "Providing " + keyFiles.size + " diagnosis key files to google play services.")

        if (BuildConfig.DEBUG) {
            logKeys(keyFiles, "")
        }

        return ExposureNotificationClientWrapper.provideDiagnosisKeys(filesFrom(keyFiles))
    }

    private fun filesFrom(keyFiles: List<KeyFile>): List<File> {
        val files: MutableList<File> = ArrayList()
        for (f in keyFiles) {
            f.file()?.let { files.add(it) }
        }
        return files
    }

    @Throws(IOException::class)
    private fun readFile(file: File): FileContent {
        ZipFile(file).use { zip ->

            val signatureEntry = zip.getEntry(KeyFileConstants.SIG_FILENAME)
            val exportEntry = zip.getEntry(KeyFileConstants.EXPORT_FILENAME)

            val sigData: ByteArray = IOUtils.toByteArray(zip.getInputStream(signatureEntry))
            val bodyData: ByteArray = IOUtils.toByteArray(zip.getInputStream(exportEntry))

            val header = bodyData.copyOf(16)
            val exportData = bodyData.copyOfRange(16, bodyData.size)

            val headerString = String(header)
            val signature = TEKSignatureList.parseFrom(sigData)
            val export = TemporaryExposureKeyExport.parseFrom(exportData)

            return FileContent(headerString, export, signature)
        }
    }

    private fun logKeys(files: List<KeyFile>, keyHexToLog: String) {
        var filenum = 1
        for (f in files) {
            try {
                val fc = f.file()?.let { readFile(file = it) }
                Log.d(TAG, "File " + filenum + " has signature:\n" + fc!!.signature)
                Log.d(TAG, "File " + filenum + " has [" + fc.export.keysCount + "] keys.")
                for (k: TemporaryExposureKey in fc.export.keysList) {
                    // We don't log all keys. Sometimes that's too much to log. Log only keys matching a hex
                    // substring we're interested in for debug purposes.
                    val keyHex: String = BASE16.encode(k.keyData.toByteArray())
//                    if (!keyHex.contains(keyHexToLog.toLowerCase())) {
//                        continue
//                    }
                    Log.d(TAG, "TEK hex:["
                            + keyHex
                            + "] base64:["
                            + BASE64.encode(k.keyData.toByteArray())
                            + "] interval_num:["
                            + k.rollingStartIntervalNumber
                            + "] rolling_period:["
                            + k.rollingPeriod
                            + "] risk:["
                            + k.transmissionRiskLevel
                            + "]"
                    )
                }
                filenum++
            } catch (e: IOException) {
                Log.d(TAG, "Failed to read or parse file $f $e")
            }
        }
    }

    private data class FileContent(
        val header: String,
        val export: TemporaryExposureKeyExport,
        val signature: TEKSignatureList
    )

    object KeyFileConstants {
        @VisibleForTesting
        val SIG_FILENAME = "export.sig"

        @VisibleForTesting
        val EXPORT_FILENAME = "export.bin"
    }
}