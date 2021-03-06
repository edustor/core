package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.rest.TagsController

@Component
open class TagsSyncController @Autowired constructor(
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
        val tag = task.params["tag"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'tag' field is not provided")
        tagsController.delete(task.account, tag)
    }

    fun restore(task: SyncTask) {
        val tag = task.params["tag"]
                ?: throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'tag' field is not provided")
        tagsController.restore(task.account, tag)
    }
}