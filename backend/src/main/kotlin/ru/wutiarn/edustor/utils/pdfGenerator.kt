package ru.wutiarn.edustor.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfSmartCopy
import com.itextpdf.text.pdf.PdfStamper
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID


/**
 * Created by wutiarn on 25.02.16.
 */
fun getQR(text: String = randomUUID().toString()): BufferedImage {
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.DATA_MATRIX, 300, 300, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.MARGIN to 0
    ));
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    return bufferedImage
}

fun getPdf(count: Int = 1): ByteArray {
    val origPdfReader = PdfReader("page.pdf")
    val out1 = ByteArrayOutputStream()


    val document = Document()
    val copy = PdfSmartCopy(document, out1)

    document.open()
    document.addTitle("Edustor blank pages")

    val page = copy.getImportedPage(origPdfReader, 1)
    for (i in 1..count) {
        copy.addPage(page)
    }
    document.close()

    val pdfReader = PdfReader(out1.toByteArray())

    val out2 = ByteArrayOutputStream()
    val pdfStamper = PdfStamper(pdfReader, out2)

    for (i in 1..pdfReader.numberOfPages) {
        val image = Image.getInstance(getImageAsByteArray(getQR()))

        val content = pdfStamper.getOverContent(i)
        image.scaleAbsolute(Rectangle(60f, 60f))
        image.setAbsolutePosition(500f, 55f)
        content.addImage(image)
    }

    pdfStamper.close()

    return out2.toByteArray()
}