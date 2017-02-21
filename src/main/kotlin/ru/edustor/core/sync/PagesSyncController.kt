package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.rest.PagesController
import java.time.Instant

@Component
open class PagesSyncController @Autowired constructor(
        val lessonRepository: LessonRepository,
        val pagesController: PagesController) {
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
        pagesController.linkPage(task.params["qr"]!!, lesson,
                instant, task.user, task.params["id"]!!)
    }

    fun delete(task: SyncTask) {
        val page = task.params["page"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'page' field is not provided")
        pagesController.delete(task.user, page)
    }

    fun restore(task: SyncTask) {
        val page = task.params["page"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'page' field is not provided")
        pagesController.restore(task.user, page)
    }
}