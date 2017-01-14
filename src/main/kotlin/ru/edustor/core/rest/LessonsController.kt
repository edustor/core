package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.model.Tag
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.util.extensions.assertHasAccess
import ru.edustor.core.util.extensions.recalculateIndexes
import rx.Observable
import java.time.LocalDate

@RestController
@RequestMapping("/api/lessons")
open class LessonsController @Autowired constructor(
        val lessonRepo: LessonRepository,
        val pageRepository: PageRepository
) {

    @RequestMapping("/{lessonId}", method = arrayOf(RequestMethod.POST))
    fun create(lessonId: String, tag: Tag, date: LocalDate, user: Account) {
        user.assertHasAccess(tag)
        val lesson = Lesson(tag, date, user)
        lesson.id = lessonId
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{lesson}")
    fun getLesson(@PathVariable lesson: Lesson, user: Account): Lesson.LessonDTO {
        user.assertHasAccess(lesson)
        lesson.pages = lesson.pages.filter { !it.removed }.toMutableList()
        return lesson.toDTO()
    }

    @RequestMapping("/{lesson}/removed")
    fun getLessonRemovedDocs(@PathVariable lesson: Lesson, user: Account): List<Page> {
        user.assertHasAccess(lesson)
        return lesson.pages.filter(Page::removed)
    }


    @RequestMapping("/{lesson}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = true
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{lesson}/pages")
    fun lessonPages(user: Account, @PathVariable lesson: Lesson): List<Page> {
        user.assertHasAccess(lesson)
        return lesson.pages
    }

    @RequestMapping("/{lesson}/restore")
    fun restore(user: Account, @PathVariable lesson: Lesson) {
        user.assertHasAccess(lesson)
        lesson.removed = false
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{lesson}/topic", method = arrayOf(RequestMethod.POST))
    fun setTopic(@PathVariable lesson: Lesson, @RequestParam(required = false) topic: String?, user: Account) {
        user.assertHasAccess(lesson)
        lesson.topic = topic
        lessonRepo.save(lesson)
    }

    @RequestMapping("/qr/{qr}")
    fun byPageQR(user: Account, @PathVariable qr: String): Lesson.LessonDTO {
        val page = pageRepository.findByQr(qr) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Page is not found")
        user.assertHasAccess(page.lesson)

        return page.lesson.toDTO()
    }

    @RequestMapping("/{lesson}/pages/reorder")
    fun reorderPages(user: Account, @PathVariable lesson: Lesson, @RequestParam page: Page, @RequestParam(required = false) after: Page?) {
        Observable.just(lesson)
                .map { lesson ->
                    user.assertHasAccess(lesson)

                    val pagesCheckList = mutableListOf(page)
                    after?.let { pagesCheckList.add(after) }

                    val pagesList = lesson.pages

                    if (!lesson.pages.containsAll(pagesCheckList)) throw HttpRequestProcessingException(HttpStatus.NOT_FOUND, "Specified lesson must contain both pages")
                    pagesList.remove(page)

                    val targetIndex = if (after != null) lesson.pages.indexOf(after) + 1 else 0
                    pagesList.add(targetIndex, page)

                    pagesList.recalculateIndexes(lesson)
                    pageRepository.save(pagesList)
                }
                // Optimistic locking
                .retry { i, throwable ->
                    if (throwable !is OptimisticLockingFailureException) return@retry false
                    return@retry i <= 3
                }
                .toBlocking().subscribe()
    }
}
