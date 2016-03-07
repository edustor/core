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
import ru.wutiarn.edustor.utils.getPdf
import java.io.ByteArrayOutputStream

/**
 * Created by wutiarn on 22.02.16.
 */
@Controller
class PdfController @Autowired constructor(val gfs: GridFsOperations) {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN") // Kotlin's Int can't be == null
    @RequestMapping("/pdf", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun pdf(@RequestParam(required = false) c: Integer?): ByteArray {
        val count = c?.toInt() ?: 1
        if (!(count >= 1 && count <= 100)) {
            throw RuntimeException("Too much pages")
        }
        val pdf = getPdf(count)
        return pdf
    }

    @RequestMapping("/pdf/{lesson}", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun getPdf(@PathVariable lesson: Lesson?): ByteArray {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)

        if (!lesson.documents.any { it.contentType == "application/pdf" }) throw HttpRequestProcessingException(HttpStatus.NO_CONTENT, "No pages found")
        val document = com.itextpdf.text.Document()

        val outputStream = ByteArrayOutputStream()
        val copy = PdfCopy(document, outputStream)
        document.open()

        lesson.documents
                .filter { it.isUploaded == true }
                .filter { it.contentType == "application/pdf" }
                .sorted()
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