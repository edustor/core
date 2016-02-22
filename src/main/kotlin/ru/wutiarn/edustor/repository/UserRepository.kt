package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 22.02.16.
 */
@Repository
interface UserRepository: MongoRepository<User, String> {
    fun findByLogin(login: String): User
    fun countByLogin(login: String): Long
    fun deleteByLogin(login: String): Long
}