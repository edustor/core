package ru.edustor.core.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account
import ru.edustor.core.model.Session
import ru.edustor.core.model.mongo.MongoSession

@Repository
interface MongoSessionRepository : MongoRepository<MongoSession, String> {
    fun findByToken(token: String): Session?
    fun findByUser(user: Account): List<Session>
}