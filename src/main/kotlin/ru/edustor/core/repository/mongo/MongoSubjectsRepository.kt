package ru.edustor.core.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Subject
import ru.edustor.core.model.User
import ru.edustor.core.model.mongo.MongoSubject
import java.time.Instant

interface MongoSubjectsRepository : MongoRepository<MongoSubject, String> {
    fun findByOwner(user: User): List<Subject>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Subject>
}