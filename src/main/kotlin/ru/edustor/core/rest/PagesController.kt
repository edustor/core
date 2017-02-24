package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.util.extensions.assertHasAccess
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/pages")
class PagesController @Autowired constructor(
        val lessonRepo: LessonRepository
) {
    @RequestMapping("/link")
    fun linkPage(@RequestParam qr: String,
                 @RequestParam lesson: Lesson,
                 @RequestParam(required = false) instant: Instant?,
                 user: Account,
                 @RequestParam id: String = UUID.randomUUID().toString()
    ) {
        user.assertHasAccess(lesson)

        val oldPageLesson = lessonRepo.findByPagesQr(qr)
        if (oldPageLesson != null) {
            val page = oldPageLesson.pages.first { it.qr == qr }
            if (page.removed) {
                lesson.pages.remove(page)
                lessonRepo.save(oldPageLesson)
            } else {
                throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This QR is already linked to ${oldPageLesson.id}")
            }
        }

        val page = Page(qr = qr, timestamp = instant ?: Instant.now(), id = id)
        lesson.pages.add(page)
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{page}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable("page") pageId: String) {
        val lesson = lessonRepo.findByPagesId(pageId) ?: throw NotFoundException()
        val page = lesson.pages.first { it.id == pageId }
        user.assertHasAccess(lesson)
        page.removed = true
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{page}/restore")
    fun restore(user: Account, @PathVariable("page") pageId: String) {
        val lesson = lessonRepo.findByPagesId(pageId) ?: throw NotFoundException()
        val page = lesson.pages.first { it.id == pageId }
        user.assertHasAccess(lesson)
        page.removed = false
        lessonRepo.save(lesson)
    }
}