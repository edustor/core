package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.edustor.core.models.Subject
import ru.edustor.core.models.User

interface SubjectsRepository : MongoRepository<Subject, String> {
    fun findByOwner(user: User): List<Subject>
}