package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson

/**
 * Created by wutiarn on 22.02.16.
 */
@Repository
interface DocumentsRepository : MongoRepository<Document, String> {
    fun findByUuid(uuid: String): Document?
    fun findByLesson(lesson: Lesson): List<Document>
}