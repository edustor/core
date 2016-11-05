package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.FoldersRepository
import ru.edustor.core.rest.FoldersController

@Component
open class SubjectsSyncController @Autowired constructor(
        val subjectRepo: FoldersRepository,
        val foldersController: FoldersController
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("SubjectsSyncController cannot resolve ${task.method}")
        }
    }

    fun delete(task: SyncTask) {
        val subject = subjectRepo.findOne(task.params["subject"]!!) ?:
                throw NotFoundException("Subject is not found")
        foldersController.delete(task.user, subject)
    }

    fun restore(task: SyncTask) {
        val subject = subjectRepo.findOne(task.params["subject"]!!) ?:
                throw NotFoundException("Subject is not found")
        foldersController.restore(task.user, subject)
    }
}