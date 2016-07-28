package ru.wutiarn.edustor.sync

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import ru.wutiarn.edustor.api.LessonsController
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.util.sync.SyncTask
import ru.wutiarn.edustor.repository.SubjectsRepository

@Component
open class LessonsSyncController @Autowired constructor(
        val lessonsController: LessonsController,
        val subjectRepo: SubjectsRepository
) {
    fun processTask(task: SyncTask): Any? {
        when (task.method) {
            "date" -> return getByDate(task)
        }
        return null
    }

    fun getByDate(task: SyncTask): Lesson? {
//        val subject = subjectRepo.findOne(task.params["subject"]) ?: return null
//        val lesson = lessonsController.getLessonByDate(subject, LocalDate.ofEpochDay(task.params["date"]!!.toLong()))
//        return lesson
        return null
    }
}