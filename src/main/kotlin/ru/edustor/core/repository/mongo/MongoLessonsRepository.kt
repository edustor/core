package ru.edustor.core.repository.mongo

import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import ru.edustor.core.model.mongo.MongoLesson
import java.time.Instant
import java.time.LocalDate

interface MongoLessonsRepository : MongoRepository<MongoLesson, String> {
    fun findBySubject(subject: Subject, pageable: Pageable): List<Lesson>
    fun findBySubject(subject: Subject): List<Lesson>
    fun findBySubjectIn(subjects: List<Subject>): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
    fun findLessonBySubjectAndDate(subject: Subject, date: LocalDate): Lesson?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}