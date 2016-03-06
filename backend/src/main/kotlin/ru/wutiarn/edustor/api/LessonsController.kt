package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.User
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.utils.extensions.getActiveLesson
import ru.wutiarn.edustor.utils.filterHasAccess
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository, val documentsRepository: DocumentsRepository) {
    @RequestMapping("/{lesson}/documents")
    fun getDocuments(@PathVariable lesson: Lesson?): List<Document> {
        lesson ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lesson.documents
    }

    @RequestMapping("/current")
    fun current(@AuthenticationPrincipal user: User, @RequestParam offset: Int): Lesson {
        val userNow = OffsetDateTime.now(ZoneOffset.ofHours(offset)).toLocalDateTime()
        return user.timetable.getActiveLesson(lessonsRepo, userNow) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/uuid/{uuid}")
    fun byDocumentUUID(@AuthenticationPrincipal user: User, @PathVariable uuid: String): List<Lesson> {
        val document = documentsRepository.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Document is not found")
        val lessons = lessonsRepo.findByDocumentsContaining(document)
        if (lessons.isEmpty()) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lessons are not found")

        val accessibleLessons = lessons.filterHasAccess(user)
        if (accessibleLessons.isEmpty()) throw HttpRequestProcessingException(HttpStatus.FORBIDDEN, "You have not access to any lesson linked with this document")

        return accessibleLessons
    }
}
