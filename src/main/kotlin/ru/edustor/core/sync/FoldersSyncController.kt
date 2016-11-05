package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.FoldersRepository
import ru.edustor.core.rest.FoldersController

@Component
open class FoldersSyncController @Autowired constructor(
        val foldersRepo: FoldersRepository,
        val foldersController: FoldersController
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("FoldersSyncController cannot resolve ${task.method}")
        }
    }

    fun delete(task: SyncTask) {
        val folder = foldersRepo.findOne(task.params["folder"]!!) ?:
                throw NotFoundException("Folder is not found")
        foldersController.delete(task.user, folder)
    }

    fun restore(task: SyncTask) {
        val folder = foldersRepo.findOne(task.params["folder"]!!) ?:
                throw NotFoundException("Folder is not found")
        foldersController.restore(task.user, folder)
    }
}