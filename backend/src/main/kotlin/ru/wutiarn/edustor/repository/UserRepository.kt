package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByLogin(login: String): User?
    @Query("{'sessions': {'\$elemMatch': {'token': ?0}}}")
    fun findBySession(session: String): User?
}