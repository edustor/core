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
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.services.PdfReaderService
import ru.wutiarn.edustor.utils.extensions.assertHasAccess
import ru.wutiarn.edustor.utils.extensions.assertIsOwner
import ru.wutiarn.edustor.utils.extensions.getActiveLesson
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Created by wutiarn on 26.02.16.
 */
@RestController
@RequestMapping("/api/documents")
class DocumentsController @Autowired constructor(
        val repo: DocumentsRepository,
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository,
        val PdfReaderService: PdfReaderService,
        val gfs: GridFsOperations
) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile): String? {
        when (file.contentType) {
            "application/pdf" -> {
                PdfReaderService.processPdfUpload(file.inputStream)
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

        val lesson: Lesson
        if (lessonId == "current") {
            val userNow = OffsetDateTime.ofInstant(instant ?: Instant.now(), ZoneOffset.ofHours(offset))
            lesson = user.timetable.getActiveLesson(lessonsRepo, userNow.toLocalDateTime()) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "No entry found in timetable")
        } else {
            lesson = lessonsRepo.findOne(lessonId) ?: throw throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson is not found")
            user.assertHasAccess(lesson)
        }

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