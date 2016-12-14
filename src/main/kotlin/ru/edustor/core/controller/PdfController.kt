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
import org.springframework.web.bind.annotation.ResponseBody
import ru.edustor.commons.storage.service.BinaryObjectStorageService
import ru.edustor.commons.storage.service.BinaryObjectStorageService.ObjectType.PAGE
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Lesson
import java.io.ByteArrayOutputStream

@Controller
class PdfController @Autowired constructor(val storage: BinaryObjectStorageService) {

    val logger: Logger = LoggerFactory.getLogger(PdfController::class.java)

    @RequestMapping("/pdf/{lesson}", produces = arrayOf("application/pdf"))
    @ResponseBody
    fun getPdf(@PathVariable lesson: Lesson?): ByteArray {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)

        if (!lesson.pages.any { it.contentType == "application/pdf" }) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "No pages found")
        val document = com.itextpdf.text.Document()

        val outputStream = ByteArrayOutputStream()
        val copy = PdfCopy(document, outputStream)
        document.open()
        document.addTitle("$lesson")

        lesson.pages
                .filter { it.isUploaded == true }
                .filter { it.removed == false }
                .filter { it.contentType == "application/pdf" }
                .map {
                    it.fileId?.let { storage.get(PAGE, it) } ?: throw NotFoundException("Cannot find page file: ${it.fileId}")
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