package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.model.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
    fun findByTelegramLinkToken(token: String): User?
    fun findByTelegramChatId(token: String): User?
}