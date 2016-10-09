package ru.edustor.core.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Subject
import ru.edustor.core.model.mongo.MongoSubject
import java.time.Instant

interface MongoSubjectsRepository : MongoRepository<MongoSubject, String> {
    fun findByOwner(user: Account): List<Subject>
    fun findByRemovedOnLessThan(removedOn: Instant): List<Subject>
}