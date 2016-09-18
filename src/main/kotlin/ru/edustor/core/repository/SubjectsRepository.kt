package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import java.time.Instant

interface SubjectsRepository : MongoRepository<Subject, String> {
    fun findByOwner(user: User): List<Subject>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Subject>
}