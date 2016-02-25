package ru.wutiarn.edustor.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID
import javax.imageio.ImageIO

/**
 * Created by wutiarn on 25.02.16.
 */
fun getQR(text: String = randomUUID().toString()): BufferedImage {
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 100, 100, mapOf(EncodeHintType.MARGIN to 0));
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    return bufferedImage
}

fun getImageAsByteArray(image: BufferedImage): ByteArray {
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(image, "png", outputStream)
    return outputStream.toByteArray()
}

fun getPdf(): ByteArray {
    val pdfReader = PdfReader("page.pdf")
    val out = ByteArrayOutputStream()

    val pdfStamper = PdfStamper(pdfReader, out)
    val image = Image.getInstance(getImageAsByteArray(getQR()))

    val content = pdfStamper.getOverContent(1)
    image.scaleAbsolute(Rectangle(60f, 60f))
    image.setAbsolutePosition(500f, 55f)
    content.addImage(image)

    pdfStamper.close()

    return out.toByteArray()
}