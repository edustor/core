package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.repository.LessonsRepository

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository) {
    @RequestMapping("/list")
    fun listTimetable(subject: Subject): List<Lesson> {
        return lessonsRepo.findBySubject(subject)
    }
}
