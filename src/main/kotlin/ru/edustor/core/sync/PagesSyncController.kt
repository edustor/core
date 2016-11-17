package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.repository.PageRepository
import ru.edustor.core.rest.PagesController
import java.time.Instant

@Component
open class PagesSyncController @Autowired constructor(
        val lessonRepository: LessonRepository,
        val pagesController: PagesController,
        val pageRepository: PageRepository
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "qr/activate" -> activateQR(task)
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("PagesSyncController cannot resolve ${task.method}")
        }
    }

    fun activateQR(task: SyncTask) {
        val instant = Instant.ofEpochSecond(task.params["instant"]!!.toLong())
        val lesson = lessonRepository.findOne(task.params["lesson"]!!)
        pagesController.activateQr(task.params["qr"]!!, lesson,
                instant, task.user, task.params["id"]!!)
    }

    fun delete(task: SyncTask) {
        val page = pageRepository.findOne(task.params["page"]!!) ?:
                throw NotFoundException("Page is not found")
        pagesController.delete(task.user, page)
    }

    fun restore(task: SyncTask) {
        val page = pageRepository.findOne(task.params["page"]!!) ?:
                throw NotFoundException("Page is not found")
        pagesController.restore(task.user, page)
    }
}