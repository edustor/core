package ru.edustor.migration.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.Account
import ru.edustor.migration.model.mongo.MongoUser

@Repository
interface MongoUserRepository : MongoRepository<MongoUser, String> {
    fun findByEmail(email: String): Account?
    fun findByTelegramLinkToken(token: String): Account?
    fun findByTelegramChatId(token: String): Account?
}