package ru.edustor.core.pdf.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.util.*

@Component
open class QRGenerator {
    open fun makeQR(content: String): BufferedImage {
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300, mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
                EncodeHintType.MARGIN to 0
        ));
        val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
        return bufferedImage
    }

    open fun makePageUriQR(uuid: String = UUID.randomUUID().toString()): BufferedImage {
        return makeQR("edustor://d/$uuid")
    }
}