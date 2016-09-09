package ru.edustor.core.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.edustor.core.models.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}