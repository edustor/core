package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.TagRepository
import ru.edustor.core.rest.TagsController

@Component
open class TagsSyncController @Autowired constructor(
        val tagRepo: TagRepository,
        val tagsController: TagsController
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("TagsSyncController cannot resolve ${task.method}")
        }
    }

    fun delete(task: SyncTask) {
        val tag = tagRepo.findOne(task.params["tag"]!!) ?:
                throw NotFoundException("Tag is not found")
        tagsController.delete(task.user, tag)
    }

    fun restore(task: SyncTask) {
        val tag = tagRepo.findOne(task.params["tag"]!!) ?:
                throw NotFoundException("Tag is not found")
        tagsController.restore(task.user, tag)
    }
}