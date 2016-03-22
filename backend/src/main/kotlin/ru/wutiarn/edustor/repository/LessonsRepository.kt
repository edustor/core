package ru.wutiarn.edustor.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import java.time.LocalDate

interface LessonsRepository : MongoRepository<Lesson, String>, LessonsRepositoryCustom {
    fun findBySubject(subject: Subject): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
}

interface LessonsRepositoryCustom {
    fun findLesson(subject: Subject, date: LocalDate): Lesson?
}

@Suppress("unused")
class LessonsRepositoryImpl @Autowired constructor(val mongo: MongoOperations) : LessonsRepositoryCustom {
    override fun findLesson(subject: Subject, date: LocalDate): Lesson? {
        return mongo.findOne(query(
                where("subject").`is`(subject)
                        .and("date").`is`(date)
        ), Lesson::class.java)
    }
}