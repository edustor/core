package ru.wutiarn.edustor.repository

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import ru.wutiarn.edustor.models.Group
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 22.02.16.
 */
@Repository
interface UserRepository : MongoRepository<User, String>, UserRepositoryCustom {
    fun findByLogin(login: String): User?
    @Query("{'sessions': {'\$elemMatch': {'token': ?0}}}")
    fun findBySession(session: String): User?
}

interface UserRepositoryCustom {
    fun processGroupDestroy(group: Group)
    fun findByGroup(group: Group): List<User>
}

@Suppress("unused")
private class UserRepositoryImpl @Autowired constructor(val mongo: MongoOperations) : UserRepositoryCustom {
    override fun processGroupDestroy(group: Group) {
        val users = findByGroup(group)
        for (u in users) {
            u.groups.remove(group)
            mongo.save(u)
        }
    }

    override fun findByGroup(group: Group): List<User> {
        val users = mongo.find(query(where("groups").elemMatch(where("\$id").`is`(ObjectId(group.id)))), User::class.java)
        return users
    }
}