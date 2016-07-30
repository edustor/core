package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.LessonsController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.exceptions.NotFoundException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.SubjectsRepository
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
        val lesson = lessonsController.getLessonByDate(getSubject(task), parseDate(task))
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