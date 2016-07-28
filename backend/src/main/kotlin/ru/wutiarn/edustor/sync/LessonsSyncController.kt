package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.LessonsController
import ru.wutiarn.edustor.exceptions.NotFoundException
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.SubjectsRepository
import java.time.LocalDate

@Component
open class LessonsSyncController @Autowired constructor(
        val lessonsController: LessonsController,
        val subjectRepo: SubjectsRepository
) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {
            "date" -> return getByDate(task)
            "date/topic/put" -> setTopicByDate(task)
        }
        return null
    }

    fun getByDate(task: SyncTask): Lesson {
        val lesson = lessonsController.getLessonByDate(getSubject(task), parseDate(task))
        return lesson
    }

    fun setTopicByDate(task: SyncTask) {
        lessonsController.setTopicByDate(getSubject(task), parseDate(task), task.params["topic"], task.user)
    }

    private fun getSubject(task: SyncTask): Subject {
        return subjectRepo.findOne(task.params["subject"]!!) ?: throw NotFoundException("Subject is not found")
    }

    private fun parseDate(task: SyncTask): LocalDate {
        return LocalDate.ofEpochDay(task.params["date"]!!.toLong())
    }
}