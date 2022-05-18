package tw.gov.cdc.exposurenotifications.common

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import java.util.*

object QRCodeEncoder {

    fun getBitmap(contents: String, dimension: Int, black: Int, white: Int): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                guessAppropriateEncoding(contents)?.let { put(EncodeHintType.CHARACTER_SET, it) }
                put(EncodeHintType.MARGIN, 0)
            }
            val result = MultiFormatWriter().encode(contents, BarcodeFormat.QR_CODE, dimension, dimension, hints)
            val width = result.width
            val height = result.height
            val pixels = IntArray(width * height).also {
                for (y in 0 until height) {
                    val offset = y * width
                    for (x in 0 until width) {
                        it[offset + x] = if (result[x, y]) black else white
                    }
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun guessAppropriateEncoding(contents: CharSequence): String? {
        // Very crude at the moment
        for (element in contents) {
            if (element.code > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }
}