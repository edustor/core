package ru.wutiarn.edustor.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by wutiarn on 28.02.16.
 */
interface LessonsRepository : MongoRepository<Lesson, String>, LessonsRepositoryCustom {
}

interface LessonsRepositoryCustom {
    fun findLesson(subject: Subject, date: LocalDate, start: LocalTime, end: LocalTime): Lesson?
}

@Suppress("unused")
class LessonsRepositoryImpl @Autowired constructor(val mongo: MongoOperations) : LessonsRepositoryCustom {
    override fun findLesson(subject: Subject, date: LocalDate, start: LocalTime, end: LocalTime): Lesson? {
        return mongo.findOne(query(
                where("subject").`is`(subject)
                        .and("date").`is`(date)
                        .and("start").`is`(start)
                        .and("end").`is`(end)
        ), Lesson::class.java)
    }

}
