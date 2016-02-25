package ru.wutiarn.edustor.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.awt.image.BufferedImage
import java.util.UUID.randomUUID

/**
 * Created by wutiarn on 25.02.16.
 */
fun getQR(text: String = randomUUID().toString()): BufferedImage? {
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 100, 100, mapOf(EncodeHintType.MARGIN to 0));
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    return bufferedImage
//    val out = ByteArrayOutputStream()
//    ImageIO.write(bufferedImage, "png", out)
//    return out.toByteArray()
}