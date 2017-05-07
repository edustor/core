package ru.edustor.core.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Page
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.util.extensions.assertHasAccess
import ru.edustor.core.util.extensions.setIndexes
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/pages")
class PagesController @Autowired constructor(
        val lessonRepo: LessonRepository,
        val pageRepository: PageRepository
) {
    @RequestMapping("/link")
    fun linkPage(@RequestParam qr: String,
                 @RequestParam lesson: Lesson,
                 @RequestParam(required = false) instant: Instant?,
                 user: Account,
                 @RequestParam id: String = UUID.randomUUID().toString()
    ) {
        user.assertHasAccess(lesson)

        val oldPage = pageRepository.findByQr(qr)
        if (oldPage != null && !oldPage.removed) {
            throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This QR is already linked to ${oldPage.lesson.id}")
        }

        val page = oldPage ?: Page(lesson = lesson, qr = qr, timestamp = instant ?: Instant.now(), id = id)
        page.removedOn = null
        lesson.pages.remove(page)

        lesson.pages.add(page)
        lesson.pages.setIndexes()
        lessonRepo.save(lesson)
    }

    @RequestMapping("/{page}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable page: Page) {
        user.assertHasAccess(page)
        page.removed = true
        pageRepository.save(page)
    }

    @RequestMapping("/{page}/restore")
    fun restore(user: Account, @PathVariable page: Page) {
        user.assertHasAccess(page)
        page.removed = false
        pageRepository.save(page)
    }
}