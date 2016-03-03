package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.services.PdfReaderService
import ru.wutiarn.edustor.utils.extensions.getActiveTimetableEntry
import ru.wutiarn.edustor.utils.extensions.getLesson
import rx.lang.kotlin.toObservable
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

    @RequestMapping("uuid_info")
    fun uuid_info(@RequestParam uuid: String): Document? {
        val document = repo.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return document
    }

    @RequestMapping("activate_uuid")
    fun activate_uuid(@RequestParam uuid: String, @RequestParam offset: Int, @AuthenticationPrincipal user: User): Document {
        repo.findByUuid(uuid)?.let {
            throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This UUID is already activated")
        }

        val userNow = OffsetDateTime.now(ZoneOffset.ofHours(offset))
        val lesson = user.timetable.toObservable()
                .getActiveTimetableEntry(userNow.toLocalTime())
                .getLesson(lessonsRepo, userNow.toLocalDate())
                .map { lessonsRepo.save(it); it }
                .toBlocking().first()
        val document = Document(uuid = uuid, lesson = lesson, owner = user)
        lessonsRepo.save(lesson)
        repo.save(document)
        return document
    }
}