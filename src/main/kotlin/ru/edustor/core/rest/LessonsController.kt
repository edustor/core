package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Document
import ru.edustor.core.model.Folder
import ru.edustor.core.model.Lesson
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.util.extensions.assertHasAccess
import rx.Observable
import java.time.LocalDate

@RestController
@RequestMapping("/api/lessons")
open class LessonsController @Autowired constructor(
        val lessonsRepo: LessonsRepository,
        val documentsRepository: DocumentsRepository
) {

    @RequestMapping("/{lessonId}", method = arrayOf(RequestMethod.POST))
    fun create(lessonId: String, folder: Folder, date: LocalDate) {
        val lesson = Lesson(folder, date)
        lesson.id = lessonId
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/{lesson}")
    fun getLesson(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: Account): Lesson {
        user.assertHasAccess(lesson)
        lesson.documents = lesson.documents.filter { !it.removed }.toMutableList()
        return lesson
    }

    @RequestMapping("/{lesson}/removed")
    fun getLessonRemovedDocs(@PathVariable lesson: Lesson, @AuthenticationPrincipal user: Account): List<Document> {
        user.assertHasAccess(lesson)
        return lesson.documents.filter { it.removed }
    }


    @RequestMapping("/{lesson}", method = arrayOf(RequestMethod.DELETE))
    fun delete(@AuthenticationPrincipal user: Account, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = true
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/{lesson}/documents")
    fun lessonDocuments(@AuthenticationPrincipal user: Account, @PathVariable lesson: Lesson): MutableList<Document> {
        user.assertHasAccess(lesson)
        return lesson.documents
    }

    @RequestMapping("/{lesson}/restore")
    fun restore(@AuthenticationPrincipal user: Account, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = false
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/{lesson}/topic", method = arrayOf(RequestMethod.POST))
    fun setTopic(@PathVariable lesson: Lesson, @RequestParam(required = false) topic: String?, @AuthenticationPrincipal user: Account) {
        user.assertHasAccess(lesson)
        lesson.topic = topic
        lessonsRepo.save(lesson)
    }

    @RequestMapping("/qr/{qr}")
    fun byDocumentQR(@AuthenticationPrincipal user: Account, @PathVariable qr: String): Lesson {
        val document = documentsRepository.findByQr(qr) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Document is not found")
        val lesson = lessonsRepo.findByDocumentsContaining(document) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Lesson is not found")

        user.assertHasAccess(lesson)

        return lesson
    }

    @RequestMapping("/{lesson}/documents/reorder")
    fun reorderDocuments(@AuthenticationPrincipal user: Account, @PathVariable lesson: Lesson, @RequestParam document: Document, @RequestParam(required = false) after: Document?) {
        Observable.just(lesson)
                .map { lesson ->
                    user.assertHasAccess(lesson)

                    val documentsCheckList = mutableListOf(document)
                    after?.let { documentsCheckList.add(after) }

                    if (!lesson.documents.containsAll(documentsCheckList)) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson must contain both documents")
                    lesson.documents.remove(document)

                    val targetIndex = if (after != null) lesson.documents.indexOf(after) + 1 else 0
                    lesson.documents.add(targetIndex, document)

                    lesson.recalculateDocumentsIndexes()
                    documentsRepository.save(lesson.documents)
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
