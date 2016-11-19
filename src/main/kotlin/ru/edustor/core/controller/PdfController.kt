package ru.edustor.core.controller

import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Lesson
import ru.edustor.core.pdf.gen.BlankPdfGenerator
import ru.edustor.core.pdf.gen.BlankPdfGenerator.PdfTemplates.BLANK
import ru.edustor.core.pdf.gen.BlankPdfGenerator.PdfTemplates.GRID
import ru.edustor.core.pdf.gen.BlankPdfGenerator.QRLocations.*
import ru.edustor.core.pdf.storage.PdfStorage
import java.io.ByteArrayOutputStream

@Controller
class PdfController @Autowired constructor(val pdfStorage: PdfStorage, val pdfGenerator: BlankPdfGenerator) {

    val logger: Logger = LoggerFactory.getLogger(PdfController::class.java)

    @RequestMapping("/pdf", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf(@RequestParam(required = false) c: Int?): ByteArray {
        val count = c?.toInt() ?: 10
        if (!(count >= 1 && count <= 100)) {
            throw RuntimeException("Too many pages requested")
        }
        val pdf = pdfGenerator.genPdf(count, GRID)
        return pdf
    }

    @RequestMapping("/pdf/overprint", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf_overprint(@RequestParam(required = false) c: Int?,
                      @RequestParam(required = false) qrp: String?
    ): ByteArray {
        val count = c?.toInt() ?: 10
        if (!(count >= 1 && count <= 100)) {
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Too many pages requested")
        }

        val qrPositions: List<BlankPdfGenerator.QRLocations> = (qrp ?: "0,1,2,3")!!.split(",").map {
            when (it) {
                "0" -> LEFT_BOTTOM
                "1" -> LEFT_TOP
                "2" -> RIGHT_TOP
                "3" -> RIGHT_BOTTOM
                else -> throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Can't found qr location for index $it")
            }
        }

        val pdf = pdfGenerator.genPdf(count, BLANK, qrPositions)
        return pdf
    }

    @RequestMapping("/pdf/{lesson}", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun getPdf(@PathVariable lesson: Lesson?): ByteArray {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)

        if (!lesson.pages.any { it.contentType == "application/pdf" }) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "No pages found")
        val document = com.itextpdf.text.Document()

        val outputStream = ByteArrayOutputStream()
        val copy = PdfCopy(document, outputStream)
        document.open()

        lesson.pages
                .filter { it.isUploaded == true }
                .filter { it.removed == false }
                .filter { it.contentType == "application/pdf" }
                .map {
                    pdfStorage.get(it.id) ?: throw NotFoundException("Cannot find page file: ${it.id}")
                }
                .filterNotNull()
                .forEach { pageStream ->
                    val pdfReader = PdfReader(pageStream)
                    copy.addDocument(pdfReader)
                }
        document.close()

        logger.info("Accessing lesson PDF: ${lesson.id}")

        return outputStream.toByteArray()
    }
}