package ru.wutiarn.edustor.controller

import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.utils.BlankPdfGenerator
import java.io.ByteArrayOutputStream

@Controller
class PdfController @Autowired constructor(val gfs: GridFsOperations) {
    @RequestMapping("/pdf", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf(@RequestParam(required = false) c: Int?): ByteArray {
        val count = c?.toInt() ?: 10
        if (!(count >= 1 && count <= 100)) {
            throw RuntimeException("Too much pages")
        }
        val pdf = BlankPdfGenerator.genPdf(count, BlankPdfGenerator.PdfTemplates.GRID)
        return pdf
    }

    @RequestMapping("/pdf/overprint", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf_overprint(@RequestParam(required = false) c: Int?,
                      @RequestParam(required = false) qrp: String?
    ): ByteArray {
        val count = c?.toInt() ?: 10
        if (!(count >= 1 && count <= 100)) {
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Too many pages")
        }

        val qrPositions: List<BlankPdfGenerator.QRLocations> = (qrp ?: "0,1,2,3")!!.split(",").map {
            when (it) {
                "0" -> BlankPdfGenerator.QRLocations.LEFT_BOTTOM
                "1" -> BlankPdfGenerator.QRLocations.LEFT_TOP
                "2" -> BlankPdfGenerator.QRLocations.RIGHT_TOP
                "3" -> BlankPdfGenerator.QRLocations.RIGHT_BOTTOM
                else -> throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Can't found qr location for index $it")
            }
        }

        val pdf = BlankPdfGenerator.genPdf(count, BlankPdfGenerator.PdfTemplates.BLANK, qrPositions)
        return pdf
    }

    @RequestMapping("/pdf/{lesson}", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun getPdf(@PathVariable lesson: Lesson?): ByteArray {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)

        if (!lesson.documents.any { it.contentType == "application/pdf" }) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "No pages found")
        val document = com.itextpdf.text.Document()

        val outputStream = ByteArrayOutputStream()
        val copy = PdfCopy(document, outputStream)
        document.open()

        lesson.documents
                .filter { it.isUploaded == true }
                .filter { it.contentType == "application/pdf" }
                .map {
                    gfs.findOne(Query.query(GridFsCriteria.whereFilename().`is`(it.id)))
                }
                .filterNotNull()
                .map {
                    val pdfReader = PdfReader(it.inputStream)
                    copy.addDocument(pdfReader)
                }
        document.close()
        return outputStream.toByteArray()
    }
}