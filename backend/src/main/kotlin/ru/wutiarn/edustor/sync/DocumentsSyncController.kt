package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.DocumentsController
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.DocumentsRepository

@Component
open class DocumentsSyncController @Autowired constructor(
        val documentsController: DocumentsController,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {

        }
        return null
    }
}