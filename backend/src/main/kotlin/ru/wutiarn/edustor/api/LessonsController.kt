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
import ru.wutiarn.edustor.utils.extensions.assertHasAccess
import ru.wutiarn.edustor.utils.extensions.getActiveLesson
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
open class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository, val documentsRepository: DocumentsRepository) {

    @RequestMapping("/{lesson}/documents")
    fun getDocuments(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: User): List<Document> {
        user.assertHasAccess(lesson)

        return lesson.documents
    }

    @RequestMapping("/current")
    open fun current(@AuthenticationPrincipal user: User, @RequestParam offset: Int): Lesson {
        val userNow = OffsetDateTime.now(ZoneOffset.ofHours(offset)).toLocalDateTime()
        return user.timetable.getActiveLesson(lessonsRepo, userNow) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/uuid/{uuid}")
    fun byDocumentUUID(@AuthenticationPrincipal user: User, @PathVariable uuid: String): Lesson {
        val document = documentsRepository.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Document is not found")
        val lesson = lessonsRepo.findByDocumentsContaining(document) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lesson is not found")

        user.assertHasAccess(lesson)

        return lesson
    }

    @RequestMapping("/{lesson}/reorder")
    fun reorderDocuments(@AuthenticationPrincipal user: User, @PathVariable lesson: Lesson, @RequestParam document: Document, @RequestParam after: Document) {
        user.assertHasAccess(lesson)
        if (!lesson.documents.containsAll(listOf(document, after))) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson must contain both documents")

        lesson.documents.remove(document)
        val targetIndex = lesson.documents.indexOf(after) + 1
        lesson.documents.add(targetIndex, document)

        //        TODO: Optimistic lock exception handling
        lessonsRepo.save(lesson)
    }
}
