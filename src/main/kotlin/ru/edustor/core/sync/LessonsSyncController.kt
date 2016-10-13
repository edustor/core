package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Document
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.LessonsRepository
import ru.edustor.core.repository.SubjectsRepository
import ru.edustor.core.rest.LessonsController
import java.time.LocalDate

@Component
open class LessonsSyncController @Autowired constructor(
        val lessonsController: LessonsController,
        val lessonsRepository: LessonsRepository,
        val subjectRepo: SubjectsRepository,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "create" -> create(task)
            "topic/put" -> setTopic(task)
            "documents/reorder" -> reorderDocuments(task)
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("LessonsSyncController cannot resolve ${task.method}")
        }
    }

    fun create(task: SyncTask) {
        val id = task.params["id"]
        val epochDay = task.params["date"]!!.toLong()
        val subjectId = task.params["subject"]!!

        val subject = subjectRepo.findOne(subjectId)

        lessonsController.create(id!!, subject, LocalDate.ofEpochDay(epochDay))
    }

    fun setTopic(task: SyncTask) {
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!)
        lessonsController.setTopic(lesson, task.params["topic"], task.user)
    }

    fun reorderDocuments(task: SyncTask) {
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!)
        val document = getDocument(task, required = true)!!
        val after = getDocument(task, "after", false)

        return lessonsController.reorderDocuments(task.user, lesson, document, after)
    }

    private fun getDocument(task: SyncTask, field: String = "document", required: Boolean = true): Document? {
        val key = task.params[field] ?: if (required)
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "$field field is not provided") else return null

        return documentsRepository.findOne(key) ?: throw NotFoundException("Document ($field) is not found")

    }

    fun delete(task: SyncTask) {
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!) ?:
                throw NotFoundException("Lesson is not found")
        lessonsController.delete(task.user, lesson)
    }

    fun restore(task: SyncTask) {
        val lesson = lessonsRepository.findOne(task.params["lesson"]!!) ?:
                throw NotFoundException("Lesson is not found")
        lessonsController.restore(task.user, lesson)
    }
}