package ru.wutiarn.edustor.utils

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.VerticalPositionMark
import ru.wutiarn.edustor.EdustorApplication
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID.randomUUID


fun getQR(text: String = randomUUID().toString()): BufferedImage {
    val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300, mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.MARGIN to 0
    ));
    val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
    return bufferedImage
}

fun getPdf(count: Int = 1, template: String = "pdf_templates/page.pdf", reserveCodes: Boolean = false): ByteArray {

    val proximaThinFont = BaseFont.createFont("fonts/Proxima Nova Thin.otf", BaseFont.WINANSI, true)

    val origPdfReader = PdfReader(template)
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

    val now = LocalDateTime.now(ZoneId.of("Europe/Moscow")).withNano(0)
    val nowStr = "Generated by Edustor Core v${EdustorApplication.VERSION} on ${now.format(DateTimeFormatter.ISO_LOCAL_DATE)} ${now.format(DateTimeFormatter.ISO_LOCAL_TIME)} MSK. " +
            "© Edustor Project. Dmitry Romanov, 2016"

    for (i in 1..pdfReader.numberOfPages) {

        val uuid = randomUUID().toString()

        val uuidEnd = uuid.split("-").last()
        val idString = "#${uuidEnd.substring(0, 4)}-${uuidEnd.substring(4, 8)}-${uuidEnd.substring(8, 12)}"

        val phrase = Phrase()
        phrase.font = Font(proximaThinFont, 8f, Font.NORMAL, BaseColor.BLACK)
        phrase.add(nowStr)
        phrase.add(Chunk(VerticalPositionMark()))
        phrase.add(idString)

        val cell = PdfPCell(phrase)
        cell.border = PdfPCell.NO_BORDER

        val table = PdfPTable(1)
        table.addCell(cell)
        table.totalWidth = page.width - 12 * 2


        val content = pdfStamper.getOverContent(i)
        table.writeSelectedRows(0, -1, 0, -1, 12f, table.totalHeight + 12, content)

        val qrCoords = mutableListOf(
                540f to 23.5f
        )

        if (reserveCodes) {
            qrCoords.addAll(listOf(
                    540f to 775.5f,
                    14.5f to 775.5f,
                    14.5f to 23.5f
            ))
        }

        val uri = "edustor://d/$uuid"
        val qr = getQR(uri).getAsByteArray()

        qrCoords.forEach {
            val image = Image.getInstance(qr)
            image.scaleAbsolute(Rectangle(41f, 41f))
            image.setAbsolutePosition(it.first, it.second)
            content.addImage(image)
        }
    }

    pdfStamper.close()

    return out2.toByteArray()
}