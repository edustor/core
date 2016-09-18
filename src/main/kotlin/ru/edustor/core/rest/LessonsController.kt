package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.service.FCMService
import ru.edustor.core.util.extensions.assertHasAccess
import rx.Observable
import java.time.LocalDate

@RestController
@RequestMapping("/api/lessons")
open class LessonsController @Autowired constructor(
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository,
        val fcmService: FCMService
) {

    @RequestMapping("/subject/{subject}")
    fun subjectLessons(@PathVariable subject: Subject?, @RequestParam(required = false, defaultValue = "0") page: Int): List<Lesson> {
        subject ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return lessonsRepo.findBySubject(subject, PageRequest(page, 30)).filter { it.documents.isNotEmpty() && !it.removed }.sortedDescending()
    }

    @RequestMapping("/{lesson}")
    fun getLesson(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: User): Lesson {
        user.assertHasAccess(lesson)
        lesson.documents = lesson.documents.filter { !it.removed }.toMutableList()
        return lesson
    }

    @RequestMapping("/{lesson}/removed")
    fun getLessonRemovedDocs(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: User): List<Document> {
        user.assertHasAccess(lesson)
        return lesson.documents.filter { it.removed }
    }


    @RequestMapping("/{lesson}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: User, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = true
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/{lesson}/restore")
    fun restore(@AuthenticationPrincipal user: User, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = false
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/date/{date}/{subject}")
    fun getLessonByDate(@PathVariable subject: Subject,
                        @PathVariable date: LocalDate,
                        @AuthenticationPrincipal user: User
    ): Lesson {
        var lesson = lessonsRepo.findLessonBySubjectAndDate(subject, date)

        if (lesson != null) {
            user.assertHasAccess(lesson)
            if (lesson.removed) {
                lesson.removed = false
                lessonsRepo.save(lesson)
            }
        }

        if (lesson == null) {
            user.assertHasAccess(subject)
            lesson = Lesson(subject, date)
            lessonsRepo.save(lesson)
        }

        return lesson
    }

    @RequestMapping("/{lesson}/topic", method = arrayOf(RequestMethod.POST))
    fun setTopic(@PathVariable lesson: Lesson, @RequestParam(required = false) topic: String?, @AuthenticationPrincipal user: User) {
        user.assertHasAccess(lesson)
        lesson.topic = topic
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/date/{date}/{subject}/topic", method = arrayOf(RequestMethod.PUT))
    fun setTopicByDate(@PathVariable subject: Subject, @PathVariable date: LocalDate, @RequestParam(required = false) topic: String?, @AuthenticationPrincipal user: User) {
        val lesson = getLessonByDate(subject, date, user)
        user.assertHasAccess(lesson)
        lesson.topic = topic
        lessonsRepo.save(lesson)
        fcmService.sendUserSyncNotification(user)
    }

    @RequestMapping("/uuid/{uuid}")
    fun byDocumentUUID(@AuthenticationPrincipal user: User, @PathVariable uuid: String): Lesson {
        val document = documentsRepository.findByUuid(uuid) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Document is not found")
        val lesson = lessonsRepo.findByDocumentsContaining(document) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lesson is not found")

        user.assertHasAccess(lesson)

        return lesson
    }

    @RequestMapping("/{lesson}/documents/reorder")
    fun reorderDocuments(@AuthenticationPrincipal user: User, @PathVariable lesson: Lesson, @RequestParam document: Document, @RequestParam(required = false) after: Document?) {
        Observable.just(lesson)
                .map { lesson ->
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

    @RequestMapping("date/{date}/{subject}/documents/reorder")
    fun reorderDocumentsByDate(@AuthenticationPrincipal user: User, @PathVariable subject: Subject, @PathVariable date: LocalDate,
                               @RequestParam document: Document, @RequestParam(required = false) after: Document?) {
        val lesson = getLessonByDate(subject, date, user)
        reorderDocuments(user, lesson, document, after)

    }
}
