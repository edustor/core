package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.Session
import ru.wutiarn.edustor.models.User

@Repository
interface SessionRepository : MongoRepository<Session, String> {
    fun findByToken(token: String): Session?
    fun findByUser(user: User): List<Session>
}