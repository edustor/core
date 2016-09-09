package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByEmail(email: String): User?
}