package ru.wutiarn.edustor.repository

import org.springframework.data.mongodb.repository.MongoRepository
import ru.wutiarn.edustor.models.Group
import ru.wutiarn.edustor.models.Subject
import ru.wutiarn.edustor.models.User

/**
 * Created by wutiarn on 28.02.16.
 */
interface SubjectsRepository : MongoRepository<Subject, String> {
    fun findByOwner(user: User): List<Subject>
    fun findByGroupsContaining(group: Group): List<Subject>
}