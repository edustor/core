package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.DocumentsController
import ru.wutiarn.edustor.exceptions.NotFoundException
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository
import java.time.Instant
import java.time.LocalDate

@Component
open class DocumentsSyncController @Autowired constructor(
        val subjectsRepository: SubjectsRepository,
        val lessonsRepository: LessonsRepository,
        val documentsController: DocumentsController,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {
            "uuid/activate" -> return activateUUID(task)
            "uuid/activate/date" -> return activateUUIDByDate(task)
            "delete" -> return delete(task)
            else -> throw NoSuchMethodException("DocumentsSyncController cannot resolve ${task.method}")
        }
    }

    fun activateUUID(task: SyncTask) {
        val instant = Instant.ofEpochSecond(task.params["instant"]!!.toLong())
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!)
        documentsController.activateUuid(task.params["uuid"]!!, lesson,
                instant, task.user, task.params["id"]!!)
    }

    fun activateUUIDByDate(task: SyncTask) {
        val instant = Instant.ofEpochSecond(task.params["instant"]!!.toLong())
        val date = LocalDate.ofEpochDay(task.params["date"]!!.toLong())
        val subject = subjectsRepository.findOne(task.params["subject"]!!) ?: throw NotFoundException("Subject is not found")
        documentsController.activateUUidByDate(task.params["uuid"]!!, subject,
                date, instant, task.user, task.params["id"]!!)
    }

    fun delete(task: SyncTask) {
        val document = documentsRepository.findOne(task.params["document"]!!) ?:
                throw NotFoundException("Document is not found")
        documentsController.delete(task.user, document)

    }
}