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
import ru.edustor.core.util.extensions.assertIsOwner
import ru.edustor.core.util.extensions.recalculateIndexes
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/api/pages")
class PagesController @Autowired constructor(
        val lessonRepo: LessonRepository,
        val pageRepository: PageRepository
) {
    @RequestMapping("/qr/{qr}")
    fun pageByQr(@PathVariable qr: String, user: Account): Page? {
        val page = pageRepository.findByQr(qr) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        user.assertHasAccess(page)
        return page
    }

    @RequestMapping("/qr/activate")
    fun activateQr(@RequestParam qr: String,
                   @RequestParam lesson: Lesson,
                   @RequestParam(required = false) instant: Instant?,
                   user: Account,
                   @RequestParam id: String = UUID.randomUUID().toString()
    ) {
        user.assertHasAccess(lesson)

        val existingDoc = pageRepository.findByQr(qr)
        if (existingDoc != null) {
            if (existingDoc.removed == true) {
                pageRepository.delete(existingDoc)
            } else {
                throw HttpRequestProcessingException(HttpStatus.CONFLICT, "This QR is already activated")
            }
        }

        val page = Page(qr = qr, owner = user, timestamp = instant ?: Instant.now(), id = id)
        lesson.pages.add(page)
        lesson.pages.recalculateIndexes(lesson)
        pageRepository.save(page)

        lessonRepo.save(lesson)
    }

    @RequestMapping("/{page}", method = arrayOf(RequestMethod.DELETE))
    fun delete(user: Account, @PathVariable page: Page) {
        page.assertIsOwner(user)
        page.removed = true
        pageRepository.save(page)
    }

    @RequestMapping("/{page}/restore")
    fun restore(user: Account, @PathVariable page: Page) {
        page.assertIsOwner(user)
        page.removed = false
        pageRepository.save(page)
    }
}