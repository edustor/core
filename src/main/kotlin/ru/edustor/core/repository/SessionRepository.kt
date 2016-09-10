package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Session
import ru.edustor.core.model.User

@Repository
interface SessionRepository : MongoRepository<Session, String> {
    fun findByToken(token: String): Session?
    fun findByUser(user: User): List<Session>
}