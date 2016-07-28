package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.DocumentsController
import ru.wutiarn.edustor.exceptions.NotFoundException
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.DocumentsRepository
import java.time.Instant

@Component
open class DocumentsSyncController @Autowired constructor(
        val documentsController: DocumentsController,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {
            "uuid/activate" -> activateUUID(task)
            "delete" -> delete(task)
        }
        return null
    }

    fun activateUUID(task: SyncTask) {
        val instant = Instant.parse(task.params["instant"]!!)
        documentsController.activateUuid(task.params["uuid"]!!, task.params["lesson"]!!, instant, task.user)
    }

    fun delete(task: SyncTask) {
        val document = documentsRepository.findOne(task.params["document"]!!) ?:
                throw NotFoundException("Document is not found")
        documentsController.delete(task.user, document)

    }
}