package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.services.PdfReaderService
import ru.wutiarn.edustor.utils.extensions.getActiveLesson
import ru.wutiarn.edustor.utils.extensions.hasAccess
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
        val PdfReaderService: PdfReaderService
) {
    @RequestMapping("upload", method = arrayOf(RequestMethod.POST))
    fun upload(@RequestParam("file") file: MultipartFile): String? {
        file.contentType
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
        if (!user.hasAccess(document, lessonsRepo)) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN)
        return document
    }

    @RequestMapping("/uuid/activate")
    fun activate_uuid(@RequestParam uuid: String, @RequestParam offset: Int, @AuthenticationPrincipal user: User): Document {
        repo.findByUuid(uuid)?.let {
            throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This UUID is already activated")
        }

        val userNow = OffsetDateTime.now(ZoneOffset.ofHours(offset))
        val lesson = user.timetable.getActiveLesson(lessonsRepo, userNow.toLocalDateTime()) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "No entry found in timetable")
        val document = Document(uuid = uuid, owner = user)
        lesson.documents.add(document)
        repo.save(document)
        lessonsRepo.save(lesson)
        return document
    }
}