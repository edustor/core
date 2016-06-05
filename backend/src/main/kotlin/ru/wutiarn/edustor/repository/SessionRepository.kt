package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.Session

@Repository
interface SessionRepository : MongoRepository<Session, String> {
    fun findByToken(token: String): Session?
}