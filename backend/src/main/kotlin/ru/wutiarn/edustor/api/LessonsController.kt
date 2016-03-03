package ru.wutiarn.edustor.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wutiarn.edustor.exceptions.HttpRequestProcessingException
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.repository.DocumentsRepository
import ru.wutiarn.edustor.repository.LessonsRepository

/**
 * Created by wutiarn on 28.02.16.
 */
@RestController
@RequestMapping("/api/lessons")
class LessonsController @Autowired constructor(val lessonsRepo: LessonsRepository, val documentsRepository: DocumentsRepository) {
    @RequestMapping("/list")
    fun listTimetable(subject: Subject): List<Lesson> {
        return lessonsRepo.findBySubject(subject)
    }

    @RequestMapping("/documents")
    fun getDocuments(lesson_id: String): List<Document> {
        val lesson = lessonsRepo.findOne(lesson_id) ?: throw HttpRequestProcessingException(HttpStatus.NOT_FOUND)
        return documentsRepository.findByLesson(lesson)
    }
}
