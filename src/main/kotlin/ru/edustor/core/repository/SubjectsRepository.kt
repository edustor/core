package ru.edustor.core.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Subject
import java.time.Instant

interface SubjectsRepository : JpaRepository<Subject, String> {
    fun findByOwner(user: Account): List<Subject>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Subject>
}