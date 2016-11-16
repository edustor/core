package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.rest.SubjectsController

@Component
open class SubjectsSyncController @Autowired constructor(
        val subjectRepo: SubjectsRepository,
        val subjectsController: SubjectsController
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
        subjectsController.delete(task.user, subject)
    }

    fun restore(task: SyncTask) {
        val subject = subjectRepo.findOne(task.params["subject"]!!) ?:
                throw NotFoundException("Subject is not found")
        subjectsController.restore(task.user, subject)
    }
}