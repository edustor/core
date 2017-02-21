package ru.edustor.core.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import ru.edustor.core.exceptions.HttpRequestProcessingException
import ru.edustor.core.exceptions.NotFoundException
import ru.edustor.core.model.internal.sync.SyncTask
import ru.edustor.core.repository.LessonRepository
import ru.edustor.core.rest.LessonsController
import java.time.LocalDate

@Component
open class LessonsSyncController @Autowired constructor(
        val lessonsController: LessonsController,
        val lessonRepository: LessonRepository
) {
    fun processTask(task: SyncTask): Any {
        return when (task.method) {
            "create" -> create(task)
            "topic/put" -> setTopic(task)
            "pages/reorder" -> reorderPages(task)
            "delete" -> delete(task)
            "restore" -> restore(task)
            else -> throw NoSuchMethodException("LessonsSyncController cannot resolve ${task.method}")
        }
    }

    fun create(task: SyncTask) {
        val id = task.params["id"]
        val epochDay = task.params["date"]!!.toLong()
        val tagId = task.params["tag"]!!

        lessonsController.create(id!!, tagId, LocalDate.ofEpochDay(epochDay), task.user)
    }

    fun setTopic(task: SyncTask) {
        val lesson = lessonRepository.findOne(task.params["lesson"]!!)
        lessonsController.setTopic(lesson, task.params["topic"], task.user)
    }

    fun reorderPages(task: SyncTask) {
        val lesson = lessonRepository.findOne(task.params["lesson"]!!)
        val page = task.params["page"] ?:
                throw HttpRequestProcessingException(HttpStatus.BAD_REQUEST, "'page' field is not provided")
        val after = task.params["after"]

        return lessonsController.reorderPages(task.user, lesson, page, after)
    }

    fun delete(task: SyncTask) {
        val lesson = lessonRepository.findOne(task.params["lesson"]!!) ?:
                throw NotFoundException("Lesson is not found")
        lessonsController.delete(task.user, lesson)
    }

    fun restore(task: SyncTask) {
        val lesson = lessonRepository.findOne(task.params["lesson"]!!) ?:
                throw NotFoundException("Lesson is not found")
        lessonsController.restore(task.user, lesson)
    }
}