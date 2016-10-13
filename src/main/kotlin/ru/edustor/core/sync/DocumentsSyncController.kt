package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.rest.DocumentsController
import java.time.Instant

@Component
open class DocumentsSyncController @Autowired constructor(
        val lessonsRepository: LessonsRepository,
        val documentsController: DocumentsController,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "qr/activate" -> activateQR(task)
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("DocumentsSyncController cannot resolve ${task.method}")
        }
    }

    fun activateQR(task: SyncTask) {
        val instant = Instant.ofEpochSecond(task.params["instant"]!!.toLong())
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!)
        documentsController.activateQr(task.params["qr"]!!, lesson,
                instant, task.user, task.params["id"]!!)
    }

    fun delete(task: SyncTask) {
        val document = documentsRepository.findOne(task.params["document"]!!) ?:
                throw NotFoundException("Document is not found")
        documentsController.delete(task.user, document)
    }

    fun restore(task: SyncTask) {
        val document = documentsRepository.findOne(task.params["document"]!!) ?:
                throw NotFoundException("Document is not found")
        documentsController.restore(task.user, document)
    }
}