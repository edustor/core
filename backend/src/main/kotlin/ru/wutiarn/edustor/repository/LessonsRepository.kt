package ru.wutiarn.edustor.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Document
import ru.wutiarn.edustor.models.Lesson
import ru.wutiarn.edustor.models.Subject
import java.time.LocalDate

interface LessonsRepository : MongoRepository<Lesson, String> {
    fun findBySubject(subject: Subject, pageable: Pageable): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
    fun findLessonBySubjectAndDate(subject: Subject, date: LocalDate): Lesson?
}