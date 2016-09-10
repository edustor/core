package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.api.LessonsController
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.model.util.sync.SyncTask
import ru.edustor.core.repository.DocumentsRepository
import ru.edustor.core.repository.SubjectsRepository
import java.time.LocalDate

@Component
open class LessonsSyncController @Autowired constructor(
        val lessonsController: LessonsController,
        val subjectRepo: SubjectsRepository,
        val documentsRepository: DocumentsRepository
) {
    fun processTask(task: SyncTask): Any {
        when (task.method) {
            "date" -> return getByDate(task)
            "date/topic/put" -> return setTopicByDate(task)
            "date/documents/reorder" -> return reorderDocumentsByDate(task)
            else -> throw NoSuchMethodException("LessonsSyncController cannot resolve ${task.method}")
        }
    }

    fun getByDate(task: SyncTask): Lesson {
        val lesson = lessonsController.getLessonByDate(getSubject(task), parseDate(task), task.user)
        return lesson
    }

    fun setTopicByDate(task: SyncTask) {
        lessonsController.setTopicByDate(getSubject(task), parseDate(task), task.params["topic"], task.user)
    }

    fun reorderDocumentsByDate(task: SyncTask) {
        val document = getDocument(task)
        val after = getDocument(task, "after", false)

        return lessonsController.reorderDocumentsByDate(task.user, getSubject(task), parseDate(task), document!!, after)
    }

    private fun getSubject(task: SyncTask): Subject {
        return subjectRepo.findOne(task.params["subject"]!!) ?: throw NotFoundException("Subject is not found")
    }

    private fun getDocument(task: SyncTask, field: String = "document", required: Boolean = true): Document? {
        val key = task.params[field] ?: if (required)
            throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "$field field is not provided") else return null

        return documentsRepository.findOne(key) ?: throw NotFoundException("Document ($field) is not found")

    }

    private fun parseDate(task: SyncTask): LocalDate {
        return LocalDate.ofEpochDay(task.params["date"]!!.toLong())
    }
}