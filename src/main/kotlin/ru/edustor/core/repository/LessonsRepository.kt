package ru.edustor.core.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import java.time.LocalDate

interface LessonsRepository : MongoRepository<Lesson, String> {
    fun findBySubject(subject: Subject, pageable: Pageable): List<Lesson>
    fun findBySubjectIn(subjects: List<Subject>): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
    fun findLessonBySubjectAndDate(subject: Subject, date: LocalDate): Lesson?
}