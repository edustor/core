package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.service.PdfUploadService
import ru.edustor.core.util.extensions.assertHasAccess
import ru.edustor.core.util.extensions.assertIsOwner
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/documents")
class DocumentsController @Autowired constructor(
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository,
        val PdfUploadService: PdfUploadService
) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile,
               @AuthenticationPrincipal user: Account,
               @RequestParam(required = false) lesson: Lesson?
    ): String? {
        val uploadPreferences = PdfUploadPreferences(uploader = user, lesson = lesson)
        when (file.contentType) {
            "application/pdf" -> {
                PdfUploadService.processPdfUpload(file.inputStream, uploadPreferences)
            }
            else -> {
                throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "Unsupported content type: ${file.contentType}")
            }
        }
        return "Successfully uploaded"
    }

    @RequestMapping("/qr/{qr}")
    fun documentByQr(@PathVariable qr: String, @AuthenticationPrincipal user: Account): Document? {
        val document = documentsRepository.findByQr(qr) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        user.assertHasAccess(document, lessonsRepo)
        return document
    }

    @RequestMapping("/qr/activate")
    fun activateQr(@RequestParam qr: String,
                   @RequestParam lesson: Lesson,
                   @RequestParam(required = false) instant: Instant?,
                   @AuthenticationPrincipal user: Account,
                   @RequestParam id: String = UUID.randomUUID().toString()
    ) {
        user.assertHasAccess(lesson)

        val existingDoc = documentsRepository.findByQr(qr)
        if (existingDoc != null) {
            if (existingDoc.removed == true) {
                documentsRepository.delete(existingDoc)
            } else {
                throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This QR is already activated")
            }
        }

        val document = Document(qr = qr, owner = user, timestamp = instant ?: Instant.now(), id = id)
        lesson.documents.add(document)
        lesson.recalculateDocumentsIndexes()
        documentsRepository.save(document)

        lessonsRepo.save(lesson)
    }

    @RequestMapping("/{document}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: Account, @PathVariable document: Document) {
        document.assertIsOwner(user)
        document.removed = true
        documentsRepository.save(document)
    }

    @RequestMapping("/{document}/restore")
    fun restore(@AuthenticationPrincipal user: Account, @PathVariable document: Document) {
        document.assertIsOwner(user)
        document.removed = false
        documentsRepository.save(document)
    }
}