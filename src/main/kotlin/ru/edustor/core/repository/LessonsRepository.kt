package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Document
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import java.time.Instant
import java.time.LocalDate

interface LessonsRepository : JpaRepository<Lesson, String> {
    fun findBySubject(subject: Subject): List<Lesson>
    fun findBySubjectIn(subjects: List<Subject>): List<Lesson>
    fun findByDocumentsContaining(document: Document): Lesson?
    fun findLessonBySubjectAndDate(subject: Subject, date: LocalDate): Lesson?
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}