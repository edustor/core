package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.rest.PagesController
import java.time.Instant

@Component
open class PagesSyncController @Autowired constructor(
        val lessonRepository: LessonRepository,
        val pageRepository: PageRepository,
        val pagesController: PagesController) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "link" -> linkPage(task)
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("PagesSyncController cannot resolve ${task.method}")
        }
    }

    fun linkPage(task: SyncTask) {
        val instant = Instant.ofEpochSecond(task.params["instant"]!!.toLong())
        val lesson = lessonRepository.findOne(task.params["lesson"]!!)
        pagesController.linkPage(task.params["qr"]!!, lesson,
                instant, task.account, task.params["id"]!!)
    }

    fun delete(task: SyncTask) {
        val pageId = task.params["page"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'page' field is not provided")
        val page = pageRepository.findOne(pageId) ?: throw NotFoundException("Page is not found")
        pagesController.delete(task.account, page)
    }

    fun restore(task: SyncTask) {
        val pageId = task.params["page"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'page' field is not provided")
        val page = pageRepository.findOne(pageId) ?: throw NotFoundException("Page is not found")
        pagesController.restore(task.account, page)
    }
}