package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import java.time.Instant

interface SubjectsRepository : JpaRepository<Subject, String> {
    fun findByOwner(user: User): List<Subject>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Subject>
}