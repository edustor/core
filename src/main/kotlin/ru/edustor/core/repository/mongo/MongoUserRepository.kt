package ru.edustor.core.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.User
import ru.edustor.core.model.mongo.MongoUser

@Repository
interface MongoUserRepository : MongoRepository<MongoUser, String> {
    fun findByEmail(email: String): User?
    fun findByTelegramLinkToken(token: String): User?
    fun findByTelegramChatId(token: String): User?
}