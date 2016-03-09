package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
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
import rx.Observable
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
open class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository, val documentsRepository: DocumentsRepository) {

    @RequestMapping("/{lesson}")
    fun getLesson(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: User): Lesson {
        user.assertHasAccess(lesson)

        return lesson
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

    @RequestMapping("/{lesson}/documents/reorder")
    fun reorderDocuments(@AuthenticationPrincipal user: User, @PathVariable("lesson") lessonId: String, @RequestParam document: Document, @RequestParam(required = false) after: Document?) {
        Observable.just(lessonId)
                .map {
                    val lesson = lessonsRepo.findOne(lessonId) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lesson is not found")
                    user.assertHasAccess(lesson)

                    val documentsCheckList = mutableListOf(document)
                    after?.let { documentsCheckList.add(after) }

                    if (!lesson.documents.containsAll(documentsCheckList)) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson must contain both documents")
                    lesson.documents.remove(document)

                    val targetIndex = if (after != null) lesson.documents.indexOf(after) + 1 else 0
                    lesson.documents.add(targetIndex, document)
                    lessonsRepo.save(lesson)
                }
                // Optimistic locking
                .retry { i, throwable ->
                    if (throwable !is OptimisticLockingFailureException) return@retry false
                    return@retry i <= 3
                }
                .toBlocking().subscribe()
    }
}
