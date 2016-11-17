package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Lesson
import ru.edustor.core.model.Subject
import java.time.Instant

interface LessonsRepository : JpaRepository<Lesson, String> {
    fun findBySubject(subject: Subject): List<Lesson>
    fun findByOwner(account: Account): List<Lesson>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Lesson>
}