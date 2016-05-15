package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsCriteria
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.services.PdfUploadService
import ru.wutiarn.edustor.utils.UploadPreferences
import ru.wutiarn.edustor.utils.extensions.assertHasAccess
import ru.wutiarn.edustor.utils.extensions.assertIsOwner
import java.time.Instant
import java.time.LocalDate

@RestController
@RequestMapping("/api/documents")
class DocumentsController @Autowired constructor(
        val repo: DocumentsRepository,
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository,
        val PdfUploadService: PdfUploadService,
        val gfs: GridFsOperations
) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile, @AuthenticationPrincipal user: User,
               @RequestParam(required = false) subject: Subject?,
               @RequestParam("date", required = false) date_str: String?,
               @RequestParam(required = false) topic: String?
    ): String? {
        var date: LocalDate? = null
        date_str?.let {
            date = LocalDate.parse(date_str)
        }

        val uploadPreferences = UploadPreferences(uploader = user)

        if (subject != null && date != null) {
            uploadPreferences.lesson = lessonsRepo.findLesson(subject, date!!) ?: Lesson(subject, date, topic)
        }

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
    fun uuid_info(@PathVariable uuid: String, @AuthenticationPrincipal user: User): Document? {
        val document = repo.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        user.assertHasAccess(document, lessonsRepo)
        return document
    }

    @RequestMapping("/uuid/activate")
    fun activate_uuid(@RequestParam uuid: String,
                      @RequestParam("lesson") lessonId: String,
                      @RequestParam offset: Int,
                      @RequestParam(required = false) instant: Instant?,
                      @AuthenticationPrincipal user: User
    ): Document {


        repo.findByUuid(uuid)?.let {
            throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This UUID is already activated")
        }

        val lesson = lessonsRepo.findOne(lessonId) ?: throw throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson is not found")
        user.assertHasAccess(lesson)

        val document = Document(uuid = uuid, owner = user)
        lesson.documents.add(document)
        repo.save(document)

        //        TODO: Optimistic lock handling
        lessonsRepo.save(lesson)
        return document
    }

    @RequestMapping("/{document}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: User, @PathVariable document: Document) {
        document.assertIsOwner(user)
        val lesson = lessonsRepo.findByDocumentsContaining(document)
        lesson?.documents?.remove(document)
        lessonsRepo.save(lesson)
        document.isUploaded.let {
            gfs.delete(Query.query(GridFsCriteria.whereFilename().`is`(document.id)))
        }
        documentsRepository.delete(document)
    }
}