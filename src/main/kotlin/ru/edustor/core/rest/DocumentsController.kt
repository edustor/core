package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import ru.edustor.core.model.internal.pdf.PdfUploadPreferences
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.service.PdfUploadService
import ru.edustor.core.util.extensions.assertHasAccess
import ru.edustor.core.util.extensions.assertIsOwner
import java.time.Instant
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/documents")
class DocumentsController @Autowired constructor(
        val repo: DocumentsRepository,
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository,
        val PdfUploadService: PdfUploadService,
        val lessonsController: LessonsController
) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile,
               @AuthenticationPrincipal user: User,
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

    @RequestMapping("/uuid/{uuid}")
    fun uuidInfo(@PathVariable uuid: String, @AuthenticationPrincipal user: User): Document? {
        val document = repo.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        user.assertHasAccess(document, lessonsRepo)
        return document
    }

    @RequestMapping("/uuid/activate")
    fun activateUuid(@RequestParam qr: String,
                     @RequestParam lesson: Lesson,
                     @RequestParam(required = false) instant: Instant?,
                     @AuthenticationPrincipal user: User,
                     @RequestParam uuid: String = UUID.randomUUID().toString()
    ) {
        user.assertHasAccess(lesson)

        val existingDoc = repo.findByUuid(uuid)
        if (existingDoc != null) {
            if (existingDoc.removed == true) {
                val oldLesson = lessonsRepo.findByDocumentsContaining(existingDoc)
                oldLesson?.documents?.remove(existingDoc)
                lessonsRepo.save(oldLesson)

                existingDoc.owner = user
                existingDoc.timestamp = Instant.now()
            } else {
                throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This UUID is already activated")
            }
        }

        val document = existingDoc ?: Document(qr = qr, owner = user, timestamp = instant ?: Instant.now(), uuid = uuid)
        lesson.documents.add(document)
        repo.save(document)

        lessonsRepo.save(lesson)
    }

    @RequestMapping("/uuid/activate/date")
    fun activateUUidByDate(@RequestParam uuid: String,
                           @RequestParam subject: Subject,
                           @RequestParam date: LocalDate,
                           @RequestParam(required = false) instant: Instant?,
                           @AuthenticationPrincipal user: User,
                           @RequestParam id: String = UUID.randomUUID().toString()) {
        val lesson = lessonsController.getLessonByDate(subject, date, user)
        activateUuid(uuid, lesson, instant, user, id)
    }

    @RequestMapping("/{document}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: User, @PathVariable document: Document) {
        document.assertIsOwner(user)
        document.removed = true
        documentsRepository.save(document)
    }

    @RequestMapping("/{document}/restore")
    fun restore(@AuthenticationPrincipal user: User, @PathVariable document: Document) {
        document.assertIsOwner(user)
        document.removed = false
        documentsRepository.save(document)
    }
}