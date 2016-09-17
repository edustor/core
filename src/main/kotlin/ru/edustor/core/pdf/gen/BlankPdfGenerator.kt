package ru.edustor.core.pdf.gen

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.VerticalPositionMark
import org.springframework.stereotype.Component
import ru.edustor.core.EdustorApplication
import ru.edustor.core.pdf.qr.QRGenerator
import ru.edustor.core.util.extensions.getAsByteArray
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID.randomUUID

@Component
open class BlankPdfGenerator(val qrGen: QRGenerator) {

    enum class QRLocations(val putLocation: Pair<Float, Float>) {
        RIGHT_BOTTOM(540f to 23.5f),
        RIGHT_TOP(540f to 775.5f),
        LEFT_TOP(14.5f to 775.5f),
        LEFT_BOTTOM(14.5f to 23.5f)
    }

    enum class PdfTemplates(val path: String) {
        GRID("pdf_templates/page.pdf"),
        BLANK("pdf_templates/overprint.pdf"),
    }

    private val proximaThinFont = BaseFont.createFont("fonts/Proxima Nova Thin.otf", BaseFont.WINANSI, true)

    fun genPdf(count: Int,
               template: PdfTemplates,
               requestedCodeLocations: List<QRLocations> = listOf(QRLocations.LEFT_BOTTOM)): ByteArray {


        val origPdfReader = PdfReader(template.path)
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

            val content = pdfStamper.getOverContent(i)

            val topPhrase = Phrase()
            topPhrase.font = Font(proximaThinFont, 11f, Font.NORMAL, BaseColor.BLACK)
            topPhrase.add("Edustor Alpha")
            topPhrase.add(Chunk(VerticalPositionMark()))
            topPhrase.add("#${uuidEnd.substring(0, 4)}  #")
            val topTable = getPhraseTable(topPhrase, page.width - 12 * 2 - 50)

            val bottomPhrase = Phrase()
            bottomPhrase.font = Font(proximaThinFont, 8f, Font.NORMAL, BaseColor.BLACK)
            bottomPhrase.add(nowStr)
            bottomPhrase.add(Chunk(VerticalPositionMark()))
            bottomPhrase.add("#${uuidEnd.substring(0, 4)}-${uuidEnd.substring(4, 8)}-${uuidEnd.substring(8, 12)}")
            val bottomTable = getPhraseTable(bottomPhrase, page.width - 12 * 2)
            bottomTable.writeSelectedRows(0, -1, 0, -1, 12f, bottomTable.totalHeight + 12, content)

            topTable.writeSelectedRows(0, -1, 0, -1, 12f, page.height - topTable.totalHeight / 2 - 2, content)

            val qrCoords = requestedCodeLocations.map { it.putLocation }

            val qr = qrGen.makePageUriQR(uuid).getAsByteArray()

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

    private fun getPhraseTable(phrase: Phrase, tableWidth: Float): PdfPTable {
        val cell = PdfPCell(phrase)
        cell.border = PdfPCell.NO_BORDER

        val table = PdfPTable(1)
        table.addCell(cell)
        table.totalWidth = tableWidth

        return table
    }
}